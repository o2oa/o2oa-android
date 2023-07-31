package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_index_portal.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPViewPagerFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.calendar.CalendarMainActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer.BigImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.cms.application.CMSApplicationActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.cms.application.CMSPublishDocumentActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.cms.view.CMSWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.meeting.main.MeetingMainActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.ReadCompletedListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.ReadListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.TaskCompletedListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.TaskListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSAPPConfig
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSApplicationInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSCategoryInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSDocumentInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.StringUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing.activity.CaptureActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.O2WebviewDownloadListener
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.WebChromeClientWithProgressAndValueCallback
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport

/**
 * Created by fancyLou on 21/03/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


class IndexPortalFragment :
    BaseMVPViewPagerFragment<IndexPortalContract.View, IndexPortalContract.Presenter>(),
    IndexPortalContract.View {
    override var mPresenter: IndexPortalContract.Presenter = IndexPortalPresenter()

    override fun layoutResId(): Int = R.layout.fragment_index_portal

    companion object {
        const val PORTAL_ID_KEY = "PORTAL_ID_KEY"
        const val PORTAL_PAGE_ID_KEY = "PORTAL_PAGE_ID_KEY"
        fun instance(portalId: String, pageId: String? = null): IndexPortalFragment {
            val instance = IndexPortalFragment()
            val args = Bundle()
            args.putString(PORTAL_ID_KEY, portalId)
            if (!TextUtils.isEmpty(pageId)) {
                args.putString(PORTAL_PAGE_ID_KEY, pageId)
            }
            instance.arguments = args
            return instance
        }
    }

    private val webChromeClient: WebChromeClientWithProgressAndValueCallback
            by lazy { WebChromeClientWithProgressAndValueCallback.with(this) }
    private val jsNotification: JSInterfaceO2mNotification by lazy {
        JSInterfaceO2mNotification.with(
            this
        )
    }
    private val jsUtil: JSInterfaceO2mUtil by lazy { JSInterfaceO2mUtil.with(this) }
    private val jsBiz: JSInterfaceO2mBiz by lazy { JSInterfaceO2mBiz.with(this) }

    private var portalId: String = ""
    private var pageId: String = ""
    private var portalUrl: String = ""
    override fun initUI() {
        portalId = arguments?.getString(PORTAL_ID_KEY) ?: ""
        pageId = arguments?.getString(PORTAL_PAGE_ID_KEY) ?: ""
        if (TextUtils.isEmpty(portalId)) {
            XToast.toastShort(activity, getString(R.string.message_portal_need_id))
            web_view_portal_content.loadData(
                getString(R.string.message_portal_need_id),
                "text/plain",
                "UTF-8"
            )
        } else {
            portalUrl = APIAddressHelper.instance().getPortalWebViewUrl(portalId, pageId)
            XLog.debug("portal url : $portalUrl")
            web_view_portal_content.addJavascriptInterface(this, "o2android") //注册js对象
            jsNotification.setupWebView(web_view_portal_content)
            jsUtil.setupWebView(web_view_portal_content)
            jsBiz.setupWebView(web_view_portal_content)
            web_view_portal_content.addJavascriptInterface(
                jsNotification,
                JSInterfaceO2mNotification.JSInterfaceName
            )
            web_view_portal_content.addJavascriptInterface(
                jsUtil,
                JSInterfaceO2mUtil.JSInterfaceName
            )
            web_view_portal_content.addJavascriptInterface(jsBiz, JSInterfaceO2mBiz.JSInterfaceName)
            web_view_portal_content.webViewSetCookie(activity!!, portalUrl)
            web_view_portal_content.setDownloadListener(O2WebviewDownloadListener(activity!!))
            web_view_portal_content.webChromeClient = webChromeClient
            // 设置标题
            webChromeClient.onO2ReceivedTitle = { title ->
                if (activity != null && activity is PortalWebViewActivity) {
                    (activity as PortalWebViewActivity).setWebViewTitle(title)
                }
            }
            web_view_portal_content.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (view != null) {
                        imgReset(view) // 处理图片过大的问题
                        XLog.debug("处理了大图了？？？？？？？？？？？？")
                    }
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    XLog.error("ssl error, $error")
                    handler?.proceed()
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    XLog.debug("跳转 url : $url")
                    if (!TextUtils.isEmpty(url)) {
                        if (StringUtil.isImgUrl(url)) {
                            BigImageViewActivity.startInternetImageUrl(activity!!, url!!)
                        } else {
                            view?.loadUrl(url!!)
                        }
                    }
                    return true
                }

            }
            loadWebview()
        }

    }

    fun loadWebview() {
        web_view_portal_content.loadUrl(portalUrl)
    }

    fun windowReload() {
        if (web_view_portal_content != null) {
            web_view_portal_content.evaluateJavascript("window.location.reload()") { value ->
                XLog.info("执行windowReload ， result: $value")
            }
        }
    }

    /**
     * 是否能返回
     */
    fun previousPage(): Boolean {
        return if (web_view_portal_content?.canGoBack() == true) {
            web_view_portal_content.goBack()
            true
        } else {
            false
        }
    }

    override fun lazyLoad() {
        //页面显示的时候调用一个js方法 这个方法可以用来刷新数据之类的
        windowReload()
    }

