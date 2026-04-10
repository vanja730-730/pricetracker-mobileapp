package rs.etf.snippet.rest.ktor.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Prices(
    val barcode:String,
    val regularPrice: Double?,
    @Contextual
    val priceDate: Instant,
    val discountedPrice: Double?,
    val discountStartDate: Instant?,
    val discountEndDate: Instant?,
    val store: Stores
)
