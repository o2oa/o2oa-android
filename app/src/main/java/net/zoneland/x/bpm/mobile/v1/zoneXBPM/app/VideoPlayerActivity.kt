package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import kotlinx.android.synthetic.main.activity_video_player.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.AndroidUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import java.io.File

class VideoPlayerActivity : AppCompatActivity() {



    companion object {
        const val VIDEO_URL_KEY = "VIDEO_URL_KEY"
        const val VIDEO_TITLE_KEY = "VIDEO_TITLE_KEY"
        fun startPlay(activity: Activity, videoUrl: String, title: String = "") {
            val bundle = Bundle()
            bundle.putString(VIDEO_URL_KEY, videoUrl)
            bundle.putString(VIDEO_TITLE_KEY, title)
            activity.go<VideoPlayerActivity>(bundle)
        }
    }

    private var orientationUtils: OrientationUtils? = null
    private var currentUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            // 始终允许窗口延伸到屏幕短边上的刘海区域
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }

        setContentView(R.layout.activity_video_player)

        // 透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        val statusBarHeight = AndroidUtils.getStatusBarHeight(this)

        val barLayout = ll_video_player_bar.layoutParams as ConstraintLayout.LayoutParams
        barLayout.topMargin = statusBarHeight
        ll_video_player_bar.layoutParams = barLayout

        val url = intent.getStringExtra(VIDEO_URL_KEY)
        val title = intent.getStringExtra(VIDEO_TITLE_KEY) ?: ""
        if (url != null && url.isNotEmpty()) {
            init(url, title)
        }else {
            XToast.toastShort(this, getString(R.string.message_have_no_play_url))
            finish()
        }
    }


    private fun init(url: String, title: String) {
        currentUrl = url
        tv_video_player_title.text = title
        btn_video_player_close.setOnClickListener { onBackPressed() }
        btn_video_player_share.setOnClickListener { share() }

        video_player.setUp(url, true, title)
        //增加封面
//        val imageView = ImageView(this)
//        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//        imageView.setImageResource(R.mipmap.my_app_top)
//        video_player.thumbImageView = imageView
        //增加title
        video_player.titleTextView.visibility = View.GONE
        //设置返回键
        video_player.backButton.visibility = View.GONE
        //设置旋转
        orientationUtils = OrientationUtils(this, video_player)
        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        video_player.fullscreenButton.setOnClickListener(View.OnClickListener { orientationUtils?.resolveByClick() })
        //是否可以滑动调整
        video_player.setIsTouchWiget(true)
        //设置返回按键功能
//        video_player.backButton.setOnClickListener(View.OnClickListener { onBackPressed() })
        video_player.startPlayLogic()
    }

    override fun onResume() {
        super.onResume()
        video_player.onVideoResume()
    }

    override fun onPause() {
        super.onPause()
        video_player.onVideoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        orientationUtils?.releaseListener()
    }

    override fun onBackPressed() {
        //先返回正常状态
        if (orientationUtils?.screenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            video_player.fullscreenButton.performClick()
            return
        }
        //释放所有
        video_player.setVideoAllCallBack(null)
        super.onBackPressed()

    }

    private fun share() {
        currentUrl?.let {
            AndroidUtils.shareFile(this, File(it))
        }
    }
}