//    override fun loadCmsCategoryListByAppId(categoryList: List<CMSCategoryInfoJson>) {
//        hideLoadingDialog()
//        if (categoryList.isNotEmpty()) {
//            val app = CMSApplicationInfoJson()
//            app.appName = categoryList.first().appName
//            app.id = categoryList.first().appId
//            app.wrapOutCategoryList = categoryList
//            activity?.go<CMSApplicationActivity>(CMSApplicationActivity.startBundleData(app))
//        } else {
//            XLog.error("该应用无法打开 没有分类数据。。。。。")
//        }
//    }

    override fun cmsApplication(app: CMSApplicationInfoJson?) {
        hideLoadingDialog()
        cmsApp = app
        if (cmsApp != null && cmsStatus == "1") {
            val categoryList = cmsApp!!.wrapOutCategoryList
            if (categoryList.isEmpty()) {
                XToast.toastShort(activity, "当前栏目没有分类信息，无法创建文档！")
                return
            }
            if (cmsOptions != null && !TextUtils.isEmpty(cmsOptions!!["category"])) {
                cmsPublishCategory =
                    categoryList.firstOrNull { it.id == cmsOptions!!["category"] || it.categoryName == cmsOptions!!["category"] }
                if (cmsPublishCategory != null) {
                    mPresenter.findDocumentDraftWithCategory(cmsPublishCategory!!.id)
                }
            } else {
                showPublishCategoriesList(categoryList)
            }
        }
    }

    override fun openCmsApplication(app: CMSApplicationInfoJson?) {
        hideLoadingDialog()
        if (app != null) {
            activity?.go<CMSApplicationActivity>(CMSApplicationActivity.startBundleData(app))
        } else {
            XToast.toastShort(activity, "没有查询到对应的应用！")
        }
    }

    private fun showPublishCategoriesList(canPublishCategories: List<CMSCategoryInfoJson>) {
        //处理绑定流程的那些发布
        val items = canPublishCategories.map { it.categoryName }
        if (activity != null) {
            BottomSheetMenu(activity!!)
                .setTitle("选择发布的分类")
                .setItems(
                    items,
                    ContextCompat.getColor(activity!!, R.color.z_color_primary)
                ) { index ->
                    XLog.info("选择了$index 分类")
                    cmsPublishCategory = canPublishCategories[index]
                    mPresenter.findDocumentDraftWithCategory(canPublishCategories[index].id)
                }.setCancelButton(
                    "取消",
                    ContextCompat.getColor(activity!!, R.color.z_color_text_hint)
                ) {
                    XLog.debug("取消。。。。。")
                }
                .show()
        }
    }

    override fun documentDraft(list: List<CMSDocumentInfoJson>) {
        if (cmsPublishCategory == null || cmsApp == null) {
            XToast.toastShort(activity, "参数不正确，无法创建文档！")
            return
        }
        val config = cmsApp!!.config
        var ignoreTitle = false
        if (!TextUtils.isEmpty(config)) {
            val cmsConfig = O2SDKManager.instance().gson.fromJson(config, CMSAPPConfig::class.java)
            if (cmsConfig != null) {
                ignoreTitle = cmsConfig.ignoreTitle
                if (!cmsConfig.latest) {
                    XLog.info("没有草稿，跳转到发布页面 有配置latest 。。。。。")
                    activity?.go<CMSPublishDocumentActivity>(
                        CMSPublishDocumentActivity.start(
                            cmsPublishCategory!!,
                            ignoreTitle
                        )
                    )
                    return
                }
            }
        }
        if (list.isEmpty()) {
            XLog.info("没有草稿，跳转到发布页面")
            activity?.go<CMSPublishDocumentActivity>(
                CMSPublishDocumentActivity.start(
                    cmsPublishCategory!!,
                    ignoreTitle
                )
            )
        } else {
            XLog.info("有草稿，跳转到详细页面")
            val document = list[0]
            val options = "{\"readonly\": false}"
            activity?.go<CMSWebViewActivity>(
                CMSWebViewActivity.startBundleDataWithOptions(
                    document.id,
                    document.title,
                    options
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        webChromeClient.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 是否含有ActionBar
     */
    @JavascriptInterface
    fun actionBarLoaded(flag: String) {
        XLog.debug("actionBarLoaded.......$flag")
        if (!TextUtils.isEmpty(flag)) {
            if (activity != null && activity is PortalWebViewActivity) {
                (activity as PortalWebViewActivity).hideToolBar()
            }
        }
    }

    /**
     * js 调用  window.o2.loadUrl(url)
     */
    @JavascriptInterface
    fun loadUrl(url: String) {
        web_view_portal_content.loadUrl(url)
    }


    @JavascriptInterface
    fun openO2Work(work: String, workCompleted: String, title: String) {
        XLog.debug("open work : $work, $workCompleted, $title")
        activity?.go<TaskWebViewActivity>(TaskWebViewActivity.start(work, workCompleted, title))
    }


    // 应用对象 创建文档 打开内容管理应用页面等使用
    private var cmsApp: CMSApplicationInfoJson? = null
    private var cmsStatus = "-1" // 0 打开cms应用页面 1创建文档
    private var cmsOptions: HashMap<String, String>? = null
    private var cmsPublishCategory: CMSCategoryInfoJson? = null

    /**
     * 创建文档 目前只有 column 和 category 有效果
     * options : {
    "column" : column, //（string）可选，内容管理应用（栏目）的名称、别名或ID
    "category" : category, //（string）可选，要创建的文档所属的分类的名称、别名或ID
    "data" : data, //（json object）可选，创建文档时默认的业务数据
    "identity" : identity, //（string）可选，创建文档所使用的身份。如果此参数为空，且当前人有多个身份的情况下，会弹出身份选择对话框；否则使用默认身份。
    "callback" : callback, //（funcation）可选，文档创建后的回调函数。
    "target" : target, //（boolean）可选，为true时，在当前页面打开创建的文档；否则打开新窗口。默认false。
    "latest" : latest, //（boolean）可选，为true时，如果当前用户已经创建了此分类的文档，并且没有发布过，直接调用此文档为新文档；否则创建一个新文档。默认true。
    "selectColumnEnable" : selectColumnEnable, //（boolean）可选，是否可以选择应用和分类进行创建文档。有category参数时为默认false,否则默认为true。
    "ignoreTitle" : ignoreTitle //（boolean）可选，值为false时，创建的时候需要强制填写标题，默认为false。
    "restrictToColumn" : restrictToColumn //（boolean）可选，值为true时，会限制在传入的栏目中选择分类，默认为false。
    }
     */
    @JavascriptInterface
    fun createO2CmsDocument(options: String?) {
        XLog.debug("createO2CmsDocument : $options ")
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        cmsOptions = O2SDKManager.instance().gson.fromJson(options, type)
        if (cmsOptions == null) {
            XToast.toastShort(activity, "缺少 column 参数，目前移动端必须传入 column 参数")
            return
        }
        if (TextUtils.isEmpty(cmsOptions!!["column"])) {
            XToast.toastShort(activity, "缺少 column 参数，目前移动端必须传入 column 参数")
            return
        }
        cmsStatus = "1"
        mPresenter.loadCmsApplication(cmsOptions!!["column"]!!)
    }

    @JavascriptInterface
    fun openO2CmsApplication(appId: String, title: String) {
        XLog.debug("openO2CmsApplication : $appId  title: $title")
        showLoadingDialog()
        mPresenter.openCmsApplication(appId)
    }

    @JavascriptInterface
    fun openO2CmsDocument(docId: String, docTitle: String) {
        XLog.debug("openO2CmsDocument old : $docId, $docTitle")
        activity?.go<CMSWebViewActivity>(
            CMSWebViewActivity.startBundleDataWithOptions(
                docId,
                docTitle,
                null
            )
        )
    }

    /**
     * 新版 打开内容管理 添加第三个参数 options
     */
    @JavascriptInterface
    fun openO2CmsDocumentV2(docId: String, docTitle: String, options: String?) {
        XLog.debug("openO2CmsDocument new : $docId, $docTitle $options")
        activity?.go<CMSWebViewActivity>(
            CMSWebViewActivity.startBundleDataWithOptions(
                docId,
                docTitle,
                options
            )
        )
    }

    @JavascriptInterface
    fun openO2Meeting(result: String) {
        XLog.debug("openO2Meeting rrrrrrrrrrr")
        activity?.go<MeetingMainActivity>()
    }

    @JavascriptInterface
    fun openO2Calendar(result: String) {
        XLog.debug("openO2Calendarvvvvvvvvvvvvvvv")
        activity?.go<CalendarMainActivity>()
    }

    @JavascriptInterface
    fun openDingtalk(result: String) {
        XLog.debug("open钉钉。。。。。。")
        val intent = Intent(Intent.ACTION_VIEW)
        val jumpUrl = "dingtalk://dingtalkclient/page/link?url="
        intent.data = Uri.parse(jumpUrl)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            if (context != null && null != intent.resolveActivity(context!!.packageManager)) {
                context!!.startActivity(intent)
            } else {
                XLog.info("找不到。。。。")
            }
        } catch (e: Exception) {
            XLog.error("", e)
        }
    }

    @JavascriptInterface
    fun openScan(result: String) {
        XLog.debug("open scan ........")
        activity?.runOnUiThread {
            PermissionRequester(activity!!)
                .request(Manifest.permission.CAMERA)
                .o2Subscribe {
                    onNext { (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
                        XLog.info("granted:$granted , shouldShowRequest:$shouldShowRequestPermissionRationale, denied:$deniedPermissions")
                        if (!granted) {
                            O2DialogSupport.openAlertDialog(
                                activity,
                                "需要摄像头权限才能进行扫一扫功能！"
                            )
                        } else {
                            activity?.go<CaptureActivity>()
                        }
                    }
                }
        }

    }

    @JavascriptInterface
    fun openO2WorkSpace(type: String) {
        XLog.info("open work space $type")
        when (type.toLowerCase()) {
            "task" -> activity?.go<TaskListActivity>()
            "taskcompleted" -> activity?.go<TaskCompletedListActivity>()
            "read" -> activity?.go<ReadListActivity>()
            "readcompleted" -> activity?.go<ReadCompletedListActivity>()
            else -> activity?.go<TaskListActivity>()
        }
    }

    @JavascriptInterface
    fun closeNativeWindow(result: String) {
        XLog.info("result：$result")
        if (result == "true") {
            if (activity != null) {
                when (activity) {
                    is PortalWebViewActivity -> {
                        activity?.finish()
                    }

                    else -> {
                        XLog.error("what Activity 。。。。。。。。。")
                    }
                }
            }

        }
    }

    /**
     * 弹出窗 js调试用
     */
    @JavascriptInterface
    fun openO2Alert(message: String?) {
        if (message != null) {
            XLog.debug("弹出窗。。message:$message")
            activity?.runOnUiThread {
                O2DialogSupport.openAlertDialog(activity, message)
            }
        }
    }

}