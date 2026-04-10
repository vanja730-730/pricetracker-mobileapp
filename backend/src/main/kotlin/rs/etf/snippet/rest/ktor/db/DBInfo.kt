package rs.etf.snippet.rest.ktor.db

import com.mongodb.kotlin.client.coroutine.MongoClient
import rs.etf.snippet.rest.ktor.models.Prices
import rs.etf.snippet.rest.ktor.models.ProductInfo

object DBInfo {
    val dbClient = MongoClient.create("mongodb://localhost:27017")
    val db = dbClient.getDatabase("products")
    val products = db.getCollection<ProductInfo>("info")
    val prices = db.getCollection<Prices>("prices")
}