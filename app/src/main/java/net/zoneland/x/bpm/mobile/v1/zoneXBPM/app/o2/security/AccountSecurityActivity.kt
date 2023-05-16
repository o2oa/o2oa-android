package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security


import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_account_security.account_change_mobile_id
import kotlinx.android.synthetic.main.activity_account_security.account_change_mobile_label_id
import kotlinx.android.synthetic.main.activity_account_security.account_name_id
import kotlinx.android.synthetic.main.activity_account_security.image_btn_account_security_biometry_enable
import kotlinx.android.synthetic.main.activity_account_security.ll_account_security_bind_device_layout
import kotlinx.android.synthetic.main.activity_account_security.ll_account_security_empower_btn
import kotlinx.android.synthetic.main.activity_account_security.rl_account_security_bind_device_btn
import kotlinx.android.synthetic.main.activity_account_security.rl_account_security_name_btn
import kotlinx.android.synthetic.main.activity_account_security.rl_account_security_password_btn
import kotlinx.android.synthetic.main.activity_account_security.tv_account_security_biometry_name
import kotlinx.android.synthetic.main.activity_account_security.tv_account_security_unit_name
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.BuildConfig
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.bind.BindPhoneActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.my.MyInfoActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.biometric.BioConstants
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.biometric.BiometryManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.biometric.OnBiometryAuthCallback
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.goAndClearBefore
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.inVisible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport


class AccountSecurityActivity : BaseMVPActivity<AccountSecurityContract.View, AccountSecurityContract.Presenter>(), AccountSecurityContract.View {
    override var mPresenter: AccountSecurityContract.Presenter = AccountSecurityPresenter()
    override fun layoutResId(): Int  = R.layout.activity_account_security
    override fun afterSetContentView(savedInstanceState: Bundle?) {
        mPresenter.getRSAPublicKey()
        setupToolBar(getString(R.string.title_activity_account_security), true)

        account_name_id.text = O2SDKManager.instance().cName
        account_change_mobile_label_id.text = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_PHONE_KEY, "")

        if (BuildConfig.InnerServer) {
            account_change_mobile_id.inVisible()
        }else {
            account_change_mobile_id.visible()
            account_change_mobile_id.setOnClickListener { changeMobile() }
        }

        rl_account_security_name_btn.setOnClickListener {
            go<MyInfoActivity>()
        }

        rl_account_security_password_btn.setOnClickListener {
            changeMyPassword()
        }
        ll_account_security_empower_btn.setOnClickListener {
            go<EmpowerListActivity>()
        }

