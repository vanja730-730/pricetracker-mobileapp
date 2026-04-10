package rs.etf.snippet.rest.ktor

import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinInstant
import rs.etf.snippet.rest.ktor.BULK_OPS
import rs.etf.snippet.rest.ktor.db.DBInfo
import rs.etf.snippet.rest.ktor.models.Prices
import rs.etf.snippet.rest.ktor.models.ProductInfo
import rs.etf.snippet.rest.ktor.models.Stores
import rs.etf.snippet.rest.ktor.plugins.*
import rs.etf.snippet.rest.ktor.utilities.generateTrigrams
import rs.etf.snippet.rest.ktor.utilities.normalizeSerbian
import rs.etf.snippet.rest.ktor.utilities.sanitize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

fun main() {
    embeddedServer(
        Netty,
        host = "0.0.0.0",
        port = 8080,
        module = Application::module
    ).start(wait = true)
}
val OPENDATA_APIKEY = System.getenv("OPENDATA_APIKEY") ?: throw IllegalStateException("OPENDATA_APIKEY not found in environment")
const val OPENDATA_BASEURL="https://data.gov.rs/sr/datasets/r/"
val OPENDATA_URLS = listOf(Pair(Stores.MAXI,"09f32747-ba41-45c6-a2ee-b6037d5d82bb"),
    Pair(Stores.IDEA,"5b5b6537-365e-4e9c-8622-cddb6998a7b8"),
    Pair(Stores.UNIVER,"9ea1848a-6180-4839-ab64-df99764b68c4"),
    Pair(Stores.LIDL, "d2c3585c-aed7-4ce5-90a1-ea6eb96c47bc"),
    Pair(Stores.DIS, "844a28cb-9b83-441f-a524-76055b610b73"),
    Pair(Stores.AMAN, "90465a2b-ad9f-4c42-b727-4bbc2bcbd7bb"),
    Pair(Stores.AROMA, "b5f127a9-0606-45b0-bf91-056f57494474"),
    Pair(Stores.VERO,"80e6f006-045a-4722-9502-e0980bcaebed"),
    Pair(Stores.GOMEX, "92308295-c31d-4a05-af92-617895466d23"),
    Pair(Stores.SUMADIJA, "7ec04366-a52a-4776-ac4c-cab2e528a2e2"),
    Pair(Stores.SVETOFOR, "8e0dfb07-3bd3-41db-8ea1-8c8ac754fd67")
    )

fun Application.module() {
    configureSerialization()
    configureRouting()
    val httpClient = HttpClient(CIO){
        defaultRequest {
            header("x-api-key",OPENDATA_APIKEY)
        }
        install(HttpTimeout){
            requestTimeoutMillis= HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            socketTimeoutMillis=HttpTimeoutConfig.INFINITE_TIMEOUT_MS
        }
    }
    launch(Dispatchers.IO){
        while(isActive){
            val jobs = OPENDATA_URLS.map { pair ->
                launch {
                    try {
                        retrieveCsv(httpClient, OPENDATA_BASEURL + pair.second, DBInfo.products, DBInfo.prices, pair.first)
                    } catch (e: Exception) {
                        println("Failed sync for ${pair.first}: ${e.message}")
                    }
                }
            }
            jobs.joinAll()
            delay(1.days)
        }
    }
}

const val CATEGORY_INDEX = 1
const val NAME_INDEX = 2
const val BRAND_INDEX = 3
const val BARCODE_INDEX = 4
const val PRICE_INDEX=8
const val DISCOUNT_INDEX = 10
const val DATE_INDEX = 7
const val DISCOUNT_START_INDEX = 11
const val DISCOUNT_END_INDEX = 12
const val BULK_OPS=4*1024
var cntr=0
var processed=0UL
suspend fun retrieveCsv(client: HttpClient, path: String,
                        productCollection: MongoCollection<ProductInfo>, priceCollection: MongoCollection<Prices>, store: Stores) {
    client.prepareGet(path).execute{
        response ->
        val channel = response.bodyAsChannel()
        var bulkCnt=0
        val bulkOpsProduct = mutableListOf<ReplaceOneModel<ProductInfo>>()
        val bulkOpsPrice = mutableListOf<ReplaceOneModel<Prices>>()
        val options= ReplaceOptions().upsert(true)
        val bulkOptions = BulkWriteOptions().ordered(false)
        channel.toInputStream().bufferedReader().useLines {
            lines->
            lines.forEach { line ->
                line.sanitize()
                val values = line.split(";")
                if(values.size>13&&values[0].length<3){
                    processed++
                    val filter= Filters.eq("barcode",values[BARCODE_INDEX])
                    val priceFilter = Filters.and(Filters.eq("barcode",values[BARCODE_INDEX]), Filters.eq("priceDate",values[DATE_INDEX]))
                    val doc = ProductInfo(values[BARCODE_INDEX],values[NAME_INDEX],values[NAME_INDEX].lowercase().normalizeSerbian() , values[NAME_INDEX].lowercase().generateTrigrams(),values[CATEGORY_INDEX].lowercase(), values[BRAND_INDEX])
                    val priceDoc = Prices(values[BARCODE_INDEX], values[PRICE_INDEX].toDoubleOrNull(),
                        parseCsvDateToInstant(values[DATE_INDEX]), values[DISCOUNT_INDEX].toDoubleOrNull(),
                        if(values[DISCOUNT_START_INDEX]=="")null else parseCsvDateToInstant(values[DISCOUNT_START_INDEX]),
                        if(values[DISCOUNT_END_INDEX]=="")null else parseCsvDateToInstant(values[DISCOUNT_END_INDEX]),
                        store)
                    bulkOpsProduct.add(ReplaceOneModel<ProductInfo>(filter, doc,options))
                    bulkOpsPrice.add(ReplaceOneModel<Prices>(priceFilter,priceDoc,options))
                    bulkCnt++
                    if(bulkCnt==BULK_OPS) {
                        bulkCnt = 0
                        productCollection.bulkWrite(bulkOpsProduct, bulkOptions)
                        bulkOpsProduct.clear()
                        cntr++
                        println("WRITTENPRODUCT$cntr")
                        priceCollection.bulkWrite(bulkOpsPrice, bulkOptions)
                        bulkOpsPrice.clear()
                        println("WRITTENPRICE$cntr")
                    }
                }
            }
            if (bulkOpsProduct.isNotEmpty()) {
                productCollection.bulkWrite(bulkOpsProduct, bulkOptions)
                bulkOpsProduct.clear()
                priceCollection.bulkWrite(bulkOpsPrice, bulkOptions)
                bulkOpsPrice.clear()
            }
        }
    }
    println("DONEDONE")
    println("Processed $processed products")
}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

@OptIn(ExperimentalTime::class)
fun parseCsvDateToInstant(dateString: String): kotlinx.datetime.Instant {
    if (dateString.isBlank()) return Instant.now().toKotlinInstant()
    val localDate = LocalDate.parse(dateString, formatter)
    return localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toKotlinInstant()
}
