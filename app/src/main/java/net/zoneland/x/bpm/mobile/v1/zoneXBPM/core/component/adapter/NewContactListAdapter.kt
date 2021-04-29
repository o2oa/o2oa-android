package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.NewContactListVO
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.inflate
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible

/**
 * Created by fancy on 2017/4/25.
 */

abstract class NewContactListAdapter(var items: ArrayList<NewContactListVO>) : RecyclerView.Adapter<CommonRecyclerViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: CommonRecyclerViewHolder, position: Int) {
        when(items[position]) {
            is NewContactListVO.Department -> {
                val department = items[position] as NewContactListVO.Department
                val isLast = if (position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next !is NewContactListVO.Department
                }
                val bottom = holder.getView<View>(R.id.view_item_contact_body_org_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                val isFirst = position == 0
                val top = holder.getView<View>(R.id.view_item_contact_body_org_top)
                val topLine = holder.getView<View>(R.id.view_item_contact_body_org_top_line)
                if (isFirst) {
                    top.visible()
                    topLine.gone()
                }else {
                    top.gone()
                    topLine.visible()
                }
                bindDepartment(holder, department, position)
                holder.convertView?.setOnClickListener { clickDepartment(department) }
            }
            else -> {
                val identity = items[position] as NewContactListVO.Identity
                val isLast = if (position == items.size-1) {
                    true
                }else {
                    val next = items[position+1]
                    next !is NewContactListVO.Identity
                }
                val bottom = holder.getView<View>(R.id.view_item_contact_person_body_bottom)
                if (isLast) {
                    bottom.visible()
                }else {
                    bottom.gone()
                }
                val isFirst = if (position == 0) {
                    true
                }else {
                    val last = items[position - 1]
                    last !is NewContactListVO.Identity
                }
                val top = holder.getView<View>(R.id.view_item_contact_person_body_top)
                val topLine = holder.getView<View>(R.id.view_item_contact_person_body_top_line)
                if (isFirst) {
                    top.visible()
                    topLine.gone()
                }else {
                    top.gone()
                    topLine.visible()
                }
                bindIdentity(holder, identity, position)
                holder.convertView?.setOnClickListener { view -> clickIdentity(view, identity) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonRecyclerViewHolder {
        return when(viewType) {
            0 -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_contact_org_body))
            else -> CommonRecyclerViewHolder(parent.inflate(R.layout.item_contact_person_body_new))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return  when(items[position]){
            is NewContactListVO.Department -> 0
            else -> 1
        }
    }


    abstract fun bindDepartment(hold: CommonRecyclerViewHolder?, department: NewContactListVO.Department, position: Int)
    abstract fun clickDepartment(department: NewContactListVO.Department)
    abstract fun bindIdentity(hold: CommonRecyclerViewHolder?, identity: NewContactListVO.Identity, position: Int)
    abstract fun clickIdentity(view:View, identity: NewContactListVO.Identity)
}