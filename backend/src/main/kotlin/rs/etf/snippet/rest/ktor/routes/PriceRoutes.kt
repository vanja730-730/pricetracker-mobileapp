package rs.etf.snippet.rest.ktor.routes

import com.mongodb.client.model.Filters
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.toList
import rs.etf.snippet.rest.ktor.db.DBInfo

fun Route.priceRoutes(){
    route("/prices"){
        get("/getPrice/{barcode}"){
            val barcode = call.parameters["barcode"]?:return@get call.respondText("Missing barcode parameter", status= HttpStatusCode.BadRequest)
            val store = call.queryParameters["store"]
            val filter = if(store.isNullOrEmpty()) Filters.and(Filters.eq("barcode", barcode),
                Filters.eq("store", store))
                else Filters.eq("barcode", barcode)
            val res = DBInfo.prices.find(filter)
            call.respond(res.toList())
        }
    }
}