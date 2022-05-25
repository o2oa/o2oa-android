package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.zone

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_cloud_disk_zone.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.folder.FolderFileListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CloudFileZoneAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileZoneData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.MiscUtilK
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2oaColorScheme
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport


class ZoneActivity : BaseMVPActivity<ZoneContract.View, ZoneContract.Presenter>(), ZoneContract.View {

    override var mPresenter: ZoneContract.Presenter = ZonePresenter()


    override fun layoutResId(): Int = R.layout.activity_cloud_disk_zone

    var list: ArrayList<CloudFileZoneData> = ArrayList()

    var isZoneCreator = false

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

            override fun clickMyFavoriteMoreBtn(favorite: CloudFileZoneData.MyFavorite) {
                openFavoriteItemMenu(favorite)
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

            override fun clickMyZoneMoreBtn(zone: CloudFileZoneData.MyZone) {
                openZoneItemMenu(zone)
            }
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        if (isZoneCreator) {
            menuInflater.inflate(R.menu.menu_cloud_file_zone_create, menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_zone_create) {
            addZone()
            return true
        }
        return super.onOptionsItemSelected(item)
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
        mPresenter.loadZoneCreatorPermission()
    }

    override fun canCreateZone(flag: Boolean) {
        XLog.info("创建共享区的权限： $flag")
        isZoneCreator = flag
        //刷新菜单按钮
        invalidateOptionsMenu()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun zoneList(list: List<CloudFileZoneData>) {
        srl_cloud_disk_zone.isRefreshing = false
        this.list.clear()
        this.list.addAll(list)
        adapter.notifyDataSetChanged()
    }

    override fun backError(message: String) {
        srl_cloud_disk_zone.isRefreshing = false
        hideLoadingDialog()
        if (!TextUtils.isEmpty(message)) {
            XToast.toastShort(this, message)
        }
    }

    private fun loadNewList() {
        srl_cloud_disk_zone.isRefreshing = true
        mPresenter.loadZone()
    }

    override fun createZoneSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_cloud_create_zone_success))
        loadNewList()
    }

    override fun updateZoneSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_cloud_update_zone_success))
        loadNewList()
    }

    override fun deleteZoneSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_cloud_delete_zone_success))
        loadNewList()
    }

    override fun addFavoriteSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_cloud_add_favorite_success))
        loadNewList()
    }

    override fun renameFavoriteSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_cloud_rename_favorite_success))
        loadNewList()
    }

    override fun cancelFavoriteSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_cloud_cancel_favorite_success))
        loadNewList()
    }

    // 进入到共享区
    private fun openZone(id: String, name: String) {
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name)) {
            XToast.toastShort(this, getString(R.string.message_arg_error))
            return
        }
        FolderFileListActivity.openZone(this, id, name)
    }

    // 打开某个已收藏的共享区的操作菜单
    private fun openFavoriteItemMenu(favorite: CloudFileZoneData.MyFavorite) {
        BottomSheetMenu(this)
            .setItems(arrayListOf(getString(R.string.cloud_file_zone_menu_cancel_favorite), getString(R.string.cloud_file_zone_menu_rename_favorite)), ContextCompat.getColor(this,  R.color.z_color_text_primary)) {
                index -> // 第一个 取消收藏 ， 第二个 重命名
                XLog.debug("选择菜单，$index")
                when(index) {
                    0 -> cancelFavorite(favorite)
                    1 -> renameFavorite(favorite)
                }
            }.setCancelButton(getString(R.string.cancel), ContextCompat.getColor(this, R.color.z_color_text_hint)) {
                XLog.debug("取消。。。。。")
            }
            .show()
    }

    // 打开某个共享区的操作菜单
    private fun openZoneItemMenu(zone: CloudFileZoneData.MyZone) {
        val menus =  if (zone.isAdmin == true) {
            arrayListOf(getString(R.string.cloud_file_zone_menu_add_favorite),
                getString(R.string.cloud_file_zone_menu_edit_zone),
                getString(R.string.cloud_file_zone_menu_delete_zone))
        } else {
            arrayListOf(getString(R.string.cloud_file_zone_menu_add_favorite))
        }
        BottomSheetMenu(this)
            .setItems(menus, ContextCompat.getColor(this,  R.color.z_color_text_primary)) {
                    index -> // 第一个 取消收藏 ， 第二个 重命名
                XLog.debug("选择菜单，$index")
                when(index) {
                    0 -> addToFavorite(zone)
                    1 -> editZoneName(zone)
                    2 -> deleteZone(zone)
                }
            }.setCancelButton(getString(R.string.cancel), ContextCompat.getColor(this, R.color.z_color_text_hint)) {
                XLog.debug("取消。。。。。")
            }
            .show()

    }

    // 取消收藏
    private fun cancelFavorite(favorite: CloudFileZoneData.MyFavorite) {
        showLoadingDialog()
        mPresenter.cancelFavorite(favorite.id)
    }

    // 重命名收藏的共享区
    private fun renameFavorite(favorite: CloudFileZoneData.MyFavorite) {
        val dialog = O2DialogSupport.openCustomViewDialog(this,
            getString(R.string.cloud_file_zone_menu_rename_favorite),
            R.layout.dialog_name_modify) { dialog ->
            val nameEdit = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
            val name = nameEdit.text.toString()
            if (TextUtils.isEmpty(name)) {
                XToast.toastShort(this@ZoneActivity, getString(R.string.message_cloud_not_empty_name_alert))
            } else {
                mPresenter.renameFavorite(name, favorite.id)
            }
        }
        val nameEdit = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
        nameEdit.setText(favorite.name)
    }

    // 添加收藏
    private fun addToFavorite(zone: CloudFileZoneData.MyZone) {
        showLoadingDialog()
        mPresenter.addFavorite(zone.name, zone.id)
    }
    // 编辑共享区
    private fun editZoneName(zone: CloudFileZoneData.MyZone) {
        val dialog = O2DialogSupport.openCustomViewDialog(this,
            getString(R.string.cloud_file_zone_update_title),
            R.layout.dialog_cloud_file_zone_form) { dialog ->
            val nameEdit = dialog.findViewById<EditText>(R.id.et_cloud_file_zone_form_name)
            val name = nameEdit.text.toString()
            val descEdit = dialog.findViewById<EditText>(R.id.et_cloud_file_zone_form_desc)
            if (TextUtils.isEmpty(name)) {
                XToast.toastShort(this@ZoneActivity, getString(R.string.message_cloud_not_empty_name_alert))
            } else {
                mPresenter.updateZone(zone.id, name, descEdit.text.toString())
            }
        }
        val nameEdit = dialog.findViewById<EditText>(R.id.et_cloud_file_zone_form_name)
        nameEdit.setText(zone.name)
        val descEdit = dialog.findViewById<EditText>(R.id.et_cloud_file_zone_form_desc)
        descEdit.setText(zone.description)
    }
    // 删除共享区
    private fun deleteZone(zone: CloudFileZoneData.MyZone) {
        showLoadingDialog()
        mPresenter.deleteZone(zone.id)
    }

    // 新增共享区
    private fun addZone() {
        O2DialogSupport.openCustomViewDialog(this,
            getString(R.string.cloud_file_zone_create_title),
            R.layout.dialog_cloud_file_zone_form) { dialog ->
            val nameEdit = dialog.findViewById<EditText>(R.id.et_cloud_file_zone_form_name)
            val name = nameEdit.text.toString()
            val descEdit = dialog.findViewById<EditText>(R.id.et_cloud_file_zone_form_desc)
            if (TextUtils.isEmpty(name)) {
                XToast.toastShort(this@ZoneActivity, getString(R.string.message_cloud_not_empty_name_alert))
            } else {
                mPresenter.createZone(name, descEdit.text.toString())
            }
        }
    }

}