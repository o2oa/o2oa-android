package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.cms.view


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_cms_web_view_document.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.calendar.CalendarMainActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer.BigImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.meeting.main.MeetingMainActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.ReadCompletedListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.ReadListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.TaskCompletedListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.TaskListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.DownloadDocument
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.JSInterfaceO2mBiz
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.JSInterfaceO2mNotification
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.JSInterfaceO2mUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PickTypeMode
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.O2WebviewDownloadListener
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.WebChromeClientWithProgressAndValueCallback
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialogfragment.RecordVoiceFragment
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.util.*


class CMSWebViewActivity : BaseMVPActivity<CMSWebViewContract.View, CMSWebViewContract.Presenter>(), CMSWebViewContract.View {
    override var mPresenter: CMSWebViewContract.Presenter = CMSWebViewPresenter()

    override fun layoutResId(): Int  = R.layout.activity_cms_web_view_document

    companion object {
        const val CMS_VIEW_DOCUMENT_ID_KEY = "CMS_VIEW_DOCUMENT_ID_KEY"
        const val CMS_VIEW_DOCUMENT_TITLE_KEY = "CMS_VIEW_DOCUMENT_TITLE_KEY"
        const val CMS_VIEW_DOCUMENT_OPTIONS_KEY = "CMS_VIEW_DOCUMENT_OPTIONS_KEY"

        fun startBundleData(docId: String, docTitle:String): Bundle {
            val bundle = Bundle()
            bundle.putString(CMS_VIEW_DOCUMENT_ID_KEY, docId)
            bundle.putString(CMS_VIEW_DOCUMENT_TITLE_KEY, docTitle)
            return bundle
        }

        fun startBundleDataWithOptions(docId: String, docTitle: String, options: String?): Bundle {
            val bundle = Bundle()
            bundle.putString(CMS_VIEW_DOCUMENT_ID_KEY, docId)
            bundle.putString(CMS_VIEW_DOCUMENT_TITLE_KEY, docTitle)
            bundle.putString(CMS_VIEW_DOCUMENT_OPTIONS_KEY, options)
            return bundle
        }
    }
    private val UPLOAD_REQUEST_CODE = 10086
    private val UPLOAD_DATAGRID_REQUEST_CODE = 100860
    private val REPLACE_REQUEST_CODE = 10087
    private val REPLACE_DATAGRID_REQUEST_CODE = 100870
    private val TAKE_FROM_PICTURES_CODE = 10088
    private val TAKE_FROM_CAMERA_CODE = 10089
    private var docId = ""
    private var docTitle = ""
    private var url = ""
    private val webChromeClient: WebChromeClientWithProgressAndValueCallback by lazy { WebChromeClientWithProgressAndValueCallback.with(this) }
    private val jsNotification: JSInterfaceO2mNotification by lazy { JSInterfaceO2mNotification.with(this) }
    private val jsUtil: JSInterfaceO2mUtil by lazy { JSInterfaceO2mUtil.with(this) }
    private val jsBiz: JSInterfaceO2mBiz by lazy { JSInterfaceO2mBiz.with(this) }
    private val gson: Gson by lazy { Gson() }

