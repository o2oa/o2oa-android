package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wugang.activityresult.library.ActivityResult
import kotlinx.android.synthetic.main.activity_work_web_view.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer.BigImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.O2ChatActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.fm.O2IMConversationPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.tbs.FileReaderActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.WorkNewActionItem
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.WorkControl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMMessage
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMMessageBody
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.MessageType
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessDraftWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ReadData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkInfoRes
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkOpinionData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PickTypeMode
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.O2WebviewDownloadListener
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.WebChromeClientWithProgressAndValueCallback
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import org.jetbrains.anko.dip
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList


class TaskWebViewActivity : BaseMVPActivity<TaskWebViewContract.View, TaskWebViewContract.Presenter>(), TaskWebViewContract.View {
    override var mPresenter: TaskWebViewContract.Presenter = TaskWebViewPresenter()


    override fun layoutResId(): Int = R.layout.activity_work_web_view

    companion object {
        val WORK_WEB_VIEW_TITLE = "xbpm.work.web.view.title"
        val WORK_WEB_VIEW_WORK = "xbpm.work.web.view.work"
        val WORK_WEB_VIEW_WORK_COMPLETED = "xbpm.work.web.view.work.completed"
        val WORK_WEB_VIEW_DRAFT = "xbpm.work.web.view.work.draft"

        fun start(work: String?, workCompleted: String?, title: String?):  Bundle {
            val bundle = Bundle()
            bundle.putString(WORK_WEB_VIEW_TITLE, title)
            bundle.putString(WORK_WEB_VIEW_WORK, work)
            bundle.putString(WORK_WEB_VIEW_WORK_COMPLETED, workCompleted)
            return bundle
        }

        fun startDraft(draft: ProcessDraftWorkData?):  Bundle {
            val bundle = Bundle()
            bundle.putSerializable(WORK_WEB_VIEW_DRAFT, draft)
            return bundle
        }
    }

    private  val WORK_WEB_VIEW_UPLOAD_REQUEST_CODE = 1001 // 表单上传附件
    private  val WORK_WEB_VIEW_UPLOAD_DATAGRID_REQUEST_CODE = 10010 // 表单数据表格中的上传附件
    private  val WORK_WEB_VIEW_REPLACE_REQUEST_CODE = 1002 // 表单替换附件
    private  val WORK_WEB_VIEW_REPLACE_DATAGRID_REQUEST_CODE = 10020 // 表单数据表格中的替换附件
    private  val TAKE_FROM_PICTURES_CODE = 1003
    private  val TAKE_FROM_CAMERA_CODE = 1004

    private var title = ""
    private  var workId = ""
    private  var workCompletedId = ""
    private  var isWorkCompleted = false
    private  var url = ""
    private var draft: ProcessDraftWorkData? = null


    private var workinfo: WorkInfoRes? = null
    private var control: WorkControl? = null
    private var read: ReadData? = null
    private var site = "" // 上传附件使用
    private var datagridParam = ""; // 数据表格上传附件使用
    private var attachmentId = ""
    private var formData: String? = ""//表单json数据
    private var formOpinion: String? = ""// 在表单内的意见信息
    private val routeNameList = ArrayList<String>()

    private val downloadDocument: DownloadDocument by lazy { DownloadDocument(this) }
//    private val cameraImageUri: Uri by lazy { FileUtil.getUriFromFile(this, File(FileExtensionHelper.getCameraCacheFilePath(this))) }
    private var cameraImagePath: String? = null// 拍照的地址

    private val webChromeClient: WebChromeClientWithProgressAndValueCallback by lazy { WebChromeClientWithProgressAndValueCallback.with(this) }
    var imageUploadData: O2UploadImageData? = null
    private val jsNotification: JSInterfaceO2mNotification by lazy { JSInterfaceO2mNotification.with(this) }
    private val jsUtil: JSInterfaceO2mUtil by lazy { JSInterfaceO2mUtil.with(this) }
    private val jsBiz: JSInterfaceO2mBiz by lazy { JSInterfaceO2mBiz.with(this) }
    private val gson: Gson by lazy { Gson() }




