package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileZoneData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.inflate
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible

/**
 * Created by fancy on 2017/7/10.
 * Copyright © 2017 O2. All rights reserved.
 */

abstract class CloudFileZoneAdapter(val items:List<CloudFileZoneData>) : RecyclerView.Adapter<CommonRecyclerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonRecyclerViewHolder {
        return when(viewType) {
            0 -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_cloud_file_zone_header))
            1 -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_cloud_file_zone_favorite))
            else -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_cloud_file_my_zone))
        }
    }

    override fun onBindViewHolder(holder: CommonRecyclerViewHolder, position: Int) {
        when(items[position]){
            is CloudFileZoneData.GroupHeader -> {
                val header = items[position] as CloudFileZoneData.GroupHeader
                val isLast = if(position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next is CloudFileZoneData.GroupHeader
                }
                val bottom = holder.getView<View>(R.id.view_item_cloud_file_zone_header_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                holder.setText(R.id.tv_item_cloud_file_zone_header_title, header.name)
            }
            is CloudFileZoneData.MyFavorite -> {
                val favorite = items[position] as CloudFileZoneData.MyFavorite
                val isLast = if (position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next is CloudFileZoneData.GroupHeader
                }
                val bottom = holder.getView<View>(R.id.view_item_cloud_file_favorite_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                bindMyFavorite(favorite, holder)
                val body = holder.getView<LinearLayout>(R.id.ll_item_cloud_file_body)
                body.setOnClickListener { clickMyFavorite(favorite) }
                val moreBtn = holder.getView<LinearLayout>(R.id.ll_item_cloud_file_more_btn)
                moreBtn.setOnClickListener { clickMyFavoriteMoreBtn(favorite) }
                //holder.convertView?.setOnClickListener { clickMyFavorite(department) }
            }
            else -> {
                val zone = items[position] as CloudFileZoneData.MyZone
                val isLast = if (position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next is CloudFileZoneData.GroupHeader
                }
                val bottom = holder.getView<View>(R.id.view_item_cloud_file_zone_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                bindMyZone(zone, holder)
//                holder.convertView?.setOnClickListener { clickMyZone(collect) }
                val body = holder.getView<LinearLayout>(R.id.ll_item_cloud_file_body)
                body.setOnClickListener { clickMyZone(zone) }
                val moreBtn = holder.getView<LinearLayout>(R.id.ll_item_cloud_file_more_btn)
                moreBtn.setOnClickListener { clickMyZoneMoreBtn(zone) }
            }
        }
    }


    override fun getItemCount(): Int = items.size
    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is CloudFileZoneData.GroupHeader -> 0
            is CloudFileZoneData.MyFavorite -> 1
            else -> 2
        }
    }

    abstract fun bindMyFavorite(favorite:CloudFileZoneData.MyFavorite, holder: CommonRecyclerViewHolder?)
    abstract fun clickMyFavorite(favorite: CloudFileZoneData.MyFavorite)
    abstract fun clickMyFavoriteMoreBtn(favorite: CloudFileZoneData.MyFavorite)
    abstract fun bindMyZone(zone: CloudFileZoneData.MyZone, holder: CommonRecyclerViewHolder?)
    abstract fun clickMyZone(zone: CloudFileZoneData.MyZone)
    abstract fun clickMyZoneMoreBtn(zone: CloudFileZoneData.MyZone)

}