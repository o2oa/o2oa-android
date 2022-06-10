package net.zoneland.x.bpm.mobile.v1.zoneXBPM

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import cn.jpush.android.api.JPushInterface
import com.baidu.mapapi.SDKInitializer
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.TbsListener
import com.zlw.main.recorderlib.RecordManager
import io.realm.Realm
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.skin.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.LogSingletonService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.O2MediaPlayerManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.tbs.WordReadHelper


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
        //换肤插件
        FancySkinManager.instance().withoutActivity(this)
                .addSupportAttr("icon", IconChangeColorIconSkinAttr())
                .addSupportAttr("iconCompleted", IconCompletedChangeColorIconSkinAttr())
                .addSupportAttr("color", ColorChangeColorIconSkinAttr())
                .addSupportAttr("drawableLeft", DrawableLeftRadioButtonSkinAttr())
                .addSupportAttr("tabIndicatorColor", TabIndicatorColorTabLayoutSkinAttr())
                .addSupportAttr("backgroundTint", BackgroundTintFloatingActionButtonSkinAttr())
                .addSupportAttr("o2ButtonColor", O2ProgressButtonColorSkinAttr())


        //baidu
        try {
            SDKInitializer.setAgreePrivacy(this, true)
            SDKInitializer.initialize(applicationContext)
            //bugly
            CrashReport.initCrashReport(applicationContext)
//            initTBS()
            WordReadHelper.init(this);
            //极光推送
            initJMessageAndJPush()
            //播放器
            O2MediaPlayerManager.instance().init(this)
//        Fresco.initialize(this)
            //注册日志记录器
            LogSingletonService.instance().registerApp(this)
            //录音
            RecordManager.getInstance().init(this, false)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.i("O2app", "O2app init.....................................................")
        //stetho developer tool
//        Stetho.initializeWithDefaults(this)
    }

    /**
     * 初始化腾讯TBS
     */
    private fun initTBS() {
        var map  = HashMap<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)

        QbSdk.setDownloadWithoutWifi(true)
        QbSdk.setTbsListener(object : TbsListener {
            override fun onDownloadFinish(progress: Int) {
                Log.d("QbSdk", "onDownloadFinish -->下载X5内核完成：$progress")
                //若是progress == 100 的情况下才表示 内核加载成功 其他数据不管大小都是失败的 否则重新 加载
//                if (progress != 100) {
//                    TbsDownloader.startDownload(instance)
//                }
            }

            override fun onInstallFinish(progress: Int) {
                //安装结束时的状态，安装成功时errorCode为200,其他均为失败，外部不需要关注具体的失败原因
                Log.d("QbSdk", "onInstallFinish -->安装X5内核进度：$progress")
            }

            override fun onDownloadProgress(progress: Int) {
                Log.d("QbSdk", "onDownloadProgress -->下载X5内核进度：$progress")
            }
        })
        // qb
        QbSdk.initX5Environment(this, object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
                Log.i("QbSdk", "qb sdk core init finish..........")
            }

            override fun onViewInitFinished(p0: Boolean) {
                Log.i("QbSdk", "qb sdk init $p0 ..........")
//                if (!p0) {
//                    TbsDownloader.startDownload(instance)
//                }
            }
        })
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
        JPushInterface.init(this)

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
        } catch (e: Exception) {
            XLog.error("", e)
        }
    }


}