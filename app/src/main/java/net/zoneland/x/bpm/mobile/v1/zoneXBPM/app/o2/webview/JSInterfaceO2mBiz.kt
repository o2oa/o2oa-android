package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview




import android.os.Bundle
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wugang.activityresult.library.ActivityResult
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer.BigImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.cache.MD5Util
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.O2WebviewDownloadListener.DownloadFileForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.LoadingDialog
import org.json.JSONObject
import org.json.JSONTokener
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File


class JSInterfaceO2mBiz  private constructor(val activity: FragmentActivity?) {
    companion object {
        const val JSInterfaceName = "o2mBiz"
        fun with(activity: FragmentActivity) = JSInterfaceO2mBiz(activity)
        fun with(fragment: Fragment) = JSInterfaceO2mBiz(fragment.activity)
    }

    private lateinit var webView: WebView
    private val gson: Gson by lazy { Gson() }

    fun setupWebView(webView: WebView) {
        this.webView = webView
    }


    @JavascriptInterface
    fun postMessage(message: String?) {
        if (!TextUtils.isEmpty(message)) {
            XLog.debug(message)
            try {
                val json = JSONTokener(message).nextValue()
                if (json is JSONObject) {
                    when (json.getString("type")) {
                        "contact.departmentPicker" -> departmentsPicker(message!!)
                        "contact.identityPicker" -> identityPicker(message!!)
                        "contact.groupPicker" -> groupPicker(message!!)
                        "contact.personPicker" -> personPicker(message!!)
                        "contact.complexPicker" -> complexPicker(message!!)
                        "file.previewDoc" -> previewDoc(message!!)
                    }
                } else {
                    XLog.error("message 格式错误！！！")
                }
            } catch (e: Exception) {
                XLog.error("", e)
            }
        } else {
            XLog.error("o2mBiz.postMessage error, 没有传入message内容！")
        }
    }

    private fun callbackJs(js: String) {
        if (::webView.isInitialized && !TextUtils.isEmpty(js)) {
            activity?.runOnUiThread {
                XLog.debug("执行js：$js")
                webView.evaluateJavascript(js) { value ->
                    XLog.debug("js执行完成, result:$value")
                }
            }
        } else {
            XLog.error("没有注入webView，无法执行回调函数！！！！")
        }
    }

    /**
     * 下载并预览文件
     */
    private fun previewDoc(message: String) {
        val type = object : TypeToken<O2JsPostMessage<O2BizPreviewDocMessage>>() {}.type
        val value: O2JsPostMessage<O2BizPreviewDocMessage> = gson.fromJson(message, type)
        val callback = value.callback
        if (activity != null) {
            val url = value.data?.url
            val fileName = value.data?.fileName
            if (TextUtils.isEmpty(url) || TextUtils.isEmpty(fileName)) {
                XLog.error("没有传入 url 或 fileName！")
                if (!TextUtils.isEmpty(callback)) {
                    callbackJs("$callback('{\"result\": false, \"message\": \"没有传入url 或 fileName！\"}')")
                }
            } else {
                // 防止文件名相同，md5 url作为文件夹名称
                val dirUrl = MD5Util.getMD5String(url) ?: "null"
                val path = FileExtensionHelper.getXBPMTempFolder(activity) + File.separator + dirUrl + File.separator + fileName
                showLoadingDialog()
                Observable.just(DownloadFileForm(url!!, path))
                    .subscribeOn(Schedulers.io())
                    .flatMap { form ->
                        O2FileDownloadHelper.download(form.downloadUrl, form.filePath)
                    }.observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext {
                            hideLoadingDialog()
                            openFile(path)
                            if (!TextUtils.isEmpty(callback)) {
                                callbackJs("$callback('{\"result\": true, \"message\": \"\"}')")
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            hideLoadingDialog()
                            val mes = "下载文件失败！${e?.message ?: ""}"
                            activity.runOnUiThread {XToast.toastShort(activity, mes)}
                            if (!TextUtils.isEmpty(callback)) {
                                callbackJs("$callback('{\"result\": false, \"message\": \"$mes\"}')")
                            }
                        }
                    }
            }
        } else {
            XLog.error("activity不存在 previewDoc 失败！！")
        }
    }


