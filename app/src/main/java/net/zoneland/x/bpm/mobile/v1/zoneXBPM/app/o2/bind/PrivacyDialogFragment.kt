package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.bind

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_privacy.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2App
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.O2WebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog

/**
 * Created by fancyLou on 2022-10-08.
 * Copyright © 2022 o2android. All rights reserved.
 */
class PrivacyDialogFragment: DialogFragment() {

    interface OnClickBtnListener {
        fun onclick(isAgree: Boolean)
    }

    private var onClickBtnListener: OnClickBtnListener? = null

    fun setOnClickBtnListener(listener: OnClickBtnListener) {
        this.onClickBtnListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dWin = dialog?.window
        dWin?.setDimAmount(0.8f)
        val lp = dWin?.attributes
        lp?.gravity = Gravity.CENTER
        dWin?.attributes = lp
        return inflater.inflate(R.layout.dialog_fragment_privacy, container, false)
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val style = SpannableStringBuilder()
        //设置文字
        val text = getString(R.string.user_privacy_dialog_2)
        style.append(text)
        // 《用户协议》 点击
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                XLog.debug("点击了 用户协议")
                openUserPrivacy()
            }
        }
        style.setSpan(clickableSpan, 10, 16, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val foregroundColorSpan =  ForegroundColorSpan(FancySkinManager.instance().getColor(view.context, R.color.z_color_primary_blue))
        style.setSpan(foregroundColorSpan, 10, 16, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        // 《隐私政策》
        val clickableSpan2 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                XLog.debug("点击了 隐私政策")
                openSecret()
            }
        }
        style.setSpan(clickableSpan2, 17, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        style.setSpan(foregroundColorSpan, 17, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tv_dialog_user_privacy_second.text = style
        tv_dialog_user_privacy_second.movementMethod = LinkMovementMethod.getInstance()

        btn_o2_dialog_negative.setOnClickListener {
            O2App.instance.agreePrivacyAndInitJpush(false)
            onClickBtnListener?.onclick(false)
            dismissAllowingStateLoss()
        }
        btn_o2_dialog_positive.setOnClickListener {
            O2App.instance.agreePrivacyAndInitJpush(true)
            onClickBtnListener?.onclick(true)
            dismissAllowingStateLoss()
        }
    }


    private fun openSecret() {
        activity?.let {
            O2WebViewActivity.openWebView(it, getString(R.string.secret), "https://www.o2oa.net/secret.html")
        }
    }
    private fun openUserPrivacy() {
        activity?.let {
            O2WebViewActivity.openWebView(it, getString(R.string.user_service), "https://www.o2oa.net/userService.html")
        }
    }
}