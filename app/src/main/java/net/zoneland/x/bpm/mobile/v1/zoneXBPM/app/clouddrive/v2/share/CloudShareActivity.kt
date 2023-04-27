package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.share

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_cloud_share.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.VideoPlayerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.CloudDiskShareFileDownloadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.f.CloudDiskItemAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.f.FileFolderListFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer.BigImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.FileBreadcrumbBean
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.CloudDiskItem
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible


class CloudShareActivity : BaseMVPActivity<CloudShareContract.View, CloudShareContract.Presenter>(), CloudShareContract.View {
    override var mPresenter: CloudShareContract.Presenter = CloudSharePresenter()

    override fun layoutResId(): Int = R.layout.activity_cloud_share

    companion object {
        val share_type_key = "share_type_key" // shareToMe | myShare

        fun openMyShare(): Bundle {
            val bundle = Bundle()
            bundle.putString(share_type_key, "myShare")
            return bundle
        }

        fun openShareToMe(): Bundle {
            val bundle = Bundle()
            bundle.putString(share_type_key, "shareToMe")
            return bundle
        }
    }

    // 当前页面是我的分享 还是 别人分享给我的
    private var shareType: ShareTypeEnum = ShareTypeEnum.ShareToMe
    private val font: Typeface by lazy { Typeface.createFromAsset(assets, "fonts/fontawesome-webfont.ttf") }
    private val breadcrumbBeans = ArrayList<FileBreadcrumbBean>()//面包屑导航对象
    private val adapter: CloudDiskItemAdapter by lazy { CloudDiskItemAdapter() }
    private var fileLevel = 0//默认进入的时候是第一层
    private var shareId = "" // 当前的分享对象id

    private val downloader: CloudDiskShareFileDownloadHelper by lazy { CloudDiskShareFileDownloadHelper(this) }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        val type = intent?.extras?.getString(share_type_key) ?: "shareToMe"
        shareType = if (type == "shareToMe") {
            ShareTypeEnum.ShareToMe
        }else {
            ShareTypeEnum.MyShare
        }
        val title = (if(shareType == ShareTypeEnum.MyShare) "我的分享" else "分享给我的")
        setupToolBar(title, true, isCloseBackIcon = true)

        //顶部面包屑菜单
        if (breadcrumbBeans.isEmpty()) {
            val top = FileBreadcrumbBean()
            top.displayName = getString(R.string.yunpan_all_file)
            top.folderId = ""
            top.level = 0
            breadcrumbBeans.add(top)
        }
        swipe_refresh_cloud_share_layout.setColorSchemeResources(R.color.z_color_refresh_scuba_blue,
                R.color.z_color_refresh_red, R.color.z_color_refresh_purple, R.color.z_color_refresh_orange)
        swipe_refresh_cloud_share_layout.setOnRefreshListener { refreshView() }

        initRecyclerView()

        initToolbarListener()

