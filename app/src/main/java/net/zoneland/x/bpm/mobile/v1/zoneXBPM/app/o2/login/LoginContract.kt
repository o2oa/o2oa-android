package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.login

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.CaptchaImgData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.LoginModeData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.LoginWithCaptchaForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.WwwGetSampleAccounts
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.AuthenticationInfoJson

/**
 * Created by fancy on 2017/6/8.
 */

object LoginContract {
    interface View: BaseView {
        fun getCodeError()
        fun loginSuccess(data: AuthenticationInfoJson)
        fun loginFail()
        fun showCaptcha(data: CaptchaImgData)
        fun getCaptchaError(err: String)
        fun loginMode(mode: LoginModeData?)
        fun sampleServerAccounts(accounts: WwwGetSampleAccounts?)
    }

    interface Presenter: BasePresenter<View> {


        /**
         * 登录方式
         */
        fun getLoginMode()

        /**
         * 获取验证码
         * @param value 用户名或者手机号码
         */
        fun getVerificationCode(value: String)

        /**
         * 用户名或者手机号码 验证码 登录
         */
        fun login(userName: String, code: String)



        fun ssoLogin(userId: String)

        /**
         * 获取图片验证码
         */
        fun getCaptcha()


        /**
         * 图片验证码登录
         */
        fun loginWithCaptcha(form: LoginWithCaptchaForm)

        /**
         *
         */
        fun getRSAPublicKey()


        fun getSampleServerAccounts(serverId: String)

    }
}