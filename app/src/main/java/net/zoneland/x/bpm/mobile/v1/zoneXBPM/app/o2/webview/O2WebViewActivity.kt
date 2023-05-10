package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview

import android.app.Activity
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.activity_o2_web_view.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.WebChromeClientWithProgressAndValueCallback

class O2WebViewActivity : AppCompatActivity() {

    companion object{
        const val TitleKey = "TitleKey"
        const val UrlKey = "UrlKey"
        fun openWebView(activity: Activity, title: String, url: String) {
            val bundle = Bundle()
            bundle.putString(TitleKey, title)
            bundle.putString(UrlKey, url)
            activity.go<O2WebViewActivity>(bundle)
        }
    }

    private val webChromeClient: WebChromeClientWithProgressAndValueCallback by lazy { WebChromeClientWithProgressAndValueCallback.with(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_o2_web_view)
        val title = intent?.extras?.getString(TitleKey) ?: ""
        val url = intent?.extras?.getString(UrlKey) ?: "https://www.o2oa.net"
        // actionbar
        val toolbar: Toolbar? = findViewById(R.id.toolbar_snippet_top_bar)
        toolbar?.title = ""
        setSupportActionBar(toolbar)
        // 初始化toolbar上的菜单
        menuInflater.inflate(R.menu.menu_portal, action_menu_view.menu)
        action_menu_view.setOnMenuItemClickListener { item ->
            XLog.info("点击了 item " + item.title)
            when(item.itemId) {
                R.id.left_back_btn -> goBack()
                R.id.left_close_btn -> finish()
            }
            return@setOnMenuItemClickListener false
        }
        updateToolbarTitle(title)
//        toolbar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
//        toolbar?.setNavigationOnClickListener { finish() }

        //webivew
        web_view.webChromeClient = webChromeClient
        webChromeClient.onO2ReceivedTitle = { t ->
            updateToolbarTitle(t)
        }
        web_view.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                XLog.error("ssl error, $error")
                handler?.proceed()
            }
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                XLog.debug("shouldOverrideUrlLoading:$url")
                view?.loadUrl(url)
                return true
            }
        }
        web_view.webViewSetCookie(this, url)
        web_view.loadUrl(url)

    }

    private fun updateToolbarTitle(title: String) {
        val toolbarTitle: TextView? = findViewById(R.id.tv_snippet_top_title)
        toolbarTitle?.text = title
    }

    private fun goBack() {
        val hasPrePage = if (web_view?.canGoBack() == true) {
            web_view.goBack()
            true
        } else {
            false
        }
        if (!hasPrePage) {
            finish()
        }
    }

}