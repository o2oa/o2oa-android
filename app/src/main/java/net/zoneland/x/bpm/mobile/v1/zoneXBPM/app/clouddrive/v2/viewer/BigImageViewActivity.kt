package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_big_image_view.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.CloudDiskFileDownloadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.AndroidUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.copyToAlbum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.LoadingDialog
import java.io.File


class BigImageViewActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_IS_V3_KEY = "IMAGE_IS_V3_KEY"
        const val IMAGE_TITLE_KEY = "IMAGE_TITLE_KEY"
        const val IMAGE_ID_KEY = "IMAGE_ID_KEY"
        const val IMAGE_EXTENSION_KEY = "IMAGE_EXTENSION_KEY"
        const val IMAGE_LOCAL_PATH_KEY = "IMAGE_LOCAL_PATH_KEY"
        const val IMAGE_INTERNET_PATH_KEY = "IMAGE_INTERNET_PATH_KEY"
        fun start(activity: Activity, fileId: String, extension: String, title: String = "") {
            val bundle = Bundle()
            bundle.putString(IMAGE_ID_KEY, fileId)
            bundle.putString(IMAGE_EXTENSION_KEY, extension)
            bundle.putString(IMAGE_TITLE_KEY, title)
            activity.go<BigImageViewActivity>(bundle)
        }
        fun startForV3(activity: Activity, fileId: String, extension: String, title: String = "") {
            val bundle = Bundle()
            bundle.putString(IMAGE_ID_KEY, fileId)
            bundle.putString(IMAGE_EXTENSION_KEY, extension)
            bundle.putString(IMAGE_TITLE_KEY, title)
            bundle.putString(IMAGE_TITLE_KEY, title)
            bundle.putBoolean(IMAGE_IS_V3_KEY, true)
            activity.go<BigImageViewActivity>(bundle)
        }
        fun startLocalFile(activity: Activity, filePath: String) {
            val bundle = Bundle()
            bundle.putString(IMAGE_LOCAL_PATH_KEY, filePath)
            activity.go<BigImageViewActivity>(bundle)
        }

        fun startInternetImageUrl(activity: Activity, url: String) {
            val bundle = Bundle()
            bundle.putString(IMAGE_INTERNET_PATH_KEY, url)
            activity.go<BigImageViewActivity>(bundle)
        }
    }

    var loadingDialog: LoadingDialog? = null

    private val downloader: CloudDiskFileDownloadHelper by lazy { CloudDiskFileDownloadHelper(this) }

    // 分享使用
    var currentImage: File? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            // 始终允许窗口延伸到屏幕短边上的刘海区域
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }

        setContentView(R.layout.activity_big_image_view)
        // 透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        val statusBarHeight = AndroidUtils.getStatusBarHeight(this)

        val barLayout = ll_big_picture_bar.layoutParams as ConstraintLayout.LayoutParams
        barLayout.topMargin = statusBarHeight
        ll_big_picture_bar.layoutParams = barLayout



        btn_big_picture_close.setOnClickListener { finish() }
        btn_big_picture_share.setOnClickListener { share() }

        val localPath = intent.getStringExtra(IMAGE_LOCAL_PATH_KEY) ?: ""
        val internetImageUrl = intent.getStringExtra(IMAGE_INTERNET_PATH_KEY) ?: ""
        if (!TextUtils.isEmpty(localPath)) {
            val file = File(localPath)
            currentImage = file
            tv_big_picture_title.text = file.name
            addClickSave()
//            O2ImageLoaderManager.instance().showImage(zoomImage_big_picture_view, file)
            ssiv_big_picture_view.setImage(ImageSource.uri(localPath))
        } else if (!TextUtils.isEmpty(internetImageUrl)) {
            tv_big_picture_title.gone()
            btn_big_picture_share.gone()
            rl_big_picture_download_btn.gone()
//            O2ImageLoaderManager.instance().showImage(zoomImage_big_picture_view, internetImageUrl)
            ssiv_big_picture_view.setImage(ImageSource.uri(internetImageUrl))
        } else {
            val fileId = intent.getStringExtra(IMAGE_ID_KEY) ?: ""
            val extension = intent.getStringExtra(IMAGE_EXTENSION_KEY) ?: ""
            val title = intent.getStringExtra(IMAGE_TITLE_KEY) ?: ""
            tv_big_picture_title.text = title
            if (fileId.isEmpty() || extension.isEmpty()) {
                XToast.toastShort(this, "没有传入文件id 或 扩展名 ，无法下载大图！")
            } else {

                // 企业网盘下载地址不一样
                val isV3 = intent.getBooleanExtra(IMAGE_IS_V3_KEY, false)
                if (isV3) {
                    downloader.isV3 = true
                }

                downloader.showLoading = {
                    showLoadingDialog()
                }
                downloader.hideLoading = {
                    hideLoadingDialog()
                }
                downloader.startDownload(fileId, extension) { file ->
                    XLog.debug("返回了。。。。")
                    if (file != null) {
                        currentImage = file
                        addClickSave()
//                        O2ImageLoaderManager.instance().showImage(zoomImage_big_picture_view, file)
                        ssiv_big_picture_view.setImage(ImageSource.uri(file.absolutePath))

                    }else {
                        XToast.toastShort(this, "下载大图失败！")
                    }
                }
            }
        }

    }

    override fun onStop() {
        downloader.closeDownload()
        super.onStop()
    }



    /**
     * 保存图片事件
     */
    private fun addClickSave() {
        rl_big_picture_download_btn.setOnClickListener {
            saveToAlbum()
        }
    }

    /**
     * 保存到相册
     */
    private fun saveToAlbum() {
        currentImage?.let {
            it.copyToAlbum(this, it.name, null)
        }
        XToast.toastShort(this, R.string.message_save_image_album)
    }

    /**
     * 分享图片
     */
    private fun share() {
        currentImage?.let {
            AndroidUtils.shareFile(this, it)
            //
//            val uri = it.copyToAlbum(this, it.name, null)
//            val intent = Intent(Intent.ACTION_SEND)
//                .putExtra(Intent.EXTRA_STREAM, uri)
//                .setType("image/*")
//            startActivity(Intent.createChooser(intent, null))
        }
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
}
