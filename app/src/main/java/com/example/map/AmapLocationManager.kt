package com.example.map
import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
object AmapLocationManager { // 定义一个单例对象，用于管理高德地图的定位功能
    private var mLocationClient: AMapLocationClient? = null  //用于发起定位请求的核心类
    //AMapLocationClient是高德地图 SDK 中用于执行定位操作的核心类，通过它可以启动定位、设置定位选项、监听定位结果，是实现 Android 定位功能的关键对象。
    private var locationCallback: ((AMapLocation?) -> Unit)? = null //声明一个定位回调函数

    fun startLocation(context: Context, callback: (AMapLocation?) -> Unit){
        locationCallback = callback
        if (mLocationClient == null){
            mLocationClient = AMapLocationClient(context.applicationContext) //使用applicationContext初始化定位客户端
        }
        val locationOption = AMapLocationClientOption() // 定位参数配置对象
        locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        locationOption.isOnceLocation = true
        locationOption.httpTimeOut = 30000
        locationOption.isLocationCacheEnable = false
        mLocationClient?.setLocationOption(locationOption)
        mLocationClient?.setLocationListener(locationListener)
        mLocationClient?.startLocation()
    }
    private val locationListener= AMapLocationListener{
        aMapLocation ->
        when (aMapLocation?.errorCode){
            0 -> {
                // 定位成功：返回定位结果
                locationCallback?.invoke(aMapLocation)
            }
            else -> {
                // 定位失败：返回null，外部处理错误
                locationCallback?.invoke(null)
            }
        }
        stopLocation()
    }

    fun stopLocation() {
        mLocationClient?.stopLocation()
    }
    fun destroyLocation() {
        mLocationClient?.onDestroy()
        mLocationClient = null
        locationCallback = null
    }
}