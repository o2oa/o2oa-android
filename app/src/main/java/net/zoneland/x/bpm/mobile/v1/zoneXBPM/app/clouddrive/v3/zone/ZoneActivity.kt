package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.zone

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_cloud_disk_zone.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.folder.FolderFileListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CloudFileZoneAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileZoneData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.MiscUtilK
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2oaColorScheme


class ZoneActivity : BaseMVPActivity<ZoneContract.View, ZoneContract.Presenter>(), ZoneContract.View {

    override var mPresenter: ZoneContract.Presenter = ZonePresenter()


    override fun layoutResId(): Int = R.layout.activity_cloud_disk_zone

    var list: ArrayList<CloudFileZoneData> = ArrayList()


    val adapter: CloudFileZoneAdapter by lazy {
        object: CloudFileZoneAdapter(list) {
            override fun bindMyFavorite(
                favorite: CloudFileZoneData.MyFavorite,
                holder: CommonRecyclerViewHolder?
            ) {
                holder?.setText(R.id.tv_item_cloud_file_favorite_name, favorite.name)
            }

            override fun clickMyFavorite(favorite: CloudFileZoneData.MyFavorite) {
                openZone(favorite.zoneId, favorite.name)
            }

            override fun bindMyZone(
                zone: CloudFileZoneData.MyZone,
                holder: CommonRecyclerViewHolder?
            ) {
                holder?.setText(R.id.tv_item_cloud_file_zone_name, zone.name)
            }

            override fun clickMyZone(zone: CloudFileZoneData.MyZone) {
                openZone(zone.zoneId, zone.name)
            }
        }
    }


    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar(getString(R.string.cloud_file_org), setupBackButton = true)
        srl_cloud_disk_zone.o2oaColorScheme()
        srl_cloud_disk_zone.setOnRefreshListener {
            mPresenter.loadZone()
        }
        MiscUtilK.swipeRefreshLayoutRun(srl_cloud_disk_zone, this)
        rl_cloud_file_zone_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rl_cloud_file_zone_list.adapter = adapter
        mPresenter.loadZone()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun zoneList(list: List<CloudFileZoneData>) {
        srl_cloud_disk_zone.isRefreshing = false
        this.list.clear()
        this.list.addAll(list)
        adapter.notifyDataSetChanged()
    }

    private fun openZone(id: String, name: String) {
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name)) {
            XToast.toastShort(this, getString(R.string.message_arg_error))
            return
        }
        FolderFileListActivity.openZone(this, id, name)
    }
}