    override fun afterSetContentView(savedInstanceState: Bundle?) {
        title = intent.extras?.getString(WORK_WEB_VIEW_TITLE) ?: ""
        workId = intent.extras?.getString(WORK_WEB_VIEW_WORK) ?: ""
        workCompletedId = intent.extras?.getString(WORK_WEB_VIEW_WORK_COMPLETED) ?: ""
        draft = if (intent.extras?.getSerializable(WORK_WEB_VIEW_DRAFT) != null){
            intent.extras?.getSerializable(WORK_WEB_VIEW_DRAFT) as ProcessDraftWorkData
        } else {
            null
        }

        //草稿文档处理
        if (draft != null) {
            if (!TextUtils.isEmpty(draft!!.id)){
                url = APIAddressHelper.instance().getProcessDraftWithIdUrl()
                url = String.format(url, draft!!.id)
            }else {
                url = APIAddressHelper.instance().getProcessDraftUrl()
                val json = gson.toJson(draft)
                XLog.debug("草稿对象:$json")
                val enJson = URLEncoder.encode(json, "utf-8")
                XLog.debug("草稿对象 encode:$enJson")
                url = String.format(url, enJson)
            }
        }else {
            isWorkCompleted = !TextUtils.isEmpty(workCompletedId)
            if (isWorkCompleted) {
                url = APIAddressHelper.instance().getWorkCompletedUrl()
                url = String.format(url, workCompletedId)
                mPresenter.getWorkInfoByWorkOrWorkCompletedId(workCompletedId) // 后台请求工作对象
            } else {
                url = APIAddressHelper.instance().getWorkUrlPre()
                url = String.format(url, workId)
                mPresenter.getWorkInfoByWorkOrWorkCompletedId(workId) // 后台请求工作对象
            }
        }
        url += "&time=" + System.currentTimeMillis()

        XLog.debug("title:$title ,  url:$url")
        setupToolBar(title, true)
        toolbar?.setNavigationOnClickListener {
            XLog.debug("测试。。。。。。。。。。。。。。。。。。")
            processCheckNew()
        }

        web_view.addJavascriptInterface(this, "o2android")
        jsNotification.setupWebView(web_view)
        jsUtil.setupWebView(web_view)
        jsBiz.setupWebView(web_view)
        web_view.addJavascriptInterface(jsNotification, JSInterfaceO2mNotification.JSInterfaceName)
        web_view.addJavascriptInterface(jsUtil, JSInterfaceO2mUtil.JSInterfaceName)
        web_view.addJavascriptInterface(jsBiz, JSInterfaceO2mBiz.JSInterfaceName)
        web_view.setDownloadListener(O2WebviewDownloadListener(this))
        web_view.webChromeClient = webChromeClient
        web_view.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                XLog.error("ssl error, $error")
                handler?.proceed()
            }
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                XLog.debug("shouldOverrideUrlLoading:$url")
//                if (ZoneUtil.checkUrlIsInner(url)) {
                    view?.loadUrl(url)
//                } else {
//                    AndroidUtils.runDefaultBrowser(this@TaskWebViewActivity, url)
//                }
                return true
            }

        }


        web_view.webViewSetCookie(this, url)
        web_view.loadUrl(url)
        // 设置标题

        webChromeClient.onO2ReceivedTitle = { title ->
            updateToolbarTitle(title)
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            processCheckNew()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // 网页内 js 选择照片的能力
            if (webChromeClient.onActivityResult(requestCode, resultCode, data)) {
                return
            }
            when (requestCode) {
//                WORK_WEB_VIEW_UPLOAD_REQUEST_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        mPresenter.uploadAttachment(result!!, site, workId, "")
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
//                WORK_WEB_VIEW_UPLOAD_DATAGRID_REQUEST_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        mPresenter.uploadAttachment(result!!, site, workId, datagridParam)
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
//                WORK_WEB_VIEW_REPLACE_REQUEST_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        mPresenter.replaceAttachment(result!!, site, attachmentId, workId, "")
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
//                WORK_WEB_VIEW_REPLACE_DATAGRID_REQUEST_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        mPresenter.replaceAttachment(result!!, site, attachmentId, workId, datagridParam)
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
                TAKE_FROM_CAMERA_CODE -> {
                    //拍照
                    XLog.debug("拍照//// ")
                    if (!TextUtils.isEmpty(cameraImagePath)){
//                        uploadImage2FileStorageStart(FileExtensionHelper.getCameraCacheFilePath(this))
                        uploadImage2FileStorageStart(cameraImagePath!!)
                    }

                }
            }
        }
    }

    //MARK：- 表单处理

    //region 表单处理按钮

    /**
     * 删除工作
     */
    fun formDeleteBtnClick(view: View?) {
        O2DialogSupport.openConfirmDialog(this@TaskWebViewActivity, getString(R.string.delete_work_confirm_message), listener =  {
            showLoadingDialog()
            mPresenter.delete(workId)
        })
    }

    /**
     * 保存工作
     */
    fun formSaveBtnClick(view: View?) {
        XLog.debug("click save button")
        web_view.clearFocus()
        evaluateJavascriptGetFormDataAndSave()
    }

    /**
     * 继续流转
     */
    fun formGoNextBtnClick(view: View?) {
        XLog.debug("click submit button")
        web_view.clearFocus()
        formData{
            getFormOpinion{
                submitData()
            }
        }
    }

    /**
     * 标记为已阅
     */
    fun formSetReadBtnClick(view: View?) {
        O2DialogSupport.openConfirmDialog(this@TaskWebViewActivity, getString(R.string.read_complete_confirm_message), listener =  {
            showLoadingDialog()
            mPresenter.setReadComplete(read)
        })
    }

    /**
     * 撤回工作
     */
    fun formRetractBtnClick(view: View?) {
        O2DialogSupport.openConfirmDialog(this@TaskWebViewActivity, getString(R.string.retract_confirm_message), listener = {
            showLoadingDialog()
            mPresenter.retractWork(workId)
        })
    }

    //endregion

    // MARK: - finish submit callback webview javascript
    /**
     * @param site 如果是手写签批的 返回site值
     */
    fun finishSubmit(site: String?) {
        if (!TextUtils.isEmpty(site)) {
            XLog.info("finish submit ...$site")
        }
        finish()
    }


    // MARK: - javascriptInterface

    //region javascriptInterface

    /**
     * 统一处理
     */
    @JavascriptInterface
    fun postMessage(message: String?) {
        XLog.debug("进入postMessage 。。。。。。。")
        if (!TextUtils.isEmpty(message)) {
            XLog.debug(message)
            try {
                val json = JSONTokener(message).nextValue()
                if (json is JSONObject) {
                    when (json.getString("type")) {
                        // 关闭当前页面
                        "closeWork" -> runOnUiThread { finish() }
                        // 表单加载成功后
                        "appFormLoaded" -> appFormLoaded(message!!)
                        // 附件控件的上传
                        "uploadAttachment" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2TaskUploadAttachmentMessage>>() {}.type
                            val value: O2JsPostMessage<O2TaskUploadAttachmentMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data?.site)) {
                                uploadAttachment(value.data!!.site!!)
                            } else {
                                XLog.error("uploadAttachment 参数不正确，缺少site，无法上传附件")
                            }
                        }
                        // 附件控件的上传 在数据表格中
                        "uploadAttachmentForDatagrid" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2TaskUploadAttachmentMessage>>() {}.type
                            val value: O2JsPostMessage<O2TaskUploadAttachmentMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data?.site) && !TextUtils.isEmpty(value.data?.param)) {
                                uploadAttachmentForDatagrid(value.data!!.site!!, value.data!!.param!!)
                            } else {
                                XLog.error("uploadAttachmentForDatagrid 参数不正确，缺少site或param，无法上传附件")
                            }
                        }
                        // 附件控件的替换
                        "replaceAttachment" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2TaskReplaceAttachmentMessage>>() {}.type
                            val value: O2JsPostMessage<O2TaskReplaceAttachmentMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data?.site) && !TextUtils.isEmpty(value.data?.attachmentId)) {
                                replaceAttachment(value.data!!.attachmentId!!, value.data!!.site!!)
                            } else {
                                XLog.error("replaceAttachment 参数不正确，缺少 attachmentId 或 site，无法替换附件")
                            }
                        }
                        // 附件控件的替换 在数据表格中
                        "replaceAttachmentForDatagrid" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2TaskReplaceAttachmentMessage>>() {}.type
                            val value: O2JsPostMessage<O2TaskReplaceAttachmentMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data?.site) && !TextUtils.isEmpty(value.data?.attachmentId) && !TextUtils.isEmpty(value.data?.param)) {
                                replaceAttachmentForDatagrid(value.data!!.attachmentId!!, value.data!!.site!!, value.data!!.param!!)
                            } else {
                                XLog.error("replaceAttachmentForDatagrid 参数不正确，缺少 attachmentId 或 site，无法替换附件")
                            }
                        }
                        // 下载附件 并预览
                        "downloadAttachment" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2TaskDownloadAttachmentMessage>>() {}.type
                            val value: O2JsPostMessage<O2TaskDownloadAttachmentMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data?.attachmentId)) {
                                downloadAttachment(value.data!!.attachmentId!!)
                            } else {
                                XLog.error("downloadAttachment 参数不正确，缺少 attachmentId，无法下载附件")
                            }
                        }
                        // 打开正文
                        "openDocument" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2TaskOpenDocumentMessage>>() {}.type
                            val value: O2JsPostMessage<O2TaskOpenDocumentMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data?.url)) {
                                openDocument(value.data!!.url!!)
                            } else {
                                XLog.error("openDocument 参数不正确，缺少 url，无法打开正文")
                            }
                        }
                        // 表单的图片控件
                        "uploadImage2FileStorage" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2UploadImageData>>() {}.type
                            val value: O2JsPostMessage<O2UploadImageData> = gson.fromJson(message, type)
                            if (value.data != null ) {
                                uploadImage2FileStorage(gson.toJson(value.data!!))
                            } else {
                                XLog.error("uploadImage2FileStorage 参数不正确，无法上传图片")
                            }
                        }
                        // 将当前工作分享到聊天
                        "shareToIMChat" -> {
                            runOnUiThread {
                                ActivityResult.of(this)
                                    .className(O2IMConversationPickerActivity::class.java)
                                    .greenChannel().forResult { _, data ->
                                        val result = data?.getParcelableExtra<IMConversationInfo>(O2IMConversationPickerActivity.IM_CONVERSATION_PICKED_RESULT)
                                        if (result != null) {
                                            shareToIMChat(result)
                                        }
                                    }
                            }
                        }
                    }
                } else {
                    XLog.error("message 格式错误！！！")
                }
            } catch (e: Exception) {
                XLog.error("", e)
            }
        } else {
            XLog.error("o2android.postMessage error, 没有传入message内容！")
        }
    }

