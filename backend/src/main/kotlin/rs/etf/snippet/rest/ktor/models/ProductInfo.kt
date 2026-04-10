package rs.etf.snippet.rest.ktor.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductInfo(
    val barcode:String,
    val name:String,
    val searchName:String,
    val searchNameTrigrams:List<String>,
    val category:String,
    val brand:String,
)
