package com.example.map

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//高德地图 POI 搜索接口
interface AmapPoiApi {
    @GET("v3/place/around")
    fun searchNearbyPoi(
        @Query("location") location: String,
        @Query("keywords") keywords: String,
        @Query("key") key: String,
        @Query("radius") radius: Int = 1000,
        @Query("types") types: String = "",
        @Query("output") output: String = "json" // 固定返回JSON
    ): Call<PoiResponse>
}