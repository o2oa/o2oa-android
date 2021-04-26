package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_device_manager.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.CollectDeviceData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.DividerItemDecoration
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport

class DeviceManagerActivity : BaseMVPActivity<DeviceManagerContract.View, DeviceManagerContract.Presenter>(), DeviceManagerContract.View {

    override var mPresenter: DeviceManagerContract.Presenter = DeviceManagerPresenter()


    override fun layoutResId(): Int = R.layout.activity_device_manager

    private val deviceToken: String by lazy {  O2SDKManager.instance().prefs().getString(O2.PRE_BIND_PHONE_TOKEN_KEY, "") ?: "" }

    private val list: ArrayList<CollectDeviceData> = ArrayList()
    private val adapter: CommonRecycleViewAdapter<CollectDeviceData> by lazy {
        object : CommonRecycleViewAdapter<CollectDeviceData>(this, list, R.layout.item_device_unbind) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: CollectDeviceData?) {
                val textView = holder?.getView<TextView>(R.id.tv_item_device_title)
                val deviceTitle = if (TextUtils.isEmpty(t?.deviceType)) {
                    getString(R.string.setting_unknown_device)
                }else {
                    getString(R.string.setting_device_type, t?.deviceType?:"")
//                    "${t?.deviceType} 设备"
                }
                textView?.text = deviceTitle

                val btn = holder?.getView<TextView>(R.id.tv_item_device_unbind_btn)
                if (deviceToken == t?.name) { //当前设备
                    btn?.text = getString(R.string.setting_device_self)
                    btn?.setTextColor(ContextCompat.getColor(this@DeviceManagerActivity, R.color.z_color_accent))
                }else {
                    btn?.text = getString(R.string.setting_unbind_device)
                    btn?.setTextColor(ContextCompat.getColor(this@DeviceManagerActivity, R.color.icon_blue))
                }
            }

        }
    }


    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar(getString(R.string.setting_device_manager), true)
        adapter.setOnItemClickListener { _, position ->
            XLog.debug("点击了第 $position 行！")
            val data = list[position]
            if (deviceToken != data.name) {
                val deviceTitle = if (TextUtils.isEmpty(data.deviceType)) {
                    getString(R.string.setting_unknown_device)
                }else {
//                    "${data.deviceType} 设备"
                    getString(R.string.setting_device_type, data.deviceType?:"")
                }
                val msg = getString(R.string.message_setting_unbind, deviceTitle)
                O2DialogSupport.openConfirmDialog(this, msg, {
                    mPresenter.unbind(data.name)
                })
            }

        }
        rv_device_manager_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_device_manager_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
        rv_device_manager_list.adapter = adapter

        mPresenter.listDevice()
    }

    override fun unbindBack(flag: Boolean, message: String) {
        if (!flag) {
            XToast.toastShort(this, message)
        }else {
            mPresenter.listDevice()
        }
    }

    override fun list(list: List<CollectDeviceData>) {
        this.list.clear()
        this.list.addAll(list)
        this.adapter.notifyDataSetChanged()
    }

}
