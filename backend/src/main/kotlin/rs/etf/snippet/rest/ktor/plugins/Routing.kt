package rs.etf.snippet.rest.ktor.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import rs.etf.snippet.rest.ktor.routes.priceRoutes
import rs.etf.snippet.rest.ktor.routes.productRoutes

fun Application.configureRouting() {
    routing {
        productRoutes()
        priceRoutes()
    }
}
