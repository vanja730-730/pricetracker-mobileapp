package rs.etf.snippet.rest.ktor.utilities

fun String.normalizeSerbian(): String{
    return this.lowercase()
        .replace("č", "c")
        .replace("ć", "c")
        .replace("š", "s")
        .replace("ž", "z")
        .replace("đ", "dj")
        .trim()
}

fun String.sanitize(): String {
    return this.trim()
        .replace("\uFEFF", "")
        .replace("\u00A0", "")
        .replace("\u0000", "")
        .replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "")
}

fun String.generateTrigrams(): List<String> {
    val cleaned = this.replace(" ","")
    if (cleaned.length < 3) return listOf(cleaned)
    return cleaned.windowed(3)
}