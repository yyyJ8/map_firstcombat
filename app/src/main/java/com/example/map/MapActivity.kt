package com.example.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.MyLocationStyle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent

class MapActivity : AppCompatActivity() {
    private lateinit var mMapView: MapView //地图视图容器
    private var aMap: AMap? = null //用于地图操作
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val AMAP_KEY = "9b3396922c9dd915d45b0eb7b564735a"  //高德地图 Web 服务 API Key
    private var currentLat = "" // 纬度
    private var currentLng = "" // 经度

    // 控件
    private lateinit var etKeyword: EditText
    private lateinit var rvPoiList: androidx.recyclerview.widget.RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // 初始化控件
        mMapView = findViewById(R.id.map_view)
        mMapView.onCreate(savedInstanceState)  // 地图生命周期初始化
        mMapView.postDelayed({
            if (aMap == null) {
                aMap = mMapView.map  //延迟获取 aMap 对象
            }
        }, 100)
        etKeyword = findViewById(R.id.et_keyword)
        rvPoiList = findViewById(R.id.rv_poi_list)
        // 初始化RecyclerView
        rvPoiList.layoutManager = LinearLayoutManager(this) //线性布局

        // 获取AMap对象
        if (aMap == null) {
            aMap = mMapView.map
        }

        // 定位按钮点击事件
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_location).setOnClickListener {
            checkAndRequestLocationPermission()
        }

        // 搜索按钮点击事件
        findViewById<android.widget.Button>(R.id.btn_search).setOnClickListener {
            searchPoi()
        }

        // 首次进入自动定位
        checkAndRequestLocationPermission()
    }

    /**
     * 检查定位权限并定位
     */
    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        // 权限已授予，启动定位
        startLocation()
        // 启用地图蓝点
        enableMyLocation()
    }

    /**
     * 启动定位（调用封装的定位工具类）
     */
    private fun startLocation() {
        AmapLocationManager.startLocation(this) { aMapLocation ->
            runOnUiThread { //主线程
                if (aMapLocation == null) {
                    Toast.makeText(this, "定位失败：定位结果为空", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                if (aMapLocation.errorCode != 0) {
                    Toast.makeText(this, "定位错误：${aMapLocation.errorInfo}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val lat = aMapLocation.latitude // latitude和 longitude都是 Double类型，表示十进制的经纬度坐标
                val lng = aMapLocation.longitude
                if (lat == 0.0 && lng == 0.0) { // 排除无效经纬度
                    Toast.makeText(this, "定位失败：获取到无效经纬度", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                currentLat = lat.toString()
                currentLng = lng.toString()
                Toast.makeText(this, "定位成功：$lat, $lng", Toast.LENGTH_SHORT).show()


                if (aMap != null) {
                    try {
                        val targetLatLng = com.amap.api.maps.model.LatLng(lat, lng) //目标位置
                        aMap?.moveCamera(
                            com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(targetLatLng, 15f) //移动镜头到目标位置并缩放到 15 级
                        )
                    } catch (e: Exception) {
                        Toast.makeText(this, "地图定位失败：${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "地图初始化中，稍后重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 跳转POI详情页
     */
    private fun jumpToDetailPage(poi: PoiItem){
        val intent = Intent(this, PoiDetailActivity::class.java)
        intent.putExtra("poi_name", poi.name)
        intent.putExtra("poi_address", poi.address)
        val distance = if (poi.distance.isNotEmpty()) {
            val meter = poi.distance.toInt()
            if (meter >= 1000) "${meter / 1000.0}公里" else "${meter}米"
        } else {
            "未知距离"
        }
        intent.putExtra("poi_distance", distance)
        intent.putExtra("poi_type", poi.type)
        intent.putExtra("poi_location", poi.location)
        startActivity(intent)
    }

    /**
     * 显示POI列表（复用逻辑：网络请求/缓存读取后统一调用）
     */
    private fun showPoiList(poiList: List<PoiItem>) {
        rvPoiList.visibility = View.VISIBLE //显示RecyclerView
        val adapter = PoiAdapter(poiList) { poi ->
            // item点击回调
            val locationArr = poi.location.split(",")
            if (locationArr.size == 2){
                val lng = locationArr[0].toDouble()
                val lat = locationArr[1].toDouble()
                val latLng = com.amap.api.maps.model.LatLng(lat, lng) //创建高德地图的 LatLng 坐标对象

                val markerOptions = com.amap.api.maps.model.MarkerOptions()  //创建标记点的配置对象
                    .position(latLng) // Marker位置
                    .title(poi.name) // InfoWindow标题
                    .snippet(poi.address) // 显示地址
                    .icon(com.amap.api.maps.model.BitmapDescriptorFactory.defaultMarker()) // 默认红色图标
                    .draggable(false)
                val marker = aMap?.addMarker(markerOptions)
                marker?.showInfoWindow()
                aMap?.moveCamera(com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(latLng, 16f))

                aMap?.setOnInfoWindowClickListener {
                    jumpToDetailPage(poi)
                }
            }
            jumpToDetailPage(poi)
            Toast.makeText(this@MapActivity, "点击了${poi.name}", Toast.LENGTH_SHORT).show()
        }
        rvPoiList.adapter = adapter
    }

    /**
     * 搜索POI（调用高德Web API）
     */
    private fun searchPoi() {
        // 校验输入和定位
        val keyword = etKeyword.text.toString().trim()
        if (keyword.isEmpty()) {
            Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentLat.isEmpty() || currentLng.isEmpty()) {
            Toast.makeText(this, "请先定位获取当前位置", Toast.LENGTH_SHORT).show()
            checkAndRequestLocationPermission()
            return
        }
        //判断联网
        if (!NetworkUtil.isNetworkAvailable(this)){
            val cachedPoiList = SPCacheManager.getCache(this, keyword, currentLat, currentLng)
            if (cachedPoiList != null && cachedPoiList.isNotEmpty()) {
                // 显示缓存数据
                Toast.makeText(this, "无网络，使用缓存数据", Toast.LENGTH_SHORT).show()
                showPoiList(cachedPoiList) // 调用显示列表方法
            } else {
                Toast.makeText(this, "无网络且无缓存数据", Toast.LENGTH_SHORT).show()
                rvPoiList.visibility = View.GONE // 隐藏空列表
            }
            return
        }
        //  构建Retrofit请求
        val poiApi = RetrofitClient.create(AmapPoiApi::class.java)
        // 高德POI API的location参数格式是“经度,纬度”
        val location = "$currentLng,$currentLat"
        poiApi.searchNearbyPoi(location, keyword, AMAP_KEY).enqueue(object : Callback<PoiResponse> {
            override fun onResponse(call: Call<PoiResponse>, response: Response<PoiResponse>) {
                if (response.isSuccessful) {
                    val poiResponse = response.body()
                    if (poiResponse?.status == "1") {
                        // 搜索成功
                        val poiList = poiResponse.pois
                        if (poiList.isEmpty()) {
                            Toast.makeText(this@MapActivity, "未找到相关POI", Toast.LENGTH_SHORT).show()
                            rvPoiList.visibility = View.GONE
                            return
                        }
                        //保存缓存
                        SPCacheManager.saveCache(
                            this@MapActivity,
                            keyword,
                            currentLat,
                            currentLng,
                            poiResponse.pois
                        )

                        Toast.makeText(this@MapActivity, "搜索成功（已缓存）", Toast.LENGTH_SHORT).show()
                        // 显示POI列表
                        showPoiList(poiList)
                    } else {
                        // 搜索失败（Key错误、参数错误等）
                        Toast.makeText(this@MapActivity, "搜索失败：${poiResponse?.info}", Toast.LENGTH_SHORT).show()
                        Log.e("POI搜索", "失败原因：${poiResponse?.info}")
                        rvPoiList.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@MapActivity, "网络请求失败", Toast.LENGTH_SHORT).show()
                    rvPoiList.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<PoiResponse>, t: Throwable) {
                // 网络异常
                Toast.makeText(this@MapActivity, "网络异常：${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("POI搜索", "网络失败：", t)
                rvPoiList.visibility = View.GONE
            }
        })
    }

    /**
     * 启用地图蓝点
     */
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION //精确定位权限
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val myLocationStyle = MyLocationStyle() //MyLocationStyle()是高德地图 SDK 提供的专门用于设置定位样式的类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        aMap?.myLocationStyle = myLocationStyle
        aMap?.isMyLocationEnabled = true //启用高德地图的 定位图层
    }

    /**
     * 权限申请结果回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation()
                enableMyLocation()
            } else {
                Toast.makeText(this, "需要定位权限才能使用定位功能", Toast.LENGTH_LONG).show()
            }
        }
    }

    // MapView生命周期管理
    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) { //保存地图当前状态，用于配置变更后恢复现场
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
        // 销毁定位客户端
        AmapLocationManager.destroyLocation()
    }

    override fun onLowMemory() { //在系统内存紧张时，让地图释放部分资源
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}