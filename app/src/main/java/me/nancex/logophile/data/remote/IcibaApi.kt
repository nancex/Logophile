package me.nancex.logophile.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface IcibaApi {
    @GET("interface/index.php")
    suspend fun getWordSuggest(
        @Query("c") c: String = "word",
        @Query("m") m: String = "getsuggest",
        @Query("nums") nums: Int = 1,
        @Query("is_need_mean") isNeedMean: Int = 1,
        @Query("word") word: String
    ): IcibaResponse
}