//    private fun postMessageCallback(callback: String?) {
//        if (!TextUtils.isEmpty(callback)) {
//            web_view.evaluateJavascript("layout.app.appForm.uploadedAttachment(\"$site\", \"$attachmentId\")") { value ->
//                XLog.debug("uploadedAttachment， onReceiveValue value=$value")
//            }
//        }
//    }


    @JavascriptInterface
    fun closeWork(result: String) {
        XLog.debug("关闭表单 closeWork ：$result")
        runOnUiThread {
            finish()
        }
    }

    /**
     * 表单加载完成后回调
     */
    @JavascriptInterface
    fun appFormLoaded(result: String) {// 获取control 动态生成操作按钮
        XLog.debug("表单加载完成回调：$result")// 20190520 result改成了操作按钮列表 如果是result是true就是老系统，用原来的方式。。。。。。。。得兼容老方式诶
        //2019-12-09 使用workwithaction的html 不用移动端的相关操作按钮了
//        runOnUiThread {
//            if (TextUtils.isEmpty(title)) {
//                web_view.evaluateJavascript("layout.app.appForm.businessData.work.title") { value ->
//                    XLog.debug("title: $title")
//                    try {
//                        title = O2SDKManager.instance().gson.fromJson(value, String::class.java)
//                        updateToolbarTitle(title)
//                    } catch (e: Exception) {
//                    }
//                }
//            }
//
//            if (result == "true") { // 老版本的操作
//                // 获取control 生成操作按钮
//                web_view.evaluateJavascript("layout.app.appForm.businessData.control") { value ->
//                    XLog.debug("control: $value")
//                    try {
//                        control = O2SDKManager.instance().gson.fromJson(value, WorkControl::class.java)
//                    } catch (e: Exception) {
//                    }
//                    initOptionBar()
//                }
//            }else {// 2019-05-21 增加新版操作按钮
//                // 解析result 操作按钮列表
//                if (!TextUtils.isEmpty(result)) {
//                    try {
//                        val type = object : TypeToken<List<WorkNewActionItem>>() {}.type
//                        val list: List<WorkNewActionItem> = O2SDKManager.instance().gson.fromJson(result, type)
//                        initOptionBarNew(list)
//                    }catch (e: Exception){
//                        XLog.error("解析操作按钮结果列表出错", e)
//                    }
//                }else {
//                    XLog.error("操作按钮结果为空")
//                }
//
//            }
//
//            web_view.evaluateJavascript("layout.app.appForm.businessData.read") { value ->
//                XLog.debug("read: $value")
//                try {
//                    read = O2SDKManager.instance().gson.fromJson(value, ReadData::class.java)
//                } catch (e: Exception) {
//                }
//            }
//        }
    }



    /**
     * 上传附件
     *
     * @param site
     */
    @JavascriptInterface
    fun uploadAttachment(site: String) {
        XLog.debug("uploadAttachment site:$site")
        if (TextUtils.isEmpty(site)) {
            XLog.error("没有传入site")
            return
        }
        this.site = site
        runOnUiThread {
            openFancyFilePicker(WORK_WEB_VIEW_UPLOAD_REQUEST_CODE, true)
        }
    }

    /**
     * 数据表格内的附件上传
     * @param site
     * @param param
     */
    @JavascriptInterface
    fun uploadAttachmentForDatagrid(site: String, param: String) {
        XLog.debug("uploadAttachmentForDatagrid site:$site, param: $param")
        if (TextUtils.isEmpty(site)) {
            XLog.error("没有传入site")
            return
        }

        runOnUiThread {
            this.site = site
            this.datagridParam = param
            openFancyFilePicker(WORK_WEB_VIEW_UPLOAD_DATAGRID_REQUEST_CODE, true)
        }
    }

    /**
     * 替换附件
     *
     * @param attachmentId
     * @param site
     */
    @JavascriptInterface
    fun replaceAttachment(attachmentId: String, site: String) {
        XLog.debug("replaceAttachment site:$site, attachmentId:$attachmentId")
        if (TextUtils.isEmpty(attachmentId) || TextUtils.isEmpty(site)) {
            XLog.error("没有传入attachmentId 或 site")
            return
        }
        runOnUiThread {
            this.site = site
            this.attachmentId = attachmentId
            openFancyFilePicker(WORK_WEB_VIEW_REPLACE_REQUEST_CODE, false)
        }
    }

    /**
     * 数据表格内的附件替换
     * @param site
     * @param param
     */
    @JavascriptInterface
    fun  replaceAttachmentForDatagrid(attachmentId: String, site: String, param: String) {
        XLog.debug("replaceAttachmentForDatagrid site:$site, attachmentId:$attachmentId , param: $param")
        if (TextUtils.isEmpty(attachmentId) || TextUtils.isEmpty(site)) {
            XLog.error("没有传入attachmentId 或 site")
            return
        }
        runOnUiThread {
            this.site = site
            this.attachmentId = attachmentId
            this.datagridParam = param
            openFancyFilePicker(WORK_WEB_VIEW_REPLACE_DATAGRID_REQUEST_CODE, false)
        }
    }

    /**
     * 下载附件
     *
     * @param attachmentId
     */
    @JavascriptInterface
    fun downloadAttachment(attachmentId: String) {
        XLog.debug("download attachmentId:$attachmentId")
        if (TextUtils.isEmpty(attachmentId)) {
            XLog.error("调用失败，附件id没有传入！")
            return
        }
        runOnUiThread {
            showLoadingDialog()
        }
        if (isWorkCompleted) {
            mPresenter.downloadWorkCompletedAttachment(attachmentId, workCompletedId)
        }else {
            mPresenter.downloadAttachment(attachmentId, workId)
        }
    }

    /**
     * 打开文档 公文打开 office pdf 等
     */
    @JavascriptInterface
    fun openDocument(url: String) {
        XLog.debug("打开文档。。。。。文档地址：$url")
        runOnUiThread {
            showLoadingDialog()
        }
        downloadDocument.downloadDocumentAndOpenIt(url) {
            hideLoadingDialog()
        }
    }

    /**
     * 弹出窗 js调试用
     */
    @JavascriptInterface
    fun openO2Alert(message: String?) {
        if (message != null) {
            XLog.debug("弹出窗。。message:$message")
            runOnUiThread {
                O2DialogSupport.openAlertDialog(this, message)
            }
        }
    }

    /**
     * 图片控件
     */
    @JavascriptInterface
    fun uploadImage2FileStorage(json: String?) {
        imageUploadData = null
        XLog.debug("打开图片上传控件， $json")
        runOnUiThread {
            if (json != null) {
                imageUploadData =  gson.fromJson(json, O2UploadImageData::class.java)
                showPictureChooseMenu()
            }else {
                XToast.toastShort(this, getString(R.string.message_arg_error))
            }
        }
    }

    /**
     * 分享到聊天会话
     */
    private fun shareToIMChat(con: IMConversationInfo) {
        if (workinfo != null) {
            val body = IMMessageBody(type = MessageType.process.key,body = "", title = workinfo?.title ,
                job = workinfo?.job, process = workinfo?.process,
                processName = workinfo?.processName, application = workinfo?.application,
                applicationName = workinfo?.applicationName, work = workinfo?.id)
            val bodyJson = O2SDKManager.instance().gson.toJson(body)
            XLog.debug("body: $bodyJson")
            val uuid = UUID.randomUUID().toString()
            val time = DateHelper.now()
            val message = IMMessage(uuid, con.id!!, bodyJson,
                O2SDKManager.instance().distinguishedName, time, 1)
            showLoadingDialog()
            mPresenter.sendImMessage(message)
        } else {
            XToast.toastShort(this, getString(R.string.message_work_info_not_exist))
        }
    }

    //endregion

    //MARK: - view implements

    //region view implements

    override fun workOrWorkCompletedInfo(info: WorkInfoRes?) {
        workinfo = info
        if (info != null && !TextUtils.isEmpty(info.completedTime)) {
            XLog.info("当前工作已完成！")
            workCompletedId = info.id
            isWorkCompleted = true
        }
    }

    override fun finishLoading() {
        XLog.debug("finishLoading.........")
        hideLoadingDialog()
    }

    override fun saveSuccess() {
        XLog.debug("savesucess.........")
        evaluateJavascriptAfterSave {
            hideLoadingDialog()
            XToast.toastShort(this, getString(R.string.message_save_success))
        }
    }
    override fun submitSuccess() {
        hideLoadingDialog()
        finish()
    }

    override fun setReadCompletedSuccess() {
        hideLoadingDialog()
        finish()
    }

    override fun retractSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_withdraw_success))
        finish()
    }

    override fun retractFail() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_withdraw_fail))
    }

    override fun deleteSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_delete_success))
        finish()
    }

    override fun deleteFail() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_delete_fail))
    }

    override fun uploadMaxFiles() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_upload_file_max_number))
    }

    override fun uploadAttachmentSuccess(attachmentId: String, site: String, datagridParam:String) {
        XLog.debug("uploadAttachmentResponse attachmentId:$attachmentId, site:$site datagridParam：$datagridParam")
        hideLoadingDialog()
        if (TextUtils.isEmpty(datagridParam)) {
            web_view.evaluateJavascript("layout.app.appForm.uploadedAttachment(\"$site\", \"$attachmentId\")") { value ->
                XLog.debug("uploadedAttachment， onReceiveValue value=$value")
            }
        }  else {
            web_view.evaluateJavascript("layout.app.appForm.uploadedAttachmentDatagrid(\"$site\", \"$attachmentId\", \"$datagridParam\")") { value ->
                XLog.debug("uploadedAttachmentDatagrid， onReceiveValue value=$value")
            }
        }
    }

    override fun replaceAttachmentSuccess(attachmentId: String, site: String, datagridParam:String) {
        XLog.debug("replaceAttachmentResponse attachmentId:$attachmentId, site:$site")
        hideLoadingDialog()
        if (TextUtils.isEmpty(datagridParam)) {
            web_view.evaluateJavascript("layout.app.appForm.replacedAttachment(\"$site\", \"$attachmentId\")") { value ->
                XLog.debug("replacedAttachment， onReceiveValue value=$value")
            }
        } else {
            web_view.evaluateJavascript("layout.app.appForm.replacedAttachmentDatagrid(\"$site\", \"$attachmentId\", \"$datagridParam\")") { value ->
                XLog.debug("replacedAttachment， onReceiveValue value=$value")
            }
        }
    }

    override fun downloadAttachmentSuccess(file: File) {
        hideLoadingDialog()
//        if (file.exists()) AndroidUtils.openFileWithDefaultApp(this, file)
        if (file.exists()){
            if (FileExtensionHelper.isImageFromFileExtension(file.extension)) {
//                go<LocalImageViewActivity>(LocalImageViewActivity.startBundle(file.absolutePath))
                BigImageViewActivity.startLocalFile(this, file.absolutePath)
            }else {
                go<FileReaderActivity>(FileReaderActivity.startBundle(file.absolutePath))
//                QbSdk.openFileReader(this, file.absolutePath, HashMap<String, String>()) { p0 -> XLog.info("打开文件返回。。。。。$p0") }
            }
        }
    }

    override fun invalidateArgs() {
        XToast.toastShort(this, getString(R.string.message_arg_error))
    }

    override fun downloadFail(message: String) {
        finishLoading()
        XToast.toastShort(this, message)
    }

    override fun upload2FileStorageFail(message: String) {
        hideLoadingDialog()
        XToast.toastShort(this, message)
    }

    override fun upload2FileStorageSuccess(id: String) {
        hideLoadingDialog()
        if (imageUploadData != null) {
            imageUploadData!!.fileId = id
            val callback = imageUploadData!!.callback
            val json = gson.toJson(imageUploadData)
            val js = "$callback('$json')"
            XLog.debug("执行js:$js")
            web_view.evaluateJavascript(js){
                value -> XLog.debug("replacedAttachment， onReceiveValue value=$value")
            }
        }else {
            XLog.error("图片控件对象不存在。。。。。。。。")
        }
    }

    override fun sendImMessageSuccess(convId: String) {
        hideLoadingDialog()
        O2DialogSupport.openConfirmDialog(this, getString(R.string.dialog_msg_confirm_open_im_chat), { _ ->
            O2ChatActivity.startChat(this@TaskWebViewActivity, convId)
        })
    }

    override fun sendImMessageFail(err: String) {
        hideLoadingDialog()
        XToast.toastShort(this, err)
    }

    //endregion


    //region  private function


    /**
     * webview 返回上级一层
     */
    private fun goBack(): Boolean{
        return if (web_view?.canGoBack() == true) {
            web_view.goBack()
            true
        } else {
            false
        }
    }


    /**
     * 检查新建
     * 关闭页面的时候检查一下 是否要删除草稿
     */
    private fun processCheckNew() {
        if (!goBack()) {
            web_view.evaluateJavascript("layout.app.appForm.finishOnMobile()"){
                    value -> XLog.debug("finishOnMobile /。。。。。。。。。。。。。。$value")
                try {
                    finish()
                }catch (e: Exception){
                    XLog.error("", e)
                }
            }
        }
    }

    /**
     * 生成操作按钮
     */
    private fun initOptionBar() {
        XLog.debug("initOptionBar......安装操作按钮")
        if (control != null) {
            var count = 0
            if (control?.allowDelete == true) {
                count ++
                tv_work_form_delete_btn.visible()
            }
            if (control?.allowSave == true) {
                count ++
                tv_work_form_save_btn.visible()
            }
            if (control?.allowProcessing == true) {
                    count ++
                    tv_work_form_go_next_btn.visible()
                }
            if (control?.allowReadProcessing == true) {
                    count ++
                    tv_work_form_set_read_btn.visible()
                }
            if (control?.allowRetract == true) {
                    count ++
                    tv_work_form_retract_btn.visible()
            }
            if (count > 0 ) {
                bottom_operate_button_layout.visible()
                fl_bottom_operation_bar.visible()
            }
        }else {
            XLog.error("control为空。。。。。。")
        }
    }


    /**
     * 20190521
     * 生成操作按钮 新版
     */
    private fun initOptionBarNew(list: List<WorkNewActionItem>) {
        if(list.isNotEmpty()) {
            when(list.count()) {
                1 -> {
                    val menuItem = list[0]
                    tv_work_form_bottom_first_action.text = menuItem.text
                    tv_work_form_bottom_first_action.visible()
                    tv_work_form_bottom_first_action.setOnClickListener {
                        bottomButtonAction(menuItem)
                    }
                }
                2 -> {
                    val menuItem = list[0]
                    tv_work_form_bottom_first_action.text = menuItem.text
                    tv_work_form_bottom_first_action.visible()
                    tv_work_form_bottom_first_action.setOnClickListener {
                        bottomButtonAction(menuItem)
                    }
                    val menuItem2 = list[1]
                    tv_work_form_bottom_second_action.text = menuItem2.text
                    tv_work_form_bottom_second_action.visible()
                    tv_work_form_bottom_second_action.setOnClickListener {
                        bottomButtonAction(menuItem2)
                    }
                }
                else -> {
                    val menuItem = list[0]
                    tv_work_form_bottom_first_action.text = menuItem.text
                    tv_work_form_bottom_first_action.visible()
                    tv_work_form_bottom_first_action.setOnClickListener {
                        bottomButtonAction(menuItem)
                    }
                    val menuItem2 = list[1]
                    tv_work_form_bottom_second_action.text = menuItem2.text
                    tv_work_form_bottom_second_action.visible()
                    tv_work_form_bottom_second_action.setOnClickListener {
                        bottomButtonAction(menuItem2)
                    }
                    img_work_form_bottom_more_action.visible()
                    img_work_form_bottom_more_action.setOnClickListener {
                        if (rl_bottom_operation_bar_mask.visibility == View.VISIBLE) {
                            rl_bottom_operation_bar_mask.gone()
                        }else {
                            rl_bottom_operation_bar_mask.visible()
                        }
                    }
                    rl_bottom_operation_bar_mask.setOnClickListener {
                        XLog.debug("点击了背景。。。。。")
                        rl_bottom_operation_bar_mask.gone()
                    }
                    //装载更多按钮
                    ll_bottom_operation_bar_new_more.removeAllViews()
                    for ((index, item) in list.withIndex()) {
                       if (index > 1) {
                           val button = newBottomMoreButton(item)
                           ll_bottom_operation_bar_new_more.addView(button)
                           button.setOnClickListener {
                               bottomButtonAction(item)
                           }
                       }
                    }
                }

            }
            fl_bottom_operation_bar.visible()
            ll_bottom_operation_bar_new.visible()
        }
    }

    /**
     * 底部操作按钮执行操作
     */
    private fun bottomButtonAction(menuItem: WorkNewActionItem) {
        XLog.debug("点击了按钮${menuItem.text}")
        XLog.debug("动作：${menuItem.action} , control:${menuItem.control}")

        if (!TextUtils.isEmpty(menuItem.actionScript)) {
            val jsExc = "layout.app.appForm._runCustomAction(${menuItem.actionScript})"
            XLog.debug(jsExc)
            web_view.evaluateJavascript(jsExc) { value ->
                XLog.debug("onReceiveValue value=$value")
            }
        }else {
            when(menuItem.control) {
                "allowDelete" -> {
                    formDeleteBtnClick(null)
                }
                "allowSave" -> {
                    formSaveBtnClick(null)
                }
                "allowProcessing" -> {
                    formGoNextBtnClick(null)
                }
                "allowReadProcessing" ->{
                    formSetReadBtnClick(null)
                }
                "allowRetract" -> {
                    formRetractBtnClick(null)
                }
                else -> {
                    val jsExc ="layout.app.appForm[\"${menuItem.action}\"]()"
                    XLog.debug(jsExc)
                    web_view.evaluateJavascript(jsExc) { value ->
                        XLog.debug("onReceiveValue value=$value")
                    }
                }
            }
        }
        rl_bottom_operation_bar_mask.gone()
    }

    /**
     * 更多按钮生成
     */
    private fun newBottomMoreButton(menuItem: WorkNewActionItem): TextView {
        val button = TextView(this)
        val layoutparam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(42))
        layoutparam.bottomMargin = dip(5)
        button.layoutParams = layoutparam
        button.gravity = Gravity.CENTER
        button.text = menuItem.text
        button.setTextColor(ContextCompat.getColor(this, R.color.z_color_primary))
        button.setBackgroundColor(Color.WHITE)
        button.setTextSize(COMPLEX_UNIT_SP, 16f)
        return button
    }

    /**
     * 提交数据
     */
    private fun submitData() {
        web_view.evaluateJavascript("layout.app.appForm.formValidation(\"\", \"\")") { value ->
            XLog.debug("formValidation，value:$value")
            if (value == "true") {
                web_view.evaluateJavascript("layout.app.appForm.businessData.task") { task ->
                    XLog.debug("submitData, onReceiveValue value=$task")
                    try {
                        XLog.debug("submitData，TaskData:$task")
                        if (TextUtils.isEmpty(task)) {
                            XToast.toastShort(this@TaskWebViewActivity, getString(R.string.message_task_data_load_fail))
                        }else {
                            openTaskWorkSubmitDialog(task)
                        }
                    } catch (e: Exception) {
                        XLog.error("", e)
                        XToast.toastShort(this@TaskWebViewActivity, getString(R.string.message_data_parse_fail))
                    }
                }
            } else {
                XToast.toastShort(this@TaskWebViewActivity, getString(R.string.message_check_form))
            }
        }
    }

    /**
     * 校验表单
     * 选择路由和填写意见后，提交工作前
     */
    fun validateFormForSubmitDialog(route: String, opinion: String, callback:(Boolean)->Unit) {
        web_view.evaluateJavascript("layout.app.appForm.formValidation(\"$route\", \"$opinion\")") { value ->
            if (value == "true") {
                callback(true)
            }else {
                callback(false)
            }
        }
    }

    private fun openTaskWorkSubmitDialog(taskData: String) {
        TaskWorkSubmitDialogFragment.startWorkDialog(workId, taskData, formData, formOpinion)
                .show(supportFragmentManager, TaskWorkSubmitDialogFragment.TAG)
    }


    private fun formData(callback: () -> Unit) {
        web_view.evaluateJavascript("layout.app.appForm.getData()") { value ->
            XLog.debug("evaluateJavascriptGetFormData， onReceiveValue form value=$value")
            formData = value
            callback()
        }
    }

    private fun getFormOpinion(callback: () -> Unit) {
        web_view.evaluateJavascript("layout.app.appForm.getOpinion()") { value ->
            XLog.debug("evaluateJavascript get from Opinion， onReceiveValue form value=$value")
            if (!TextUtils.isEmpty(value)) {
                formOpinion = if (value == "\"\"") {
                    ""
                }else {
                    var result = ""
                    try {
                        val woData = gson.fromJson(value, WorkOpinionData::class.java)
                        result = woData.opinion ?: ""
                    } catch (e: Exception) {
                    }
                    result
                }

            }
            callback()
        }
    }


    private fun evaluateJavascriptGetFormDataAndSave() {
        showLoadingDialog()
        web_view.evaluateJavascript("(layout.app.appForm.fireEvent(\"beforeSave\");return layout.app.appForm.getData();)") { value ->
            XLog.debug("evaluateJavascriptGetFormDataAndSave， onReceiveValue save value=$value")
            formData = value
            XLog.debug("执行完成。。。。")
            runOnUiThread {
                XLog.debug("runOnUiThread  ....................")
                if (formData == null || "" == formData) {
                    XLog.debug("formData is null")
                    hideLoadingDialog()
                    XToast.toastShort(this@TaskWebViewActivity, getString(R.string.message_form_data_empty))
                }else {
                    evaluateJavascriptBeforeSave {
                        mPresenter.save(workId, formData!!)
                    }
                }
            }
        }
    }



    /**
     * 执行beforeSave
     */
    fun evaluateJavascriptBeforeSave(callback: () -> Unit) {
        web_view.evaluateJavascript("layout.app.appForm.fireEvent(\"beforeSave\")") { value ->
            XLog.info("执行beforeSave ， result: $value")
            callback()
        }
    }
    /**
     * 执行 afterSave
     */
    fun evaluateJavascriptAfterSave(callback: () -> Unit) {
        web_view.evaluateJavascript("layout.app.appForm.fireEvent(\"afterSave\")") { value ->
            XLog.info("执行afterSave ， result: $value")
            callback()
        }
    }
    /**
     * 执行 beforeProcess
     */
    fun evaluateJavascriptBeforeProcess(callback: () -> Unit) {
        web_view.evaluateJavascript("layout.app.appForm.fireEvent(\"beforeProcess\")") { value ->
            XLog.info("执行 beforeProcess ， result: $value")
            callback()
        }
    }
    /**
     * 执行 afterProcess
     */
    fun evaluateJavascriptAfterProcess(callback: () -> Unit) {
        web_view.evaluateJavascript("layout.app.appForm.fireEvent(\"afterProcess\")") { value ->
            XLog.info("执行 afterProcess ， result: $value")
            callback()
        }
    }



    private fun showPictureChooseMenu() {
        BottomSheetMenu(this)
                .setTitle(getString(R.string.upload_photo))
                .setItem(getString(R.string.take_from_album), ContextCompat.getColor(this, R.color.z_color_text_primary)) {
                    takeFromPictures()
                }
                .setItem(getString(R.string.take_photo), ContextCompat.getColor(this, R.color.z_color_text_primary)) {
                    takeFromCamera()
                }
                .setCancelButton(getString(R.string.cancel), ContextCompat.getColor(this, R.color.z_color_text_hint)) {
                    XLog.debug("取消。。。。。")
                }
                .show()
    }

    private fun takeFromPictures() {
        PicturePickUtil().withAction(this)
            .forResult { files ->
                if (files!=null && files.isNotEmpty()) {
                    uploadImage2FileStorageStart(files[0])
                }
            }
    }

    private fun takeFromCamera() {
        PermissionRequester(this).request(Manifest.permission.CAMERA)
                .o2Subscribe {
                    onNext { (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
                        XLog.info("granted:$granted , shouldShowRequest:$shouldShowRequestPermissionRationale, denied:$deniedPermissions")
                        if (!granted) {
                            O2DialogSupport.openAlertDialog(this@TaskWebViewActivity, getString(R.string.message_my_no_camera_permission))
                        } else {
                            openCamera()
                        }
                    }
                }
    }


    private fun openCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        //return-data false 不是直接返回拍照后的照片Bitmap 因为照片太大会传输失败
//        intent.putExtra("return-data", false)
//        //改用Uri 传递
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
//        intent.putExtra("noFaceDetection", true)
//        startActivityForResult(intent, TAKE_FROM_CAMERA_CODE)


        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    FileExtensionHelper.createImageFile(this)
                } catch (ex: IOException) {
                    XToast.toastShort(this, getString(R.string.message_camera_file_create_error))
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    cameraImagePath = it.absolutePath
                    val photoURI = FileUtil.getUriFromFile(this, it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_FROM_CAMERA_CODE)
                }
            }
        }
    }



    private fun openFancyFilePicker(requestCode: Int, multiple: Boolean) {
        PicturePickUtil().withAction(this)
            .setMode(PickTypeMode.File)
            .allowMultiple(multiple)
            .forResult { files ->
                if (files !=null && files.isNotEmpty()) {
                     when(requestCode) {
                         WORK_WEB_VIEW_UPLOAD_REQUEST_CODE -> {
                             showLoadingDialog()
                             mPresenter.uploadAttachmentList(files, site, workId, "")
                         }
                         WORK_WEB_VIEW_UPLOAD_DATAGRID_REQUEST_CODE -> {
                             showLoadingDialog()
                             mPresenter.uploadAttachmentList(files, site, workId, datagridParam)
                         }
                         WORK_WEB_VIEW_REPLACE_REQUEST_CODE -> {
                             val result = files[0]
                             if (!TextUtils.isEmpty(result)) {
                                 XLog.debug("uri path:$result")
                                 showLoadingDialog()
                                 mPresenter.replaceAttachment(result, site, attachmentId, workId, "")
                             } else {
                                 XLog.error("FilePicker 没有返回值！")
                             }
                         }
                         WORK_WEB_VIEW_REPLACE_DATAGRID_REQUEST_CODE -> {
                             val result = files[0]
                             if (!TextUtils.isEmpty(result)) {
                                 XLog.debug("uri path:$result")
                                 showLoadingDialog()
                                 mPresenter.replaceAttachment(result, site, attachmentId, workId, datagridParam)
                             } else {
                                 XLog.error("FilePicker 没有返回值！")
                             }
                         }
                     }
                } else {
                    XLog.error("FilePicker 没有返回值！")
                }
            }
    }



    private fun uploadImage2FileStorageStart(filePath: String) {
        showLoadingDialog()
        if (imageUploadData != null) {
            mPresenter.upload2FileStorage(filePath, imageUploadData!!.referencetype, imageUploadData!!.reference)
        }else {
            finishLoading()
            XToast.toastShort(this, getString(R.string.message_arg_error))
        }
    }

    //endregion

}
