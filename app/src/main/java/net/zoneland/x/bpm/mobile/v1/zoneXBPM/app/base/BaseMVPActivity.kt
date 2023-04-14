package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.util.NotificationUtil
import com.wugang.activityresult.library.ActivityResult
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.RetrofitClient
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.IdentityLevelForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.unit.UnitJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.ContactPickerResult
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.LoadingDialog
import org.jetbrains.anko.internals.AnkoInternals.createAnkoContext
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * Created by fancy on 2017/6/5.
 */

abstract class BaseMVPActivity<in V: BaseView, T: BasePresenter<V>>: AppCompatActivity(), BaseView {

    //need override
    abstract protected var mPresenter : T
    abstract fun afterSetContentView(savedInstanceState: Bundle?)
    abstract fun layoutResId(): Int
    //
    open fun beforeSetContentView(){}
    override fun getContext(): Context  = this

    //Toolbar 标题栏
    protected var toolbar: Toolbar? = null
    /**
     * ActionBar居中的标题
     */
    protected var toolbarTitle: TextView? = null

    var loadingDialog: LoadingDialog? = null

    // 字体不放大
    private val fontScale = 1f


    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        beforeSetContentView()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(layoutResId())
        //防止截屏
//        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        // 沉浸式状态栏
//        ImmersedStatusBarUtils.setImmersedStatusBar(this)

        mPresenter.attachView(this as V)
        afterSetContentView(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = FancySkinManager.instance().getColor(this, R.color.z_color_primary)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    //固定字体比例 防止系统设置字体影响
    override fun getResources(): Resources {
        var res =  super.getResources()
        if (res.configuration != null && res.configuration.fontScale != fontScale) {
            val config = Configuration()
            config.setToDefaults()
            config.fontScale = fontScale
            val ct = createConfigurationContext(config)
            if (ct != null) {
                res = ct.resources
            }
        }
        return res
    }


    fun setupToolBar(title:String = "", setupBackButton:Boolean = false, isCloseBackIcon: Boolean = false) {
        toolbar = findViewById(R.id.toolbar_snippet_top_bar)
        toolbar?.title = ""
        setSupportActionBar(toolbar)
        toolbarTitle = findViewById(R.id.tv_snippet_top_title)
        toolbarTitle?.text = title
        if (setupBackButton) {
            if (isCloseBackIcon){
                setToolbarBackBtnWithCloseIcon()
            }else {
                setToolbarBackBtn()
            }
        }
    }

    fun updateToolbarTitle(title: String) {
        toolbarTitle?.text = title
    }

    fun setToolbarBackBtn() {
        toolbar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
        toolbar?.setNavigationOnClickListener { finish() }
    }
    fun setToolbarBackBtnWithCloseIcon() {
        toolbar?.setNavigationIcon(R.mipmap.icon_menu_window_close)
        toolbar?.setNavigationOnClickListener { finish() }
    }

    fun showLoadingDialog() {
        if (loadingDialog==null) {
            loadingDialog = LoadingDialog(this)
        }
        loadingDialog?.show()
    }
    fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }


    fun contactPicker(bundle: Bundle, callback: (ContactPickerResult?)-> Unit) {
        ActivityResult.of(this)
                .className(ContactPickerActivity::class.java)
                .params(bundle)
                .greenChannel().forResult { _, data ->
                    val result = data?.getParcelableExtra<ContactPickerResult>(ContactPickerActivity.CONTACT_PICKED_RESULT)
                    if (result != null) {
                        callback(result)
                    }else {
                        callback(null)
                    }
                }
    }

    fun getCurrentIdentityUnit(callback:(String?)-> Unit) {
        val expressService = RetrofitClient.instance().assembleExpressApi()
        val personalService = RetrofitClient.instance().assemblePersonalApi()
        personalService.getCurrentPersonInfo()
                .subscribeOn(Schedulers.io())
                .flatMap { unitResponse ->
                    val person = unitResponse.data
                    if (person != null) {
                        val identityList = person.woIdentityList
                        if (identityList.isNotEmpty()) {
                            val identity = identityList[0]
                            val form = IdentityLevelForm(identity = identity.distinguishedName, level = 1)
                            expressService.unitByIdentityAndLevel(form)
                        } else {
                            Observable.just(ApiResponse<UnitJson>())
                        }
                    } else {
                        Observable.just(ApiResponse<UnitJson>())
                    }
                }.observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { unitJsonApiResponse ->
                        if (unitJsonApiResponse.data != null) {
                            callback(unitJsonApiResponse.data.distinguishedName)
                        }else {
                            callback(null)
                        }
                    }
                    onError{ e, _ ->
                        XLog.error("", e)
                        callback(null)
                    }
                }
    }


    /**
     * webview 图片太大的问题处理
     */
    fun imgReset(webv: WebView) {
        webv.loadUrl(
            "javascript:(function(){" +
                    "var objs = document.getElementsByTagName('img'); " +
                    "for(var i=0;i<objs.length;i++) " +
                    "{"
                    + "var img = objs[i]; " +
                    " img.style.maxWidth = '100%'; img.style.height = 'auto'; " +
                    "}" +
                    "})()"
        )
    }

    private val mNotificationManager: NotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val mNormalDefaultChannelId = "o2oa_base_notify_default_channel"
    private val mNormalDefaultChannelName = "普通"
    private val mNormalHighChannelId = "o2oa_base_notify_High_channel"
    private val mNormalHighChannelName = "重要"
    private val mNormalNotificationId = 1024 // 应用内通知id
    /**
     * app通知
     * @param importance  NotificationManager.IMPORTANCE_LOW NotificationManager.IMPORTANCE_HIGH
     */
    fun baseNotify(title: String, content:String, importance: Int = 0 ) {
        XLog.debug("发送通知，$title , $content , $importance")
        var channelId = mNormalDefaultChannelId
        // 适配8.0及以上 创建渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var imp = importance
            if (imp == 0) {
                imp = NotificationManager.IMPORTANCE_DEFAULT
            }
            if (imp == NotificationManager.IMPORTANCE_HIGH) {
                channelId = mNormalHighChannelId
            }
            val channelName = if (imp == NotificationManager.IMPORTANCE_HIGH) {
                mNormalHighChannelName
            } else {
                mNormalDefaultChannelName
            }
            val channel = NotificationChannel(channelId, channelName, imp)
            mNotificationManager.createNotificationChannel(channel)
        }
        // 构建配置
        val mBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title) // 标题
            .setContentText(content) // 文本
            .setSmallIcon(R.mipmap.logo) // 小图标
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 7.0 设置优先级
            .setAutoCancel(true) // 是否自动消失（点击）or mManager.cancel(mNormalNotificationId)、cancelAll、setTimeoutAfter()
        // 发起通知
        mNotificationManager.notify(mNormalNotificationId, mBuilder.build())

    }

}