    private val downloadDocument: DownloadDocument by lazy { DownloadDocument(this) }
//    private val cameraImageUri: Uri by lazy { FileUtil.getUriFromFile(this, File(FileExtensionHelper.getCameraCacheFilePath(this))) }
    private var cameraImagePath: String? = null
    //上传附件
    private var site = ""
    private var datagridParam = ""; // 数据表格上传附件使用
    //replace 附件
    private var attachmentId = ""
    // 图片控制器
    private var imageUploadData: O2UploadImageData? = null

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        docId = intent.extras?.getString(CMS_VIEW_DOCUMENT_ID_KEY) ?: ""
        docTitle = intent.extras?.getString(CMS_VIEW_DOCUMENT_TITLE_KEY) ?: ""
        val options = intent.extras?.getString(CMS_VIEW_DOCUMENT_OPTIONS_KEY) ?: ""
        //初始化附件存储目录  权限
        val folder = File(FileExtensionHelper.getXBPMCMSAttachFolder(this))
        if (!folder.exists()) {
            folder.mkdirs()
        }
        url = APIAddressHelper.instance().getCMSWebViewUrl(docId)
        // 把options 放到url的query里面去
        try {
            if (!TextUtils.isEmpty(options)) {
                XLog.info("options : $options")
                val type = object : TypeToken<HashMap<String, String>>() {}.type
                val jsonMap: HashMap<String, String>? =
                    O2SDKManager.instance().gson.fromJson(options, type)
                if (jsonMap != null) {
                    if (jsonMap.containsKey("readonly")) {
                        val read = jsonMap["readonly"]
                        XLog.info("read 【$read】")
                        if (!TextUtils.isEmpty(read) && read == "false") { // 编辑模式 使用带操作按钮的页面
                            url = APIAddressHelper.instance().getCMSWebViewUrlWithAction(docId)
                        }
                    }
                    jsonMap.entries.forEach { item ->
                        XLog.info("key ${item.key} value ${item.value}")
                        val valueEncode = URLEncoder.encode(item.value, "utf-8")
                        url += "&${item.key}=${valueEncode}"
                    }
                }

            }
        }catch (e: Exception) {
            XLog.error("", e)
        }
        url += "&time="+System.currentTimeMillis()
        XLog.info("document url=$url")

        setupToolBar(docTitle, true)

        //init webview
        web_view_cms_document_content.addJavascriptInterface(this, "o2android")
        jsNotification.setupWebView(web_view_cms_document_content)
        jsUtil.setupWebView(web_view_cms_document_content)
        jsBiz.setupWebView(web_view_cms_document_content)

        web_view_cms_document_content.addJavascriptInterface(jsNotification, JSInterfaceO2mNotification.JSInterfaceName)
        web_view_cms_document_content.addJavascriptInterface(jsUtil, JSInterfaceO2mUtil.JSInterfaceName)
        web_view_cms_document_content.addJavascriptInterface(jsBiz, JSInterfaceO2mBiz.JSInterfaceName)
        web_view_cms_document_content.setDownloadListener(O2WebviewDownloadListener(this))
        web_view_cms_document_content.webChromeClient = webChromeClient
        web_view_cms_document_content.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (view != null) {
                    imgReset(view) // 处理图片过大的问题
                    XLog.debug("处理了大图了？？？？？？？？？？？？")
                }
            }
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                XLog.error("ssl error, $error")
                handler?.proceed()
            }
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                XLog.info("shouldOverrideUrlLoading:$url")
                if (StringUtil.isImgUrl(url)) {
                    BigImageViewActivity.startInternetImageUrl(this@CMSWebViewActivity, url)
                } else {
                    view?.loadUrl(url)
                }