        val unitHost = O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HOST_KEY, "")
        tv_account_security_unit_name.text = getString(R.string.setting_bind_server_host, unitHost)

        initBiometryAuthView()


        if (BuildConfig.InnerServer) {
            ll_account_security_bind_device_layout.gone()
        }else {
            ll_account_security_bind_device_layout.visible()
            rl_account_security_bind_device_btn.setOnClickListener {
                go<DeviceManagerActivity>()
            }
        }

    }

    override fun logoutSuccess() {
        O2SDKManager.instance().logoutCleanCurrentPerson()
        O2SDKManager.instance().clearBindUnit()
        goAndClearBefore<BindPhoneActivity>()
    }

    override fun updateMyPasswordFail(message: String) {
        XToast.toastLong(this, message)
    }

    override fun updateMyPasswordSuccess() {
        XToast.toastShort(this, getString(R.string.message_setting_update_password_success))
    }

    private var bioManager: BiometryManager?  = null

    private fun initBiometryAuthView() {

        try {
            bioManager = BiometryManager(this)
        }catch (e: Exception) {
            XLog.error("", e)
        }
        tv_account_security_biometry_name.text = getString(R.string.login_type_fingerprint)
        val bioAuthUser = O2SDKManager.instance().prefs().getString(BioConstants.O2_bio_auth_user_id_prefs_key, "") ?: ""
        var isAuthed = false
        //判断是否当前绑定的服务器的
        if (bioAuthUser.isNotBlank()) {
            val array = bioAuthUser.split("^^")
            if (array.isNotEmpty() && array.size == 2) {
                val unitId = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_UNIT_ID_KEY, "") ?: ""
                if (array[0] == unitId) {
                    isAuthed = true
                }
            }
        }

        if (isAuthed) {
            image_btn_account_security_biometry_enable.setImageResource(R.mipmap.icon_toggle_on_29dp)
        }else {
            image_btn_account_security_biometry_enable.setImageResource(R.mipmap.icon_toggle_off_29dp)
        }

        var isBioEnable = false
        try {
            isBioEnable = bioManager?.isBiometricPromptEnable() ?: false
        }catch (e: Exception) {
            XLog.error("",e)
        }

        if (isBioEnable) {
            image_btn_account_security_biometry_enable.setOnClickListener {
                bioManager?.authenticate(object : OnBiometryAuthCallback{
                    override fun onUseFallBack() {
                        XLog.error("点击了《其他方式》按钮。。。。。")
                    }

                    override fun onSucceeded() {
                        XLog.debug("指纹识别验证成功")
                        XToast.toastShort(this@AccountSecurityActivity, getString(R.string.biometric_dialog_state_succeeded))
                        setBioAuthResult()
                    }

                    override fun onFailed() {
                        XLog.error("指纹识别验证失败了。。。。。")
                        //XToast.toastShort(this@AccountSecurityActivity, "验证失败")
                    }

                    override fun onError(code: Int, reason: String) {
                        XLog.error("指纹识别验证出错，code:$code , reason:$reason")
                        //XToast.toastShort(this@AccountSecurityActivity, "验证失败，$reason")
                    }

                    override fun onCancel() {
                        XLog.info("指纹识别取消了。。。。。")
                    }

                })
            }

        }else {
            tv_account_security_biometry_name.text = getString(R.string.message_login_type_fingerprint_disabled)
            image_btn_account_security_biometry_enable.setImageResource(R.mipmap.icon_toggle_off_29dp)
            image_btn_account_security_biometry_enable.setOnClickListener {
                XToast.toastShort(this,  getString(R.string.message_login_type_fingerprint_disabled))
            }
        }
    }

    private fun changeMyPassword() {
        O2DialogSupport.openCustomViewDialog(this, getString(R.string.change_password), R.layout.dialog_password_modify) { dialog ->
            val old = dialog.findViewById<EditText>(R.id.dialog_password_old_edit_id).text.toString()
            if (TextUtils.isEmpty(old)) {
                XToast.toastShort(this@AccountSecurityActivity, getString(R.string.message_old_password_can_not_empty))
                return@openCustomViewDialog
            }
            val newpwd = dialog.findViewById<EditText>(R.id.dialog_password_new_edit_id).text.toString()
            if (TextUtils.isEmpty(newpwd)) {
                XToast.toastShort(this@AccountSecurityActivity, getString(R.string.message_new_password_can_not_empty))
                return@openCustomViewDialog
            }
            val newpwdAgain = dialog.findViewById<EditText>(R.id.dialog_password_confirm_edit_id).text.toString()
            if (newpwd != newpwdAgain) {
                XToast.toastShort(this@AccountSecurityActivity, getString(R.string.message_new_old_password_not_same))
                return@openCustomViewDialog
            }
            mPresenter.updateMyPassword(old, newpwd, newpwdAgain)
        }
    }

    //如果识别成功 设置结果
    private fun setBioAuthResult() {
        val bioAuthUser = O2SDKManager.instance().prefs().getString(BioConstants.O2_bio_auth_user_id_prefs_key, "") ?: ""
        var isAuthed = false
        val unitId = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_UNIT_ID_KEY, "") ?: ""
        //判断是否当前绑定的服务器的
        if (bioAuthUser.isNotBlank()) {
            val array = bioAuthUser.split("^^")
            if (array.isNotEmpty() && array.size == 2) {
                if (array[0] == unitId) {
                    isAuthed = true
                }
            }
        }
        val userId = if(isAuthed)  "" else unitId+"^^"+O2SDKManager.instance().distinguishedName

        O2SDKManager.instance().prefs().edit{
            putString(BioConstants.O2_bio_auth_user_id_prefs_key, userId)
        }
        if (isAuthed) {
            image_btn_account_security_biometry_enable.setImageResource(R.mipmap.icon_toggle_off_29dp)
        }else {
            image_btn_account_security_biometry_enable.setImageResource(R.mipmap.icon_toggle_on_29dp)
        }

    }


    private fun changeMobile() {
        O2DialogSupport.openConfirmDialog(this, getString(R.string.dialog_msg_need_rebind_phone_number), {
            val deviceId = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_PHONE_TOKEN_KEY, "") ?: ""
            mPresenter.logout(deviceId)
        })
    }



}
