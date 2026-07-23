package me.nancex.logophile.data.remote

data class DictionaryResponseItem(
    val word: String?,
    val phonetic: String?,
    val phonetics: List<PhoneticItem>?
)

data class PhoneticItem(
    val text: String?,
    val audio: String?
)
