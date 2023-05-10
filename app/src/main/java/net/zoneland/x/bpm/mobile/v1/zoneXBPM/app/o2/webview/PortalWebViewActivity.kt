package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_portal_web_view.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main.IndexPortalFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone

class PortalWebViewActivity : BaseMVPActivity<PortalWebViewContract.View, PortalWebViewContract.Presenter>(), PortalWebViewContract.View  {
    override var mPresenter: PortalWebViewContract.Presenter = PortalWebViewPresenter()

    override fun layoutResId(): Int = R.layout.activity_portal_web_view

    companion object {
        val PORTAL_ID_KEY = "PORTAL_ID_KEY"
        val PORTAL_PAGE_ID_KEY = "PORTAL_PAGE_ID_KEY"
        val PORTAL_NAME_KEY = "PORTAL_NAME_KEY"
        fun startPortal(portalId: String, portalName: String, pageId: String? = null): Bundle {
            val bundle = Bundle()
            bundle.putString(PORTAL_ID_KEY, portalId)
            bundle.putString(PORTAL_NAME_KEY, portalName)
            if (!TextUtils.isEmpty(pageId)) {
                bundle.putString(PORTAL_PAGE_ID_KEY, pageId)
            }
            return bundle
        }
    }

    private var portalId: String = ""
    private var pageId: String = ""
    private var portalName: String = ""

    private var portalFragment:IndexPortalFragment? = null


    override fun afterSetContentView(savedInstanceState: Bundle?) {
        portalId = intent.extras?.getString(PORTAL_ID_KEY) ?: ""
        pageId = intent.extras?.getString(PORTAL_PAGE_ID_KEY) ?: ""
        portalName = intent.extras?.getString(PORTAL_NAME_KEY) ?: ""
        if (TextUtils.isEmpty(portalId)) {
            XToast.toastShort(this, getString(R.string.message_portal_need_id))
            finish()
        }else {
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

//            setupToolBar(portalName)
            tv_snippet_top_title.text = portalName
            portalFragment = IndexPortalFragment.instance(portalId, pageId)

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_portal_web_view_content, portalFragment!!)
            transaction.commit()
//            toolbar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
//            toolbar?.setNavigationOnClickListener {
//                if (portalFragment?.previousPage() == false) {
//                    finish()
//                }
//            }
        }
    }

    fun setWebViewTitle(title: String) {
        tv_snippet_top_title.text = title
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        portalFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_scale_in, R.anim.activity_scale_out)
    }

    private fun goBack() {
        if (portalFragment?.previousPage() == false) {
            finish()
        }
    }


    fun hideToolBar() {
        app_bar_layout_snippet.gone()
    }
}
