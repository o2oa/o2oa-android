package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.scanlogin


import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_scan_login.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.RetrofitClient
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.AndroidUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.StringUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern


class ScanLoginActivity : BaseMVPActivity<ScanLoginContract.View, ScanLoginContract.Presenter>(), ScanLoginContract.View {
    override var mPresenter: ScanLoginContract.Presenter = ScanLoginPresenter()

    override fun layoutResId(): Int = R.layout.activity_scan_login

    companion object {
        val SCAN_RESULT_KEY = "scan_result_key"
    }

    private var result = ""
    private var meta = ""
    private var title = ""

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        result =  intent.extras?.getString(SCAN_RESULT_KEY) ?: ""
        if (TextUtils.isEmpty(result)) {
            XToast.toastShort(this, getString(R.string.message_can_not_scan_anything))
            finish()
            return
        }
        XLog.debug("scan result: $result")
        parseResult()

        setupToolBar(title)
        button_scan_login_confirm.setOnClickListener{ mPresenter.confirmWebLogin(meta) }
        tv_scan_login_cancel.setOnClickListener { finish() }
    }

    override fun confirmSuccess() {
        XToast.toastShort(this, getString(R.string.message_login_success))
        finish()
    }

    override fun confirmFail() {
        XToast.toastShort(this, getString(R.string.message_login_fail_scan))
        finish()
    }

    override fun checkInSuccess() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_login_success_sign))
        finish()
    }

    override fun checkInFail() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_login_fail_sign))
        finish()
    }

    private fun parseResult() {
        if (StringUtil.isUrl(result)) {
            parseMeta()
            if (!TextUtils.isEmpty(meta)) {
                title = getString(net.zoneland.x.bpm.mobile.v1.zoneXBPM.R.string.scan_login_confirm_title)
                activity_scan_login.visible()
                tv_scan_login_text_content.gone()
            }else if (result.contains("x_meeting_assemble_control") && result.contains("/checkin")){
                meetingCheckin(result)//会议签到
            }else{
                gotoDefaultBrowser()
            }
        }else{
            activity_scan_login.gone()
            tv_scan_login_text_content.text = result
            tv_scan_login_text_content.visible()
            title = getString(net.zoneland.x.bpm.mobile.v1.zoneXBPM.R.string.scan_login_title)
        }
    }

    // 2021-11-04 不直接执行这个url 可能是内网地址，无法访问
    // http://ip:20020/x_meeting_assemble_control/jaxrs/meeting/adf3c245-dbef-41ef-b323-dfb5fae4afb7/checkin 解析地址获取id 自行请求
    private fun  meetingCheckin(url: String) {
        XLog.debug("会议签到：$url")

        val id = getMeetingIdFromUrl(url)
        showLoadingDialog()
        mPresenter.checkInMeeting(id)

//        val request = Request.Builder().get().url(url).build()
//        val client = RetrofitClient.instance().getO2HttpClient()
//        if (client != null) {
//            val call = client.newCall(request)
//            call.enqueue(object : Callback{
//                override fun onFailure(call: Call, e: IOException) {
//                    XLog.error("", e)
//                    runOnUiThread {
//                        XToast.toastShort(this@ScanLoginActivity, getString(R.string.message_login_fail_sign))
//                        finish()
//                    }
//
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    val result = response.body()?.string()
//                    XLog.debug(result)
//                    runOnUiThread {
//                        XToast.toastShort(this@ScanLoginActivity, getString(R.string.message_login_success_sign))
//                        finish()
//                    }
//
//                }
//
//            })
//        }
    }

    /**
     * 截取url中的id
     */
    private fun getMeetingIdFromUrl( url: String): String  {
        var id = ""
        val purl: Pattern =
            Pattern.compile("x_meeting_assemble_control\\/jaxrs\\/meeting\\/(.*?)\\/checkin") //正则表达式

        val murl: Matcher = purl.matcher(url)
        if (murl.find()) {
            id = murl.group(1)
        }
        return id
    }

    private fun gotoDefaultBrowser() {
        AndroidUtils.runDefaultBrowser(this, result)
        finish()
    }

    private fun parseMeta() {
        try {
            val str = result.trim().toLowerCase()
            val array = str.split("?")
            XLog.debug("$array")
            if (array.size > 1) {
                val paramArray = array[1].split("&")
                paramArray.filter { "meta" == it.split("=")[0] }.map { meta = it.split("=")[1] }
            }
        }catch (e: Exception){XLog.error("", e)}
    }
}