    private var loadingDialog: LoadingDialog? = null
    private fun showLoadingDialog() {
        activity?.runOnUiThread {
            if (loadingDialog == null) {
                loadingDialog = LoadingDialog(activity)
            }
            loadingDialog?.show()
        }
    }
    private fun hideLoadingDialog() {
        activity?.runOnUiThread {
            loadingDialog?.dismiss()
        }
    }
    private fun openFile(filePath: String) {
        val file = File(filePath)
        activity?.runOnUiThread {
            if (file.exists()) {
                if (FileExtensionHelper.isImageFromFileExtension(file.extension)) {
                    BigImageViewActivity.startLocalFile(activity, file.absolutePath)
                } else {
                    AndroidUtils.openFileWithDefaultApp(activity, file)
                }
            }
        }
    }

    /**
     * 身份选择器
     */
    private fun identityPicker(message: String) {
        val type = object : TypeToken<O2JsPostMessage<O2BizIdentityPickerMessage>>() {}.type
        val value: O2JsPostMessage<O2BizIdentityPickerMessage> = gson.fromJson(message, type)
        val callback = value.callback
        if (activity != null) {
            val jsFormData = value.data
            val bundle  = if (jsFormData != null) {
                val dutyList = jsFormData.duty ?: ArrayList()
                val topList = jsFormData.topList ?: ArrayList()
                val multiple = jsFormData.multiple ?: true
                val maxNumber = jsFormData.maxNumber ?: 0
                val pickedIdentities = jsFormData.pickedIdentities ?: ArrayList()
                ContactPickerActivity.startPickerBundle(
                        arrayListOf(ContactPickerActivity.identityPicker),
                        dutyList = dutyList,
                        multiple = multiple,
                        topUnitList = topList,
                        maxNumber = maxNumber,
                        unitType = "",
                        initIdList = pickedIdentities
                )
            }else {
                ContactPickerActivity.startPickerBundle(arrayListOf(ContactPickerActivity.identityPicker))
            }
            showPicker(bundle, callback)
        } else {
            XLog.error("activity不存在 identityPicker 失败！！")
        }
    }

    /**
     * 部门选择器
     */
    private fun departmentsPicker(message: String) {
        val type = object : TypeToken<O2JsPostMessage<O2BizUnitPickerMessage>>() {}.type
        val value: O2JsPostMessage<O2BizUnitPickerMessage> = gson.fromJson(message, type)
        val callback = value.callback
        if (activity != null) {
            val jsFormData = value.data
            val bundle  = if (jsFormData != null) {
                val orgType = jsFormData.orgType ?: ""
                val topList = jsFormData.topList ?: ArrayList()
                val multiple = jsFormData.multiple ?: true
                val maxNumber = jsFormData.maxNumber ?: 0
                val pickedDepartments = jsFormData.pickedDepartments ?: ArrayList()
                ContactPickerActivity.startPickerBundle(
                        arrayListOf(ContactPickerActivity.departmentPicker),
                        dutyList = arrayListOf(),
                        multiple = multiple,
                        topUnitList = topList,
                        maxNumber = maxNumber,
                        unitType = orgType,
                        initDeptList = pickedDepartments
                )
            }else {
                ContactPickerActivity.startPickerBundle(arrayListOf(ContactPickerActivity.departmentPicker))
            }
            showPicker(bundle, callback)
        } else {
            XLog.error("activity不存在 departmentsPicker 失败！！")
        }
    }

    /**
     * 人员选择器
     */
    private fun personPicker(message: String) {
        val type = object : TypeToken<O2JsPostMessage<O2BizPersonPickerMessage>>() {}.type
        val value: O2JsPostMessage<O2BizPersonPickerMessage> = gson.fromJson(message, type)
        val callback = value.callback
        if (activity != null) {
            val jsFormData = value.data
            val bundle  = if (jsFormData != null) {
                val multiple = jsFormData.multiple ?: true
                val maxNumber = jsFormData.maxNumber ?: 0
                val pickedPersonList = jsFormData.pickedUsers ?: ArrayList()
                ContactPickerActivity.startPickerBundle(
                        arrayListOf(ContactPickerActivity.personPicker),
                        dutyList = arrayListOf(),
                        multiple = multiple,
                        topUnitList = arrayListOf(),
                        maxNumber = maxNumber,
                        unitType = "",
                        initUserList = pickedPersonList
                )
            }else {
                ContactPickerActivity.startPickerBundle(arrayListOf(ContactPickerActivity.personPicker))
            }
           showPicker(bundle, callback)
        } else {
            XLog.error("activity不存在 personPicker 失败！！")
        }
    }