        MiscUtilK.swipeRefreshLayoutRun(swipe_refresh_cloud_share_layout, this)
        refreshView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (shareType != ShareTypeEnum.MyShare) {
            menuInflater.inflate(R.menu.menu_cloud_share, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_cloud_my_share) {
            go<CloudShareActivity>(openMyShare())
            return true
        }
        return super.onOptionsItemSelected(item)
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

    private fun onClickBackBtn(): Boolean {
        if (breadcrumbBeans.size > 1) {
            breadcrumbBeans.removeAt(breadcrumbBeans.size - 1)//删除最后一个
            refreshView()
            return true
        }
        return false
    }

    override fun itemList(list: List<CloudDiskItem>) {
        swipe_refresh_cloud_share_layout.isRefreshing = false
        adapter.items.clear()
        adapter.items.addAll(list)
        adapter.clearSelectIds()
        adapter.notifyDataSetChanged()
        refreshToolBar()
    }

    override fun error(error: String) {
        XToast.toastShort(this, error)
        swipe_refresh_cloud_share_layout.isRefreshing = false
        hideLoadingDialog()
    }

    override fun success() {
        hideLoadingDialog()
        refreshView()
    }

    private fun refreshView() {
        swipe_refresh_cloud_share_layout.isRefreshing = true
        val current = breadcrumbBeans.last()
        if (breadcrumbBeans.size == 1) {
            shareId = ""
        }
        loadList(current.folderId, current.level)
        loadBreadcrumb()
    }

    private fun initRecyclerView() {
        rv_cloud_share_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_cloud_share_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val topRowVerticalPosition = rv_cloud_share_list?.getChildAt(0)?.top ?: 0
                swipe_refresh_cloud_share_layout.isEnabled = topRowVerticalPosition >= 0
            }
        })
        rv_cloud_share_list.adapter = adapter
        adapter.onItemClickListener = object : CloudDiskItemAdapter.OnItemClickListener {

            override fun onShareClick(share: CloudDiskItem.ShareItem) {
                if (share.fileType == "folder") {
                    //进入下一层
                    XLog.debug("点击文件夹：" + share.name)
                    val newLevel = fileLevel + 1
                    val newBean = FileBreadcrumbBean()
                    newBean.displayName = share.name
                    newBean.folderId = share.fileId
                    newBean.level = newLevel
                    breadcrumbBeans.add(newBean)
                    shareId = share.id
                    refreshView()
                }else {
                    openFile(share.id, share.fileId, share.extension, share.name)
                }
            }

            override fun onFolderClick(folder: CloudDiskItem.FolderItem) {
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

            override fun onFileClick(file: CloudDiskItem.FileItem) {
                openFile(shareId, file.id, file.extension, file.name)
            }
        }
        adapter.onCheckChangeListener = object : CloudDiskItemAdapter.OnCheckChangeListener {
            override fun onChange() {
                refreshToolBar()
            }
        }
    }

    private fun initToolbarListener() {
        btn_cloud_share_cancel.setOnClickListener {
            val shareIds = ArrayList<String>()
            adapter.mSelectIds.forEach { id ->
                shareIds.add(id)
            }
            showLoadingDialog()
            mPresenter.deleteMyShare(shareIds)
        }
        btn_cloud_share_shield.setOnClickListener {
            val shareIds = ArrayList<String>()
            adapter.mSelectIds.forEach { id ->
                shareIds.add(id)
            }
            showLoadingDialog()
            mPresenter.shieldShare(shareIds)
        }
    }

    /**
     * 刷新 底部工具栏
     */
    private fun refreshToolBar() {
        if (adapter.mSelectIds.isEmpty()) {
            ll_cloud_share_toolbar.gone()
        }else {
            ll_cloud_share_toolbar.visible()
            when (shareType) {
                ShareTypeEnum.ShareToMe -> {
                    btn_cloud_share_shield.visible()
                    btn_cloud_share_cancel.gone()
                }
                ShareTypeEnum.MyShare -> {
                    btn_cloud_share_cancel.visible()
                    btn_cloud_share_shield.gone()
                }
            }
        }
    }
    /**
     * 加载面包屑导航
     */
    private fun loadBreadcrumb() {
        ll_cloud_share_breadcrumb.removeAllViews()
        breadcrumbBeans.mapIndexed { index, fileBreadcrumbBean ->
            val breadcrumbTitle = TextView(this)
            breadcrumbTitle.text = fileBreadcrumbBean.displayName
            breadcrumbTitle.tag = fileBreadcrumbBean
            breadcrumbTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            breadcrumbTitle.layoutParams = FileFolderListFragment.LPWW
            if (index == breadcrumbBeans.size - 1) {
                breadcrumbTitle.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
                ll_cloud_share_breadcrumb.addView(breadcrumbTitle)
            } else {
                breadcrumbTitle.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary_dark))
                breadcrumbTitle.setOnClickListener { v -> onClickBreadcrumb(v as TextView) }
                ll_cloud_share_breadcrumb.addView(breadcrumbTitle)
                val arrow = TextView(this)
                val lp = FileFolderListFragment.LPWW
                lp.setMargins(8, 0, 8, 0)
                arrow.layoutParams = lp
                arrow.text = getString(R.string.fa_angle_right)
                arrow.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary_dark))
                arrow.typeface = font
                arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                ll_cloud_share_breadcrumb.addView(arrow)
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



    private fun loadList(id: String, newLevel: Int) {
        fileLevel = newLevel
        when(shareType) {
            ShareTypeEnum.MyShare -> {
                mPresenter.getMyShareItemList(id, shareId)
            }
            ShareTypeEnum.ShareToMe -> {
                mPresenter.getShareToMeItemList(id, shareId)
            }
        }
    }

    private fun openFile(shareId: String , id: String, extension: String, name: String) {
        downloader.showLoading = { showLoadingDialog() }
        downloader.hideLoading = { hideLoadingDialog() }
        downloader.startDownload(shareId, id, extension) { file->
            if (file!=null) {
                when {
                    FileExtensionHelper.isVideoFromExtension(extension) -> {
                        VideoPlayerActivity.startPlay(this, file.absolutePath, name)
                    }
                    FileExtensionHelper.isImageFromFileExtension(extension) -> {
                        BigImageViewActivity.startLocalFile(this, file.absolutePath)
                    }
                    else -> {
                        AndroidUtils.openFileWithDefaultApp(this, file)
                    }
                }
            }else {
                XToast.toastShort(this, "打开文件异常！")
            }
        }
    }

}