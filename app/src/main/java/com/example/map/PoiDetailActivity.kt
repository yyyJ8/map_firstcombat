package com.example.map

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PoiDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_detail)

        // 1. 接收Intent传递的POI数据
        val poiName = intent.getStringExtra("poi_name") ?: "未知名称"
        val poiAddress = intent.getStringExtra("poi_address") ?: "未知地址"
        val poiDistance = intent.getStringExtra("poi_distance") ?: "未知距离"
        val poiType = intent.getStringExtra("poi_type") ?: "未知类型"

        val poiLocation = intent.getStringExtra("poi_location") ?: "未知经纬度"

        // 2. 绑定控件显示数据
        findViewById<TextView>(R.id.tv_detail_name).text = poiName
        findViewById<TextView>(R.id.tv_detail_distance).text = poiDistance
        findViewById<TextView>(R.id.tv_detail_type).text = poiType
        findViewById<TextView>(R.id.tv_detail_address).text = poiAddress

        findViewById<TextView>(R.id.tv_detail_location).text = poiLocation

        // 3. 返回按钮：关闭详情页返回MapActivity
        findViewById<FloatingActionButton>(R.id.fab_back).setOnClickListener {
            finish()
        }
    }
}