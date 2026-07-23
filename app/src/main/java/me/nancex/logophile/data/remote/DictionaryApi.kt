package me.nancex.logophile.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    suspend fun getWordEntry(
        @Path("word") word: String
    ): List<DictionaryResponseItem>
}
