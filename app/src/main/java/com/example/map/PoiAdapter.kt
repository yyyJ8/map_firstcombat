package com.example.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// POI列表适配器
class PoiAdapter(
    private val poiList: List<PoiItem>,
    private val onItemClick: (PoiItem) -> Unit // item点击回调
) : RecyclerView.Adapter<PoiAdapter.PoiViewHolder>() {

    // 视图持有者
    inner class PoiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_poi_name)
        val tvAddress: TextView = itemView.findViewById(R.id.tv_poi_address)
        val tvDistance: TextView = itemView.findViewById(R.id.tv_poi_distance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiViewHolder {
        // 加载item布局
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poi, parent, false)
        return PoiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoiViewHolder, position: Int) {
        val poi = poiList[position]
        // 绑定数据
        holder.tvName.text = poi.name
        holder.tvAddress.text = poi.address
        // 距离转换（米→公里，更友好）
        val distance = if (poi.distance.isNotEmpty()) {
            val meter = poi.distance.toInt()
            if (meter >= 1000) "${meter / 1000.0}公里" else "${meter}米"
        } else {
            "未知距离"
        }
        holder.tvDistance.text = distance

        // item点击事件
        holder.itemView.setOnClickListener {
            onItemClick(poi)
        }
    }

    override fun getItemCount(): Int {
        return poiList.size
    }
}