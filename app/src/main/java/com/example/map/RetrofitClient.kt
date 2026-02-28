package com.example.map

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//统一创建和提供 Retrofit 实例，并通过 create()方法把网络请求接口的 Class 对象转换成可以直接调用网络请求的实现对象
object RetrofitClient {
    private const val BASE_URL = "https://restapi.amap.com/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun <T> create(serviceClass: Class<T>): T {
        return instance.create(serviceClass)
    } //把一个网络请求接口的 Class 对象，变成一个可以真正调用网络请求的实例
}