    /**
     * 群组选择器
     */
    private fun groupPicker(message: String) {
        val type = object : TypeToken<O2JsPostMessage<O2BizGroupPickerMessage>>() {}.type
        val value: O2JsPostMessage<O2BizGroupPickerMessage> = gson.fromJson(message, type)
        val callback = value.callback
        if (activity != null) {
            val jsFormData = value.data
            val bundle  = if (jsFormData != null) {
                val multiple = jsFormData.multiple ?: true
                val maxNumber = jsFormData.maxNumber ?: 0
                val pickedGroups = jsFormData.pickedGroups ?: ArrayList()
                ContactPickerActivity.startPickerBundle(
                        arrayListOf(ContactPickerActivity.groupPicker),
                        dutyList = arrayListOf(),
                        multiple = multiple,
                        topUnitList = arrayListOf(),
                        maxNumber = maxNumber,
                        unitType = "",
                        initGroupList = pickedGroups
                )
            }else {
                ContactPickerActivity.startPickerBundle(arrayListOf(ContactPickerActivity.groupPicker))
            }
            showPicker( bundle, callback)
        } else {
            XLog.error("activity不存在 groupPicker 失败！！")
        }
    }

    /**
     * 组合选择器
     */
    private fun complexPicker(message: String) {
        val type = object : TypeToken<O2JsPostMessage<O2BizComplexPickerMessage>>() {}.type
        val value: O2JsPostMessage<O2BizComplexPickerMessage> = gson.fromJson(message, type)
        val callback = value.callback
        if (activity != null) {
            val jsFormData = value.data
            val bundle  = if (jsFormData != null) {
                val pickMode = jsFormData.pickMode ?: ArrayList()
                val dutyList = jsFormData.duty ?: ArrayList()
                val topList = jsFormData.topList ?: ArrayList()
                val multiple = jsFormData.multiple ?: true
                val maxNumber = jsFormData.maxNumber ?: 0
                val orgType = jsFormData.orgType ?: ""
                val pickedGroups = jsFormData.pickedGroups ?: ArrayList()
                val pickedDepartments = jsFormData.pickedDepartments ?: ArrayList()
                val pickedIdentities = jsFormData.pickedIdentities ?: ArrayList()
                val pickedUsers = jsFormData.pickedUsers ?: ArrayList()
                ContactPickerActivity.startPickerBundle(
                        pickMode,
                        dutyList = dutyList,
                        multiple = multiple,
                        topUnitList = topList,
                        maxNumber = maxNumber,
                        unitType = orgType,
                        initDeptList = pickedDepartments,
                        initGroupList = pickedGroups,
                        initIdList = pickedIdentities,
                        initUserList = pickedUsers
                )
            }else {
                ContactPickerActivity.startPickerBundle(ArrayList())
            }
            showPicker( bundle, callback)
        } else {
            XLog.error("activity不存在 complexPicker 失败！！")
        }
    }

    private fun showPicker(bundle: Bundle, callback: String?) {
        activity!!.runOnUiThread {
            ActivityResult.of(activity)
                    .className(ContactPickerActivity::class.java)
                    .params(bundle)
                    .greenChannel().forResult { _, data ->
                        val result = data?.getParcelableExtra<ContactPickerResult>(ContactPickerActivity.CONTACT_PICKED_RESULT)
                        if (result != null) {
                            if (!TextUtils.isEmpty(callback)) {
                                val resultJson = gson.toJson(result)
                                XLog.debug("返回json:$resultJson")
                                callbackJs("$callback('$resultJson')")
                            }
                        }
                    }
        }
    }

}