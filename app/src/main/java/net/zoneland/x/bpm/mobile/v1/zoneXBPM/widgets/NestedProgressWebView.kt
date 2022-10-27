package net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.webkit.*
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.StringUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog


/**
 * Created by fancy on 2017/5/31.
 */
class NestedProgressWebView : WebView {


    private lateinit var progressBar: ProgressBar
    private val mActionList = ArrayList<String>()
    private var mActionMode: ActionMode? = null
    private var mLinkJsInterfaceName:String = "fancyActionJsInterface"

    var mSelectActionListener: ActionSelectClickListener? = null

    constructor(context: Context): super(context) {
        isNestedScrollingEnabled = true
        initProgress()
    }
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        isNestedScrollingEnabled = true
        initProgress()
    }
    constructor(context: Context, attributeSet: AttributeSet, def: Int): super(context, attributeSet, def) {
        isNestedScrollingEnabled = true
        initProgress()
    }

    private fun initProgress() {
        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 10, 0, 0)
        val drawable = ContextCompat.getDrawable(context, R.drawable.web_view_progress_bar)
        progressBar.progressDrawable = drawable
        addView(progressBar)
        //滚动条样式
        scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        initSettings()
        webChromeClient = ProgressWebChromeClient()


    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = false
        }
        settings.userAgentString = settings.userAgentString + " O2OA"
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.setAppCacheEnabled(true)
//        settings.builtInZoomControls = false
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportMultipleWindows(false)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.domStorageEnabled = true
//        settings.useWideViewPort = true // 任意缩放？


        //5.0以上开启混合模式加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    fun addActionList(list: List<String>) {
        mActionList.addAll(list)
        addJavascriptInterface(ActionSelectInterface(), mLinkJsInterfaceName)
    }
    fun clearAllAction() {
        mActionList.clear()
    }

    /**
     * 设置当前登录用户的cookie信息
     * @param context
     * *
     * @return
     */
    fun webViewSetCookie(context: Context, url: String) {
        //设置cookie
        val domain = StringUtil.getSubDomain(url)
        XLog.info("domain:$domain")
        val cookie = O2SDKManager.instance().tokenName() + "=" + O2SDKManager.instance().zToken
        XLog.info("cookie:$cookie")
        val host = APIAddressHelper.instance().getWebViewHost()
        XLog.info("host:$host")
        val cookieStr: String
        cookieStr = if (StringUtil.isIp(host)) {
            "$cookie; path=/; domain=$host"
        } else {
            "$cookie; path=/; domain=.$domain"
        }
        XLog.info("Set-Cookie:$cookieStr")

//        CookieSyncManager.createInstance(context)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setCookie(url, cookieStr)
//        CookieSyncManager.getInstance().sync()

        val newCookie = cookieManager.getCookie(url)
        if (newCookie != null) {
            XLog.info("Nat: webView.syncCookie.newCookie $newCookie")
        }
        XLog.info("mCookieManager is finish")
    }

    override fun startActionMode(callback: ActionMode.Callback?): ActionMode? {
        val actionMode =  super.startActionMode(callback)
        if (mActionList.isEmpty()) {
            return actionMode
        }else {
            return resolveActionMode(actionMode)
        }
    }


    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        val actionMode = super.startActionMode(callback, type)
        if (mActionList.isEmpty()) {
            return actionMode
        }else {
            return resolveActionMode(actionMode)
        }
    }

    ///////////////////////////// 关于webview内部滑动和外部比如viewPager的滑动冲突的问题
    //最大递归深度
    var MAX_PARENT_DEPTH: Int = 3

    //在WebView的onTouchEvent事件为ACTION_DOWN时，查找父视图是否是可以滑动的视图(如ViewPager)，
    // 如果是,则通过requestDisallowInterceptTouchEvent(true)调用，请求父视图不要拦截touchEvent
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewParent = findViewParentIfNeeds(this, MAX_PARENT_DEPTH)
            viewParent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.onTouchEvent(event)
    }

    /**
     * 查找父布局是是否是可滑动的View
     * @param tag
     * @param depth 最大递归深度，防止出现死循环
     * @return
     */
    private fun findViewParentIfNeeds(tag: View, depth: Int): ViewParent? {
        if (depth < 0) {
            return null
        }
        val parent = tag.parent ?: return null
        return if (parent is ViewGroup) {
            if (canScrollHorizontally(parent as View) || canScrollVertically(parent as View)) {
                parent
            } else {
                findViewParentIfNeeds(parent as View, depth - 1)
            }
        } else null
    }

    /**
     * 是否可以横向滑动
     */
    private fun canScrollHorizontally(view: View): Boolean {
        return view.canScrollHorizontally(100) || view.canScrollHorizontally(-100)
    }

    /**
     * 是否可以纵向滑动
     */
    private fun canScrollVertically(view: View): Boolean {
        return view.canScrollVertically(100) || view.canScrollVertically(-100)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        // 解决webview与viewpager等滑动手势冲突问题
        if (clampedX || clampedY) {
            val viewParent = findViewParentIfNeeds(this, MAX_PARENT_DEPTH)
            viewParent?.requestDisallowInterceptTouchEvent(false)
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
    }
    ///////////////////////////// 关于webview内部滑动和外部比如viewPager的滑动冲突的问题 end /////////////

    //webview 长按菜单处理
    private fun resolveActionMode(actionMode: ActionMode?): ActionMode? {
        if (actionMode!=null) {
            mActionMode = actionMode
            val menu = actionMode.menu
            menu?.let { m->
                m.clear()
                mActionList.map { menu.add(it) }
                mActionList.mapIndexed { index, s ->
                    val item = m.getItem(index)
                    item.setOnMenuItemClickListener {
                        getSelectedData(it.title as String)
                        releaseAction()
                        true
                    }
                }
            }

        }
        mActionMode = actionMode
        return actionMode
    }

    private fun releaseAction() {
        if (mActionMode!=null) {
            mActionMode?.finish()
            mActionMode = null
        }
    }

    private fun getSelectedData(title: String) {
        val js = "(function getSelectedText() {" +
                "var txt;" +
                "var title = \"$title\";" +
                "if (window.getSelection) {" +
                "txt = window.getSelection().toString();" +
                "} else if (window.document.getSelection) {" +
                "txt = window.document.getSelection().toString();" +
                "} else if (window.document.selection) {" +
                "txt = window.document.selection.createRange().text;" +
                "}" +
                "$mLinkJsInterfaceName.actionClickCallback(txt,title);" +
                "})()"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("javascript:$js", null)
        } else {
            loadUrl("javascript:$js")
        }

    }


    interface ActionSelectClickListener {
        fun onClickActionItem(txt: String, title: String)
    }


    inner class ActionSelectInterface {
        @JavascriptInterface
        fun actionClickCallback(txt:String, title:String) {
            mSelectActionListener?.onClickActionItem(txt, title)
        }
    }

    inner class ProgressWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress == 100) {
                progressBar.visibility = View.GONE
            } else {
                if (progressBar.visibility == View.GONE)
                    progressBar.visibility = View.VISIBLE
                progressBar.progress = newProgress
            }
            super.onProgressChanged(view, newProgress)
        }


    }
}