//                if (ZoneUtil.checkUrlIsInner(url)) {
//                    view?.loadUrl(url)
//                } else {
//                    AndroidUtils.runDefaultBrowser(this@CMSWebViewActivity, url)
//                }
                return true
            }

        }
        web_view_cms_document_content.webViewSetCookie(this, url)
        web_view_cms_document_content.loadUrl(url)

        // 标题
        webChromeClient.onO2ReceivedTitle = { title ->
            XLog.info("设置标题 $title")
            updateToolbarTitle(title)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(webChromeClient.onActivityResult(requestCode, resultCode, data)){
            return
        }
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
//                UPLOAD_REQUEST_CODE ->{
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        //上传附件
//                        mPresenter.uploadAttachment(result!!, site, docId, "")
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
//                UPLOAD_DATAGRID_REQUEST_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        //上传附件
//                        mPresenter.uploadAttachment(result!!, site, docId, datagridParam)
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
//                REPLACE_REQUEST_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        //替换附件
//                        mPresenter.replaceAttachment(result!!, site, attachmentId, docId, "")
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
//                REPLACE_DATAGRID_REQUEST_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:$result")
//                        showLoadingDialog()
//                        //替换附件
//                        mPresenter.replaceAttachment(result!!, site, attachmentId, docId, datagridParam)
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }

                TAKE_FROM_CAMERA_CODE -> {
                    //拍照
                    XLog.debug("拍照//// ")
//                    uploadImage2FileStorageStart(FileExtensionHelper.getCameraCacheFilePath(this))
                    if (!TextUtils.isEmpty(cameraImagePath)) {
                        uploadImage2FileStorageStart(cameraImagePath!!)
                    }
                }
            }
        }
    }

    override fun finishLoading() {
        hideLoadingDialog()
    }

    override fun uploadMaxFiles() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_upload_file_max_number))
    }

    override fun uploadAttachmentSuccess(attachmentId: String, site: String, datagridParam: String) {
        XLog.debug("uploadAttachmentResponse attachmentId:$attachmentId, site:$site")
        hideLoadingDialog()
        if (TextUtils.isEmpty(datagridParam)) {
            web_view_cms_document_content.evaluateJavascript("layout.appForm.uploadedAttachment(\"$site\", \"$attachmentId\")") { value ->
                XLog.debug("uploadedAttachment， onReceiveValue value=$value")
            }
        } else {
            web_view_cms_document_content.evaluateJavascript("layout.appForm.uploadedAttachmentDatagrid(\"$site\", \"$attachmentId\", \"$datagridParam\")") { value ->
                XLog.debug("uploadedAttachment， onReceiveValue value=$value")
            }
        }
    }

    override fun replaceAttachmentSuccess(attachmentId: String, site: String, datagridParam: String) {
        XLog.debug("replaceAttachmentResponse attachmentId:$attachmentId, site:$site")
        hideLoadingDialog()
        if (TextUtils.isEmpty(datagridParam)) {
            web_view_cms_document_content.evaluateJavascript("layout.appForm.replacedAttachment(\"$site\", \"$attachmentId\")") { value ->
                XLog.debug("replacedAttachment， onReceiveValue value=$value")
            }
        } else {
            web_view_cms_document_content.evaluateJavascript("layout.appForm.replacedAttachment(\"$site\", \"$attachmentId\", \"$datagridParam\")") { value ->
                XLog.debug("replacedAttachment， onReceiveValue value=$value")
            }
        }
    }

    override fun downloadAttachmentSuccess(file: File) {
        hideLoadingDialog()
        if (file.exists()){
            if (FileExtensionHelper.isImageFromFileExtension(file.extension)) {
                BigImageViewActivity.startLocalFile(this, file.absolutePath)
            }else {
                AndroidUtils.openFileWithDefaultApp(this, file)
            }
        }
    }

    override fun downloadAttachmentFail(message: String) {
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
            val json = O2SDKManager.instance().gson.toJson(imageUploadData)
            val js = "$callback('$json')"
            XLog.debug("执行js:$js")
            web_view_cms_document_content.evaluateJavascript(js){
                value -> XLog.debug("replacedAttachment， onReceiveValue value=$value")
            }
        }else {
            XLog.error("图片控件对象不存在。。。。。。。。")
        }
    }

    //MARK: 操作按钮
    /**
     * 发布文档
     */
    fun publishDocument(view: View?) {
        web_view_cms_document_content.evaluateJavascript("layout.appForm.publishDocument()") { value ->
            XLog.info("发布文档返回：$value")
        }
    }

    /**
     * 删除文档
     */
    fun deleteDocument(view: View?) {
        O2DialogSupport.openConfirmDialog(this, "你确定要删除当前文档？", listener = {
            web_view_cms_document_content.evaluateJavascript("layout.appForm.deleteDocumentForMobile()") { value ->
                XLog.info("删除文档返回：$value")
            }
        })
    }

    /**
     * 编辑文档
     * 点击把文档从阅读表单变成编辑表单
     */
    fun editDocument(view: View?) {
        web_view_cms_document_content.evaluateJavascript("layout.appForm.editDocumentForMobile()") { value ->
            XLog.info("转成编辑表单，返回：$value")
        }
    }

    /**
     * 保存文档
     *
     */
    fun saveDocument(view: View?) {
        web_view_cms_document_content.evaluateJavascript("layout.appForm.saveDocument(layout.close)") { value ->
            XLog.info("保存表单，返回：$value")
        }
    }


    //MARK: javascript interface

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
                        "closeDocumentWindow" -> runOnUiThread { finish() }
                        "openO2CmsDocument" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2OpenCmsDocMessage>>() {}.type
                            val value: O2JsPostMessage<O2OpenCmsDocMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data?.docId)) {
                                var options = ""
                                if (value.data!!.options != null) {
                                    options = gson.toJson(value.data!!.options!!)
                                }
                                openO2CmsDocumentV2(value.data!!.docId!!, value.data!!.title ?: "", options)
                            } else {
                                XLog.error("openO2CmsDocumentV2 参数不正确，缺少docId，无法打开")
                            }
                        }
                        "openO2WorkSpace" -> {
                            val type = object : TypeToken<O2JsPostMessage<O2OpenTaskCenterMessage>>() {}.type
                            val value: O2JsPostMessage<O2OpenTaskCenterMessage> = gson.fromJson(message, type)
                            if (value.data != null && !TextUtils.isEmpty(value.data!!.type)) {
                                openO2WorkSpace(value.data!!.type)
                            }else {
                                XLog.error("openO2WorkSpace 参数不正确，缺少type，无法打开")
                            }
                        }
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
                        "recordVoice" -> {
                            runOnUiThread {
                                //先检查录音权限
                                PermissionRequester(this)
                                    .request(Manifest.permission.RECORD_AUDIO)
                                    .o2Subscribe {
                                        onNext { (granted, _, _) ->
                                            if (!granted) {
                                                O2DialogSupport.openAlertDialog(this@CMSWebViewActivity, getString(R.string.dialog_msg_audio_need_permission), {
                                                    AndroidUtils.gotoSettingApplication(this@CMSWebViewActivity)
                                                })
                                            } else {
                                                val dialog = RecordVoiceFragment()
                                                dialog.setResultListener(object :
                                                    RecordVoiceFragment.OnBackRecordResultListener {
                                                    override fun onBack(
                                                        voiceFilePath: String,
                                                        voiceDuringTime: Long
                                                    ) {
                                                        XLog.debug("结果： $voiceFilePath $voiceDuringTime")
                                                    }
                                                })
                                                dialog.show(supportFragmentManager, "recordVoice")
                                            }
                                        }
                                        onError { e, _ ->
                                            XLog.error("", e)
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
    /**
     * 关闭当前窗口
     */
    @JavascriptInterface
    fun closeDocumentWindow(result: String) {
        XLog.debug("关闭文档 closeDocumentWindow ：$result")
        finish()
    }

    /**
     * 表单加载完成后回调
     */
    @JavascriptInterface
    fun cmsFormLoaded(control: String) {
        XLog.debug("表单加载完成回调：$control")
//        if (!TextUtils.isEmpty(control)) {
//            try {
//                val cmsWorkControl = O2SDKManager.instance().gson.fromJson(control, CMSWorkControl::class.java)
//                runOnUiThread {
//                    var i = 0
//                    if (cmsWorkControl.allowDeleteDocument) {
//                        tv_cms_form_delete_btn.visible()
//                        i++
//                    }
//                    if (cmsWorkControl.allowPublishDocument) {
//                        tv_cms_form_publish_btn.visible()
//                        i++
//                    }else {
//                        if (cmsWorkControl.allowEditDocument && cmsWorkControl.allowSave) {
//                            if (cmsWorkControl.currentMode == "read") {
//                                tv_cms_form_edit_btn.visible()
//                                tv_cms_form_save_btn.gone()
//                            }else if (cmsWorkControl.currentMode == "edit") {
//                                tv_cms_form_edit_btn.gone()
//                                tv_cms_form_save_btn.visible()
//                            }
//                            i++
//                        }else {
//                            tv_cms_form_edit_btn.gone()
//                            tv_cms_form_save_btn.gone()
//                        }
//                    }
//                    if (i>0) {
//                        fl_bottom_operation_bar.visible()
//                        bottom_operate_button_layout.visible()
//                    }
//                }
//            } catch (e: Exception) {
//                XLog.error("json parse error", e)
//            }
//        }

    }


    /**
     * 新版 打开内容管理 添加第三个参数 options
     */
    @JavascriptInterface
    fun openO2CmsDocumentV2(docId: String, docTitle: String, options: String?) {
        XLog.debug("openO2CmsDocument new : $docId, $docTitle $options")
        go<CMSWebViewActivity>(CMSWebViewActivity.startBundleDataWithOptions(docId, docTitle, options))
    }
    @JavascriptInterface
    fun openO2Meeting(result: String) {
        XLog.debug("openO2Meeting rrrrrrrrrrr")
        go<MeetingMainActivity>()
    }

    @JavascriptInterface
    fun openO2Calendar(result: String) {
        XLog.debug("openO2Calendarvvvvvvvvvvvvvvv")
        go<CalendarMainActivity>()
    }

    @JavascriptInterface
    fun openO2WorkSpace(type: String) {
        XLog.info("open work space $type")
        when (type.lowercase(Locale.getDefault())) {
            "task" -> go<TaskListActivity>()
            "taskcompleted" -> go<TaskCompletedListActivity>()
            "read" -> go<ReadListActivity>()
            "readcompleted" -> go<ReadCompletedListActivity>()
            else -> go<TaskListActivity>()
        }
    }

    /**
     * 上传附件
     *
     * @param site
     */
    @JavascriptInterface
    fun uploadAttachment(site: String) {
        XLog.debug("upload site:$site")
        if (TextUtils.isEmpty(site)) {
            XLog.error("没有传入site")
            return
        }
        this.site = site
        runOnUiThread {
//            openFancyFilePicker(UPLOAD_REQUEST_CODE)
            showAttachmentUploadMenu(UPLOAD_REQUEST_CODE, true)
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
        this.site = site
        this.datagridParam = param
        runOnUiThread {
//            openFancyFilePicker(UPLOAD_DATAGRID_REQUEST_CODE)
            showAttachmentUploadMenu(UPLOAD_DATAGRID_REQUEST_CODE, true)
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
        XLog.debug("replace site:$site, attachmentId:$attachmentId")
        if (TextUtils.isEmpty(attachmentId) || TextUtils.isEmpty(site)) {
            XLog.error("没有传入attachmentId 或 site")
            return
        }
        this.site = site
        this.attachmentId = attachmentId
        runOnUiThread {
//            openFancyFilePicker(REPLACE_REQUEST_CODE)
            showAttachmentUploadMenu(REPLACE_REQUEST_CODE, false)
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
        this.site = site
        this.attachmentId = attachmentId
        this.datagridParam = param
        runOnUiThread {
//        openFancyFilePicker(REPLACE_DATAGRID_REQUEST_CODE)
            showAttachmentUploadMenu(REPLACE_DATAGRID_REQUEST_CODE, false)
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

        //下载附件
        mPresenter.downloadAttachment(attachmentId, docId)
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
        //打开文档
        downloadDocument.downloadDocumentAndOpenIt(url) {
            hideLoadingDialog()
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
                imageUploadData = O2SDKManager.instance().gson.fromJson(json, O2UploadImageData::class.java)
                showPictureChooseMenu()

            }else {
                XToast.toastShort(this, "没有传入对象")
            }
        }
    }


    //MARK: private method


    /**
     * 附件上传菜单
     */
    private fun showAttachmentUploadMenu(requestCode: Int, multiple: Boolean) {
        BottomSheetMenu(this)
            .setTitle(getString(R.string.upload_attachment))
            .setItem(getString(R.string.choose_from_files), ContextCompat.getColor(this, R.color.z_color_text_primary)) {
                openFancyFilePicker(requestCode, multiple)
            }
            .setItem(getString(R.string.take_photo), ContextCompat.getColor(this, R.color.z_color_text_primary)) {
                takeFromCamera(requestCode)
            }
            .setItem(getString(R.string.record_voice), ContextCompat.getColor(this, R.color.z_color_text_primary)) {
                recordVoiceForAttachment(requestCode)
            }
            .setCancelButton(getString(R.string.cancel), ContextCompat.getColor(this, R.color.z_color_text_hint)) {
                XLog.debug("取消。。。。。")
            }
            .show()
    }

    // 录音
    private fun recordVoiceForAttachment(requestCode: Int) {
        runOnUiThread {
            //先检查录音权限
            PermissionRequester(this)
                .request(Manifest.permission.RECORD_AUDIO)
                .o2Subscribe {
                    onNext { (granted, _, _) ->
                        if (!granted) {
                            O2DialogSupport.openAlertDialog(this@CMSWebViewActivity, getString(R.string.dialog_msg_audio_need_permission), {
                                AndroidUtils.gotoSettingApplication(this@CMSWebViewActivity)
                            })
                        } else {
                            val dialog = RecordVoiceFragment()
                            dialog.setResultListener(object :
                                RecordVoiceFragment.OnBackRecordResultListener {
                                override fun onBack(
                                    voiceFilePath: String,
                                    voiceDuringTime: Long
                                ) {
                                    XLog.debug("结果： $voiceFilePath $voiceDuringTime")
                                    fileResultUploadForAttachment(requestCode, arrayListOf(voiceFilePath))
                                }
                            })
                            dialog.show(supportFragmentManager, "recordVoice")
                        }
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                    }
                }
        }
    }

    private fun openFancyFilePicker(requestCode: Int, multiple: Boolean) {

        PicturePickUtil().withAction(this)
            .setMode(PickTypeMode.FileWithMedia)
            .allowMultiple(multiple)
            .forResult { files ->
                fileResultUploadForAttachment(requestCode, files ?: ArrayList())
            }
    }

    private fun fileResultUploadForAttachment(requestCode: Int, files: ArrayList<String>) {
        if (files.isNotEmpty()) {
            when(requestCode) {
                UPLOAD_REQUEST_CODE ->{
                    showLoadingDialog()
                    //上传附件
                    mPresenter.uploadAttachment(files, site, docId, "")
                }
                UPLOAD_DATAGRID_REQUEST_CODE -> {
                    showLoadingDialog()
                    //上传附件
                    mPresenter.uploadAttachment(files, site, docId, datagridParam)
                }
                REPLACE_REQUEST_CODE -> {
                    val result = files[0]
                    if (!TextUtils.isEmpty(result)) {
                        XLog.debug("uri path:$result")
                        showLoadingDialog()
                        //替换附件
                        mPresenter.replaceAttachment(result, site, attachmentId, docId, "")
                    } else {
                        XLog.error("FilePicker 没有返回值！")
                    }
                }
                REPLACE_DATAGRID_REQUEST_CODE -> {
                    val result = files[0]
                    if (!TextUtils.isEmpty(result)) {
                        XLog.debug("uri path:$result")
                        showLoadingDialog()
                        //替换附件
                        mPresenter.replaceAttachment(result, site, attachmentId, docId, datagridParam)
                    } else {
                        XLog.error("FilePicker 没有返回值！")
                    }
                }
            }
        }
    }

    private fun showPictureChooseMenu() {
        BottomSheetMenu(this)
                .setTitle("上传照片")
                .setItem("从相册选择", ContextCompat.getColor(this, R.color.z_color_text_primary)) {
                    takeFromPictures()
                }
                .setItem("拍照", ContextCompat.getColor(this, R.color.z_color_text_primary)) {
                    takeFromCamera(TAKE_FROM_CAMERA_CODE)
                }
                .setCancelButton("取消", ContextCompat.getColor(this, R.color.z_color_text_hint)) {
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

    private fun takeFromCamera(requestCode: Int) {
        PermissionRequester(this).request(Manifest.permission.CAMERA)
                .o2Subscribe {
                    onNext { (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
                        XLog.info("granted:$granted , shouldShowRequest:$shouldShowRequestPermissionRationale, denied:$deniedPermissions")
                        if (!granted) {
                            O2DialogSupport.openAlertDialog(this@CMSWebViewActivity, "非常抱歉，相机权限没有开启，无法使用相机！")
                        } else {
                            openCamera(requestCode)
                        }
                    }
                }
    }

    private fun openCamera(requestCode: Int) {
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
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
        }
    }

    private fun uploadImage2FileStorageStart(filePath: String) {
        showLoadingDialog()
        if (imageUploadData != null) {
            mPresenter.upload2FileStorage(filePath, imageUploadData!!.referencetype, imageUploadData!!.reference)
        }else {
            finishLoading()
            XToast.toastShort(this, "上传文件参数为空！！！")
        }
    }
}
