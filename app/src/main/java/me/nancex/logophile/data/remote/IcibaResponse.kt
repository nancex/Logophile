package me.nancex.logophile.data.remote

data class IcibaResponse(
    val message: List<IcibaMessage>?,
    val status: Int
)

data class IcibaMessage(
    val key: String?,
    val paraphrase: String?,
    val value: Int?,
    val means: List<IcibaMean>?
)

data class IcibaMean(
    val part: String?,
    val means: List<String>?
)
