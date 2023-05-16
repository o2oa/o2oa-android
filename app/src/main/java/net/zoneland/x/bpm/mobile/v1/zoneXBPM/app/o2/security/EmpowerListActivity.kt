package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_empower_list.linear_my_empower_to_button
import kotlinx.android.synthetic.main.activity_empower_list.ll_my_empower_button
import kotlinx.android.synthetic.main.activity_empower_list.rv_empower_list
import kotlinx.android.synthetic.main.activity_empower_list.tv_my_empower
import kotlinx.android.synthetic.main.activity_empower_list.tv_my_empower_to
import kotlinx.android.synthetic.main.activity_empower_list.view_my_empower_divider
import kotlinx.android.synthetic.main.activity_empower_list.view_my_empower_to_divider
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.EmpowerData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.DividerItemDecoration

class EmpowerListActivity : BaseMVPActivity<EmpowerListContract.View, EmpowerListContract.Presenter>(), EmpowerListContract.View {

    enum class EmpowerType{
        MyEmpower,
        EmPowerToMe
    }

    private var mode :EmpowerType = EmpowerType.MyEmpower

    override var mPresenter: EmpowerListContract.Presenter = EmpowerListPresenter()

    private val empowerList: ArrayList<EmpowerData> = ArrayList()
    private val adapter: CommonRecycleViewAdapter<EmpowerData> by lazy {
        object : CommonRecycleViewAdapter<EmpowerData>(this, empowerList, R.layout.item_empower_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: EmpowerData?) {
                var person = when(mode) {
                    EmpowerType.MyEmpower -> {
                        t?.toPerson ?: ""
                    }

                    EmpowerType.EmPowerToMe -> {
                        t?.fromPerson ?: ""
                    }
                }
                if (person.contains("@")) {
                    person = person.split("@").first()
                }
                var type = t?.type ?: ""
                when(type){
                    "all" -> type = "全部"
                    "application" -> type = "应用【${t?.applicationName}】"
                    "process" -> type = "流程【${t?.processName}】"
                }
                holder?.setText(R.id.tv_item_empower_person, person)
                    ?.setText(R.id.tv_item_empower_type, type)
                    ?.setText(R.id.tv_item_empower_time, "${t?.startTime} - ${t?.completedTime}")
            }
        }
    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar(getString(R.string.title_activity_empower), setupBackButton = true)
        rv_empower_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_empower_list.adapter =  adapter
        rv_empower_list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        ll_my_empower_button.setOnClickListener {
            changeMode(EmpowerType.MyEmpower)
        }
        linear_my_empower_to_button.setOnClickListener {
            changeMode(EmpowerType.EmPowerToMe)
        }
        changeMode(EmpowerType.MyEmpower)
    }


    override fun layoutResId(): Int = R.layout.activity_empower_list

    override fun myEmpowerList(myList: List<EmpowerData>) {
        hideLoadingDialog()
        empowerList.clear()
        empowerList.addAll(myList)
        adapter.notifyDataSetChanged()
    }

    override fun myEmpowerListTo(myListTo: List<EmpowerData>) {
        hideLoadingDialog()
        empowerList.clear()
        empowerList.addAll(myListTo)
        adapter.notifyDataSetChanged()
    }

    override fun error(errorMsg: String) {
        hideLoadingDialog()
        XToast.toastShort(errorMsg)
    }

    private fun changeMode(newMode: EmpowerType) {
        mode = newMode
        when(mode) {
            EmpowerType.MyEmpower -> {
                tv_my_empower.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
                view_my_empower_divider.visible()
                tv_my_empower_to.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary))
                view_my_empower_to_divider.gone()
            }
            EmpowerType.EmPowerToMe -> {
                tv_my_empower.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary))
                view_my_empower_divider.gone()
                tv_my_empower_to.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
                view_my_empower_to_divider.visible()
            }
        }
        loadData()
    }

    private fun loadData() {
        showLoadingDialog()
        when(mode) {
            EmpowerType.MyEmpower -> mPresenter.myEmpowerList()
            EmpowerType.EmPowerToMe -> mPresenter.myEmpowerListTo()
        }
    }
}