package net.zoneland.x.bpm.mobile.v1.zoneXBPM

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import cn.jiguang.api.utils.JCollectionAuth
import cn.jpush.android.api.JPushInterface
import com.baidu.mapapi.SDKInitializer
import com.zlw.main.recorderlib.RecordManager
import io.realm.Realm
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.skin.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.AndroidUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.LogSingletonService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.O2MediaPlayerManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit


/**
 * Created by fancy on 2017/6/5.
 */

class O2App : MultiDexApplication() {


    companion object {
        lateinit var instance: O2App
    }


    /**
     * baidu
     */
    val BAIDU_APP_ID: String by lazy {
        getAppMeta("com.baidu.speech.APP_ID")
    }
    val BAIDU_APP_KEY: String by lazy {
        getAppMeta("com.baidu.speech.API_KEY")
    }
    val BAIDU_SECRET_KEY: String by lazy {
        getAppMeta("com.baidu.speech.SECRET_KEY")
    }

    private val notificationList: ArrayList<Int> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        instance = this
        //sdk
        O2SDKManager.instance().init(this)
        //数据库
        Realm.init(this)
        //注册日志记录器
        LogSingletonService.instance().registerApp(this)
        //换肤插件
        FancySkinManager.instance().withoutActivity(this)
                .addSupportAttr("icon", IconChangeColorIconSkinAttr())
                .addSupportAttr("iconCompleted", IconCompletedChangeColorIconSkinAttr())
                .addSupportAttr("color", ColorChangeColorIconSkinAttr())
                .addSupportAttr("drawableLeft", DrawableLeftRadioButtonSkinAttr())
                .addSupportAttr("tabIndicatorColor", TabIndicatorColorTabLayoutSkinAttr())
                .addSupportAttr("backgroundTint", BackgroundTintFloatingActionButtonSkinAttr())
                .addSupportAttr("o2ButtonColor", O2ProgressButtonColorSkinAttr())

        //播放器
        O2MediaPlayerManager.instance().init(this)
        //录音
        RecordManager.getInstance().init(this, false)

        initThirdParty()

        Log.i("O2app", "O2app init.....................................................")
    }



    private fun initThirdParty() {
        val isAgree = O2SDKManager.instance().prefs().getBoolean(O2.PRE_APP_PRIVACY_AGREE_KEY, false)
        XLog.debug("init privacy isagree: $isAgree")
        if ( isAgree ) {
            try {

                SDKInitializer.setAgreePrivacy(this, true)
                SDKInitializer.initialize(applicationContext)
                //极光推送
                JCollectionAuth.setAuth(this, true)
                JPushInterface.init(this)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else {
            JCollectionAuth.setAuth(this, false)
        }
    }

    fun agreePrivacyAndInitThirdParty(isAgree: Boolean) {
        XLog.debug("set privacy isagree: $isAgree")
        O2SDKManager.instance().prefs().edit {
            putBoolean(O2.PRE_APP_PRIVACY_AGREE_KEY, isAgree)
        }
        if (isAgree) {
            try {
                SDKInitializer.setAgreePrivacy(this, true)
                SDKInitializer.initialize(applicationContext)
                //极光推送
                JCollectionAuth.setAuth(this, true)
                JPushInterface.init(this)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }


//  获取Application下的meta值
    fun getAppMeta(metaName: String, default: String = "") : String {
        return try {
            if ("com.baidu.speech.APP_ID" == metaName) {
                val appid = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getInt("com.baidu.speech.APP_ID")
                ""+appid
            }else {
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString(metaName, default)
            }
        }catch (e: Exception) {
            Log.e("O2app", "", e)
            default
        }
    }


    private fun initJMessageAndJPush() {
        if (BuildConfig.InnerServer) {
            JCollectionAuth.setAuth(this, true)
            JPushInterface.init(this)
        } else {
            val isAgree = O2SDKManager.instance().prefs().getBoolean(O2.PRE_APP_PRIVACY_AGREE_KEY, false)
            XLog.debug("init jpush isagree: $isAgree")
//            if (isAgree) {
            if (!(AndroidUtils.isHuaweiChannel(this) && !isAgree )) {
                JCollectionAuth.setAuth(this, true)
                JPushInterface.init(this)
            }
        }
    }

    fun addNotification(nId: Int) {
        notificationList.add(nId)
        XLog.info("添加通知 $nId ！！！！！！")
    }

    fun clearAllNotification() {
        XLog.info("清除所有的通知！！！！！！")
        try {
            if (notificationList.isNotEmpty()) {
                notificationList.forEach {
                    JPushInterface.clearNotificationById(this, it)
                    XLog.info("清除通知：$it")
                }
            }
            // 清除通知 清除角标 这句写了好像角标不会再出现了
            JPushInterface.setBadgeNumber(this, 0)
        } catch (e: Exception) {
            XLog.error("", e)
        }
    }


}