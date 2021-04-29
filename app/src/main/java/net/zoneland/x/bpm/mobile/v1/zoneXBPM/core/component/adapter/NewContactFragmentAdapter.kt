package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.NewContactFragmentVO
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.inflate
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible

/**
 * Created by fancy on 2017/7/10.
 * Copyright © 2017 O2. All rights reserved.
 */

abstract class NewContactFragmentAdapter(val items:List<NewContactFragmentVO>) : RecyclerView.Adapter<CommonRecyclerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonRecyclerViewHolder {
        return when(viewType) {
            0 -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_contact_fragment_header))
            1 -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_contact_fragment_body))
            else -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_new_contact_fragment_body_collect))
        }
    }

    override fun onBindViewHolder(holder: CommonRecyclerViewHolder, position: Int) {
        when(items[position]){
            is NewContactFragmentVO.GroupHeader -> {
                val header = items[position] as NewContactFragmentVO.GroupHeader
                val isLast = if(position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next is NewContactFragmentVO.GroupHeader
                }
                val bottom = holder.getView<View>(R.id.view_item_contact_fragment_header_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                holder.setText(R.id.tv_item_contact_fragment_header_title, header.name)
                        ?.setImageViewDrawable(R.id.image_item_contact_fragment_header_icon, FancySkinManager.instance().getDrawable(holder.convertView.context, header.resId))
            }
            is NewContactFragmentVO.MyDepartment -> {
                val department = items[position] as NewContactFragmentVO.MyDepartment
                val isLast = if (position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next is NewContactFragmentVO.GroupHeader
                }
                val bottom = holder.getView<View>(R.id.view_item_contact_fragment_body_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                bindMyDepartment(department, holder)
                holder.convertView?.setOnClickListener { clickMyDepartment(department) }
            }
            else -> {
                val collect = items[position] as NewContactFragmentVO.MyCollect
                val isLast = if (position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next is NewContactFragmentVO.GroupHeader
                }
                val bottom = holder.getView<View>(R.id.view_item_contact_fragment_body_collect_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                bindMyCollect(collect, holder)
                holder.convertView?.setOnClickListener { clickMyCollect(collect) }
            }
        }
    }


    override fun getItemCount(): Int = items.size
    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is NewContactFragmentVO.GroupHeader -> 0
            is NewContactFragmentVO.MyDepartment -> 1
            else -> 2
        }
    }

    abstract fun bindMyDepartment(department: NewContactFragmentVO.MyDepartment, holder: CommonRecyclerViewHolder?)
    abstract fun clickMyDepartment(department: NewContactFragmentVO.MyDepartment)
    abstract fun bindMyCollect(collect: NewContactFragmentVO.MyCollect, holder: CommonRecyclerViewHolder?)
    abstract fun clickMyCollect(collect: NewContactFragmentVO.MyCollect)

}