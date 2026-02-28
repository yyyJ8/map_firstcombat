package com.example.map

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SPCacheManager {
    private const val SP_NAME = "poi_search_cache"
    private const val KEY_POI_LIST = "last_poi_cache" // 按你要求的key命名
    private const val KEY_LAST_KEYWORD = "last_keyword"
    private const val KEY_LAST_LAT = "last_lat"
    private const val KEY_LAST_LNG = "last_lng"
    private const val KEY_CACHE_TIME = "cache_time"
    private const val CACHE_EXPIRE_TIME = 30 * 60 * 1000L // 30分钟过期

    private fun getSP(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    // 保存缓存：搜索成功后调用
    fun saveCache(context: Context, keyword: String, lat: String, lng: String, poiList: List<PoiItem>) {
        val editor = getSP(context).edit()
        val gson = Gson()
        val poiJson = gson.toJson(poiList)
        editor.putString(KEY_POI_LIST, poiJson)
        editor.putString(KEY_LAST_KEYWORD, keyword)
        editor.putString(KEY_LAST_LAT, lat)
        editor.putString(KEY_LAST_LNG, lng)
        editor.putLong(KEY_CACHE_TIME, System.currentTimeMillis())
        editor.apply()
    }

    // 获取缓存：进入APP/无网时调用
    fun getCache(context: Context, keyword: String, lat: String, lng: String): List<PoiItem>? {
        val sp = getSP(context)
        // 检查过期
        val cacheTime = sp.getLong(KEY_CACHE_TIME, 0)
        if (System.currentTimeMillis() - cacheTime > CACHE_EXPIRE_TIME) {
            clearCache(context)
            return null
        }
        // 检查关键词+经纬度匹配
        val lastKeyword = sp.getString(KEY_LAST_KEYWORD, "") ?: ""
        val lastLat = sp.getString(KEY_LAST_LAT, "") ?: ""
        val lastLng = sp.getString(KEY_LAST_LNG, "") ?: ""
        if (lastKeyword != keyword || lastLat != lat || lastLng != lng) return null
        // 解析POI列表
        val poiJson = sp.getString(KEY_POI_LIST, "") ?: ""
        if (poiJson.isEmpty()) return null
        val gson = Gson()
        val type = object : TypeToken<List<PoiItem>>() {}.type
        return gson.fromJson(poiJson, type)
    }

    // 清空缓存
    fun clearCache(context: Context) {
        getSP(context).edit().clear().apply()
    }
}