package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.folder

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_cloud_file_v3_folder_file_list.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.VideoPlayerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.CloudDiskFileDownloadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.picker.CloudDiskFolderPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.type.FileTypeEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer.BigImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.tbs.FileReaderActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.FileBreadcrumbBean
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileV3Data
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FileJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FolderJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.CloudDiskItem
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.MiscUtilK
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2oaColorScheme
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PickTypeMode
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import java.util.HashMap

class FolderFileListActivity : BaseMVPActivity<FolderFileListContract.View, FolderFileListContract.Presenter>(), FolderFileListContract.View {
    override var mPresenter: FolderFileListContract.Presenter = FolderFileListPresenter()


    override fun layoutResId(): Int  = R.layout.activity_cloud_file_v3_folder_file_list



    companion object {
        val ARG_FOLDER_ID_KEY = "ARG_FOLDER_ID_KEY"
        val ARG_FOLDER_NAME_KEY = "ARG_FOLDER_NAME_KEY"
        val LPWW = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        fun openZone(activity: Activity, id: String, name: String) {
            val bundle = Bundle()
            bundle.putString(ARG_FOLDER_ID_KEY, id)
            bundle.putString(ARG_FOLDER_NAME_KEY, name)
            activity.go<FolderFileListActivity>(bundle)
        }

    }
    private val font: Typeface by lazy { Typeface.createFromAsset(assets, "fonts/fontawesome-webfont.ttf") }
    private val breadcrumbBeans = ArrayList<FileBreadcrumbBean>()//面包屑导航对象
    private val adapter: FolderFileItemAdapter by lazy { FolderFileItemAdapter() }
    private var fileLevel = 0//默认进入的时候是第一层
    private val downloader: CloudDiskFileDownloadHelper by lazy { CloudDiskFileDownloadHelper(this) }



