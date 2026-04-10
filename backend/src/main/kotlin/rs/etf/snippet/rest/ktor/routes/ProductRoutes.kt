package rs.etf.snippet.rest.ktor.routes

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import rs.etf.snippet.rest.ktor.db.DBInfo
import rs.etf.snippet.rest.ktor.utilities.generateTrigrams
import rs.etf.snippet.rest.ktor.utilities.normalizeSerbian

fun Route.productRoutes() {
    route("/products") {
        get("/barcode/{barcode}"){
            val bc = call.parameters["barcode"]?: return@get call.respondText("Missing barcode parameter",status=HttpStatusCode.BadRequest)
            val filter = Filters.eq("barcode", bc)
            val res = DBInfo.products.find(filter).firstOrNull()?:return@get call.respondText("Product not found", status = HttpStatusCode.NotFound)
            call.respond(res)
        }
        get("/search/{category}/{name}/{page}"){
            val pageSize = 20
            val category = call.parameters["category"]?: return@get call.respondText("Missing category parameter",status=HttpStatusCode.BadRequest)
            val productName = call.parameters["name"]?: return@get call.respondText("Missing name parameter",status=HttpStatusCode.BadRequest)
            productName.lowercase().normalizeSerbian()
            val page = call.parameters["page"]?.toIntOrNull() ?: 0
            val combinedNames = mutableListOf<String>()
            combinedNames.addAll(productName.lowercase().normalizeSerbian().split(" "))
            val combinedNamesNew = combinedNames.map {
                "+$it"
            }.toMutableList()
            combinedNamesNew.addAll(productName.generateTrigrams())
            val filter = Filters.and(Filters.eq("category", category.lowercase()), Filters.text(combinedNamesNew.joinToString(" ")))
            val projection = Projections.metaTextScore("score")
            val sort = Sorts.metaTextScore("score")
            val results = DBInfo.products.find(filter)
                .projection(projection)
                .sort(sort)
                .skip(page * pageSize)
                .limit(pageSize)
            call.respond(results.toList())
        }
        get("/category"){
            val res = DBInfo.products.distinct<String>("category").toList().sorted()
            call.respond(res)
        }
    }
}