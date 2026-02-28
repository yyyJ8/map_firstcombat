package com.example.map
import com.google.gson.annotations.SerializedName

//映射高德地图 POI 接口返回的 JSON 数据
data class PoiResponse(
    @SerializedName("status") val status: String, // 1=成功，0=失败
    @SerializedName("info") val info: String, // 错误信息
    @SerializedName("pois") val pois: List<PoiItem> // POI列表
)//@SerializedName指定 JSON 字段名和 Kotlin 属性名的对应关系

data class PoiItem(
    @SerializedName("id") val id: String, // POI唯一ID
    @SerializedName("name") val name: String, // 名称
    @SerializedName("address") val address: String, // 地址
    @SerializedName("distance") val distance: String, // 距离
    @SerializedName("location") val location: String, // 经纬度
    @SerializedName("type") val type: String // 类型
//    @SerializedName("tel") val tel: String = ""
)