    override fun afterSetContentView(savedInstanceState: Bundle?) {
        val firstId = intent.extras?.getString(ARG_FOLDER_ID_KEY)
        val firstName = intent.extras?.getString(ARG_FOLDER_NAME_KEY)
        if (TextUtils.isEmpty(firstId) || TextUtils.isEmpty(firstName)) {
            XToast.toastShort(this, getString(R.string.message_arg_error))
            finish()
            return
        }
        // 导航栏
        setupToolBar(firstName ?: "", setupBackButton = true)
        toolbar?.setNavigationOnClickListener {
            if (!onClickBackBtn()) {
                finish()
            }
        }

        // 企业网盘下载地址不一样
        downloader.isV3 = true

        // 第一层面包屑
        val first = FileBreadcrumbBean()
        first.folderId = firstId
        first.displayName = firstName
        first.level = 0
        fileLevel = 0
        breadcrumbBeans.add(first)

        // 初始化ui
        srl_cloud_disk_v3_folder_file.o2oaColorScheme()
        srl_cloud_disk_v3_folder_file.setOnRefreshListener { refreshView() }
        initRecyclerView()
        initToolbarListener()
        MiscUtilK.swipeRefreshLayoutRun(srl_cloud_disk_v3_folder_file, this)
        refreshView()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!onClickBackBtn()) {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        downloader.closeDownload()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cloud_disk, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.cloud_disk_menu_upload_file -> {
                menuUploadFile()
                return true
            }
            R.id.cloud_disk_menu_create_folder -> {
                menuCreateFolder()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 返回列表数据
    override fun folderFileItemList(list: List<CloudFileV3Data>) {
        srl_cloud_disk_v3_folder_file.isRefreshing = false
        hideLoadingDialog()
        adapter.items.clear()
        adapter.items.addAll(list)
        adapter.clearSelectIds()
        adapter.notifyDataSetChanged()
        refreshToolBar()
    }

    override fun moveToMyPan(isSuccess: Boolean, error: String?) {
        hideLoadingDialog()
        if (isSuccess) {
            XToast.toastShort(this, R.string.message_save_success)
        } else {
            XToast.toastShort(this, error ?: getString(R.string.message_back_error))
        }
    }

    // 重命名结果返回
    override fun updateName(isSuccess: Boolean, error: String?) {
        opBack(isSuccess,  getString(R.string.message_update_success), error)
    }
    // 删除结果返回
    override fun delete(isSuccess: Boolean, error: String?) {
        opBack(isSuccess,  getString(R.string.message_delete_success), error)
    }

    override fun move(isSuccess: Boolean, error: String?) {
        XLog.debug("move 返回： $isSuccess $error")
        opBack(isSuccess, getString(R.string.message_cloud_move_success), error)
    }

    private fun opBack(isSuccess: Boolean, success: String, error: String?) {
        hideLoadingDialog()
        if (isSuccess) {
            XToast.toastShort(this, success)
            refreshView()
        } else {
            XToast.toastShort(this, error ?: getString(R.string.message_back_error))
        }
    }

    override fun uploadFile(isSuccess: Boolean, error: String?) {
        opBack(isSuccess, getString(R.string.message_cloud_upload_success), error)
    }

    override fun createFolder(isSuccess: Boolean, error: String?) {
        opBack(isSuccess, getString(R.string.message_cloud_create_folder_success), error)
    }

    // 点击Android系统返回按钮
    private fun onClickBackBtn(): Boolean {
        if (breadcrumbBeans.size > 1) {
            breadcrumbBeans.removeAt(breadcrumbBeans.size - 1)//删除最后一个
            refreshView()
            return true
        }
        return false
    }

    /**
     * 初始化 列表
     */
    private fun initRecyclerView() {
        rl_cloud_disk_v3_folder_file_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rl_cloud_disk_v3_folder_file_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val topRowVerticalPosition = rl_cloud_disk_v3_folder_file_list?.getChildAt(0)?.top ?: 0
                srl_cloud_disk_v3_folder_file.isEnabled = topRowVerticalPosition >= 0
            }
        })
        rl_cloud_disk_v3_folder_file_list.adapter = adapter
        adapter.onItemClickListener = object : FolderFileItemAdapter.OnItemClickListener {

            override fun onFolderClick(folder: CloudFileV3Data.FolderItem) {
                //进入下一层
                XLog.debug("点击文件夹：" + folder.name)
                val newLevel = fileLevel + 1
                val newBean = FileBreadcrumbBean()
                newBean.displayName = folder.name
                newBean.folderId = folder.id
                newBean.level = newLevel
                breadcrumbBeans.add(newBean)
                refreshView()
            }

            override fun onFileClick(file: CloudFileV3Data.FileItem) {
                if (file.type == FileTypeEnum.image.key) {
                    openImage(file)
                }else {
                    openFile(file)
                }
            }

        }
        adapter.onCheckChangeListener = object : FolderFileItemAdapter.OnCheckChangeListener {
            override fun onChange() {
                refreshToolBar()
            }
        }
    }

    // 刷新
    private fun refreshView() {
        srl_cloud_disk_v3_folder_file.isRefreshing = true
        val current = breadcrumbBeans.last()
        loadFileList(current.folderId, current.level)
        loadBreadcrumb()
    }


    // 获取文件列表
    private fun loadFileList(parentId: String, newLevel: Int) {
        showLoadingDialog()
        fileLevel = newLevel
        mPresenter.getFolderFileItemList(parentId)
    }

    /**
     * 加载面包屑导航
     */
    private fun loadBreadcrumb() {
        ll_cloud_disk_v3_folder_file_breadcrumb.removeAllViews()
        breadcrumbBeans.mapIndexed { index, fileBreadcrumbBean ->
            val breadcrumbTitle = TextView(this)
            breadcrumbTitle.text = fileBreadcrumbBean.displayName
            breadcrumbTitle.tag = fileBreadcrumbBean
            breadcrumbTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            breadcrumbTitle.layoutParams = LPWW
            if (index == breadcrumbBeans.size - 1) {
                breadcrumbTitle.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
                ll_cloud_disk_v3_folder_file_breadcrumb.addView(breadcrumbTitle)
            } else {
                breadcrumbTitle.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary_dark))
                breadcrumbTitle.setOnClickListener { v -> onClickBreadcrumb(v as TextView) }
                ll_cloud_disk_v3_folder_file_breadcrumb.addView(breadcrumbTitle)
                val arrow = TextView(this)
                val lp = LPWW
                lp.setMargins(8, 0, 8, 0)
                arrow.layoutParams = lp
                arrow.text = getString(R.string.fa_angle_right)
                arrow.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary_dark))
                arrow.typeface = font
                arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                ll_cloud_disk_v3_folder_file_breadcrumb.addView(arrow)
            }
        }
    }


    /**
     * 点击面包屑导航
     */
    private fun onClickBreadcrumb(textView: TextView) {
        val bean = textView.tag as FileBreadcrumbBean
        var newLevel = 0
        breadcrumbBeans.mapIndexed { index, fileBreadcrumbBean ->
            if (bean == fileBreadcrumbBean) {
                newLevel = index
            }
        }
        //处理breadcrumbBeans 把多余的去掉
        if (breadcrumbBeans.size > (newLevel + 1)) {
            val s = breadcrumbBeans.size
            for (i in (s-1) downTo (newLevel+1)) {
                breadcrumbBeans.removeAt(i)
            }
        }
        refreshView()
    }


    /**
     * 工具栏点击事件
     */
    private fun initToolbarListener() {
        btn_cloud_disk_v3_rename.setOnClickListener {
            XLog.debug("click rename button ")
            if (adapter.mSelectIds.size == 1) {
                renameFile()
            } else {
                XToast.toastShort(this, R.string.message_cloud_rename_alert)
            }
        }
        btn_cloud_disk_v3_delete.setOnClickListener {
            XLog.debug("click delete button ")
            if (adapter.mSelectIds.isNotEmpty()) {
                delete()
            } else {
                XToast.toastShort(this, R.string.message_cloud_delete_alert)
            }
        }
        btn_cloud_disk_v3_save_to_my.setOnClickListener {
            XLog.debug("click save to my pan button ")
            if (adapter.mSelectIds.isNotEmpty()) {
                saveToMyPan()
            } else {
                XToast.toastShort(this, R.string.message_cloud_save_to_my_pan_alert)
            }
        }
        btn_cloud_disk_v3_move.setOnClickListener {
            XLog.debug("click move button ")
            if (adapter.mSelectIds.isNotEmpty()) {
                move()
            } else {
                XToast.toastShort(this, R.string.message_cloud_move_alert)
            }
        }
    }
    /**
     * 刷新 底部工具栏
     */
    private fun refreshToolBar() {
        if (adapter.mSelectIds.isEmpty()) {
            ll_cloud_disk_v3_folder_file_toolbar.gone()
        }else {
            ll_cloud_disk_v3_folder_file_toolbar.visible()
            if (adapter.mSelectIds.size > 1) {
//                btn_cloud_disk_v3_rename.isEnabled = false
                btn_cloud_disk_v3_rename.gone()
                btn_cloud_disk_v3_delete.gone()
            }else {
                btn_cloud_disk_v3_rename.visible()
                val renameId = adapter.mSelectIds.first()
                when (val item = adapter.items.firstOrNull { it.id == renameId }) {
                    is CloudFileV3Data.FileItem -> if (item.isAdmin ||  item.isCreator) { btn_cloud_disk_v3_delete.visible() } else { btn_cloud_disk_v3_delete.gone() }
                    is CloudFileV3Data.FolderItem -> if (item.isAdmin ||  item.isCreator) { btn_cloud_disk_v3_delete.visible() } else { btn_cloud_disk_v3_delete.gone() }
                    else -> btn_cloud_disk_v3_delete.gone()
                }
            }
//            btn_cloud_disk_v3_save_to_my.isEnabled = true
//            btn_cloud_disk_v3_move.isEnabled = true
        }
    }



    private fun menuUploadFile() {
        PicturePickUtil().withAction(this)
            .setMode(PickTypeMode.FileWithMedia)
            .allowMultiple(true)
            .forResult { files ->
                if (files != null && files.isNotEmpty()) {
                    uploadFile(files)
                }
            }
    }
    private fun uploadFile(filePaths: List<String>) {
        try {
            val bean = breadcrumbBeans.last()//最后一个
            showLoadingDialog()
            mPresenter.uploadFileList(bean.folderId, filePaths)
        } catch (e: Exception) {
            XLog.error("", e)
            XToast.toastShort(this, R.string.message_cloud_upload_fail)
        }
    }
    /**
     * 新建文件夹
     */
    private fun menuCreateFolder() {
        O2DialogSupport.openCustomViewDialog(this, getString(R.string.yunpan_menu_create_folder), R.layout.dialog_name_modify) {
                dialog ->
            val text = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
            val content = text.text.toString()
            if (TextUtils.isEmpty(content)) {
                XToast.toastShort(this, R.string.message_cloud_folder_name_not_empty)
            } else {
                createFolderOnLine(content)
                dialog.dismiss()
            }
        }
    }
    private fun createFolderOnLine(folderName: String) {
        try {
            val bean = breadcrumbBeans.last()//最后一个
            showLoadingDialog()
            mPresenter.createFolder(bean.folderId, folderName)
        } catch (e: Exception) {
            XLog.error("", e)
            XToast.toastShort(this, R.string.message_cloud_create_folder_fail)
        }
    }

    // 重命名文件或文件夹
    private fun renameFile() {
        val renameId = adapter.mSelectIds.first()
        val item = adapter.items.firstOrNull { it.id == renameId }
        if (item != null) {
            val dialog = O2DialogSupport.openCustomViewDialog(this, getString(R.string.yunpan_rename), R.layout.dialog_name_modify) {
                    dialog ->
                val text = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
                val content = text.text.toString()
                if (TextUtils.isEmpty(content)) {
                    XToast.toastShort(this, R.string.message_cloud_not_empty_name_alert)
                } else {
                    showLoadingDialog()
                    if (item is CloudFileV3Data.FolderItem) {
                        mPresenter.updateFolderName(item.id, content)
                    } else if (item is CloudFileV3Data.FileItem) {
                        mPresenter.updateFileName(item.id, content)
                    }
                    dialog.dismiss()

                }
            }
            val text = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
            text.setText(item.name)
        }
    }
    // 删除文件或文件夹
    private fun delete() {
        O2DialogSupport.openConfirmDialog(this, getString(R.string.message_cloud_confirm_delete_data), { dialog ->
            val renameId = adapter.mSelectIds.first()
            val item = adapter.items.firstOrNull { it.id == renameId }
            if (item != null) {
                showLoadingDialog()
                mPresenter.deleteFolderOrFile(item)
            }
        })
    }
    // 移动文件
    private fun move() {
        //共享区的文件只能在当前共享区内移动
        val zone = breadcrumbBeans[0]
        CloudDiskFolderPickerActivity.pickFolderV3(this, zone.folderId, zone.displayName) { parentId ->
            val files = ArrayList<CloudFileV3Data.FileItem>()
            val folders = ArrayList<CloudFileV3Data.FolderItem>()
            adapter.mSelectIds.forEach { id ->
                val item = adapter.items.firstOrNull { id == it.id }
                if (item != null) {
                    when(item) {
                        is CloudFileV3Data.FileItem -> files.add(item)
                        is CloudFileV3Data.FolderItem -> folders.add(item)
                    }
                }
            }
            showLoadingDialog()
            val pId = if (TextUtils.isEmpty(parentId)) { // 返回空就是移动到顶层
                XLog.debug("移动到顶层")
                zone.folderId
            } else {
                parentId
            }
            XLog.debug("pId: $pId , files: ${files.size} folder: ${folders.size}")
            mPresenter.move(pId, files, folders)
        }
    }
    // 保存到我的网盘
    private fun saveToMyPan() {
        //使用个人网盘选择文件夹，把这边企业网盘的文件保存到个人网盘
        CloudDiskFolderPickerActivity.pickFolder(this) { parentId ->
            val files = ArrayList<CloudFileV3Data.FileItem>()
            val folders = ArrayList<CloudFileV3Data.FolderItem>()
            adapter.mSelectIds.forEach { id ->
                val item = adapter.items.firstOrNull { id == it.id }
                if (item != null) {
                    when(item) {
                        is CloudFileV3Data.FileItem -> files.add(item)
                        is CloudFileV3Data.FolderItem -> folders.add(item)
                    }
                }
            }
            val pId = if (TextUtils.isEmpty(parentId)) {
                O2.FIRST_PAGE_TAG // 移动到顶层 个人网盘顶层用特殊符号 (0)
            } else {
                parentId
            }
            showLoadingDialog()
            mPresenter.moveToMyPan(pId, files, folders)
        }
    }

    private fun openFile(file: CloudFileV3Data.FileItem) {
        downloader.showLoading = { showLoadingDialog() }
        downloader.hideLoading = { hideLoadingDialog() }
        downloader.startDownload(file.id, file.extension) { f->
            if (f != null) {
                if (file.type == FileTypeEnum.movie.key) {
                    VideoPlayerActivity.startPlay(this, f.absolutePath, file.name)
                }else {
                    go<FileReaderActivity>(FileReaderActivity.startBundle(f.absolutePath))
                }
            }else {
                XToast.toastShort(this, R.string.message_cloud_open_file_fail)
            }
        }
    }

    private fun openImage(file: CloudFileV3Data.FileItem) {
        BigImageViewActivity.startForV3(this, file.id, file.extension, file.name)
    }

}