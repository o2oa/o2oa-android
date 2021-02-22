package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_big_image_view.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.CloudDiskFileDownloadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.LoadingDialog
import java.io.File


class BigImageViewActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_TITLE_KEY = "IMAGE_TITLE_KEY"
        const val IMAGE_ID_KEY = "IMAGE_ID_KEY"
        const val IMAGE_EXTENSION_KEY = "IMAGE_EXTENSION_KEY"
        const val IMAGE_LOCAL_PATH_KEY = "IMAGE_LOCAL_PATH_KEY"
        fun start(activity: Activity, fileId: String, extension: String, title: String = "") {
            val bundle = Bundle()
            bundle.putString(IMAGE_ID_KEY, fileId)
            bundle.putString(IMAGE_EXTENSION_KEY, extension)
            bundle.putString(IMAGE_TITLE_KEY, title)
            activity?.go<BigImageViewActivity>(bundle)
        }
        fun startLocalFile(activity: Activity, filePath: String) {
            val bundle = Bundle()
            bundle.putString(IMAGE_LOCAL_PATH_KEY, filePath)
            activity.go<BigImageViewActivity>(bundle)
        }
    }

    var loadingDialog: LoadingDialog? = null

    private val downloader: CloudDiskFileDownloadHelper by lazy { CloudDiskFileDownloadHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //隐藏状态栏
        //定义全屏参数
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag)

        setContentView(R.layout.activity_big_image_view)

        btn_big_picture_close.setOnClickListener { finish() }

        val localPath = intent.getStringExtra(IMAGE_LOCAL_PATH_KEY) ?: ""
        if (TextUtils.isEmpty(localPath)) {
            val fileId = intent.getStringExtra(IMAGE_ID_KEY) ?: ""
            val extension = intent.getStringExtra(IMAGE_EXTENSION_KEY) ?: ""
            val title = intent.getStringExtra(IMAGE_TITLE_KEY) ?: ""
            tv_big_picture_title.text = title
            if (fileId.isEmpty() || extension.isEmpty()) {
                XToast.toastShort(this, "没有传入文件id 或 扩展名 ，无法下载大图！")
            } else {
                downloader.showLoading = {
                    showLoadingDialog()
                }
                downloader.hideLoading = {
                    hideLoadingDialog()
                }
                downloader.startDownload(fileId, extension) { file ->
                    XLog.debug("返回了。。。。")
                    if (file != null) {
                        O2ImageLoaderManager.instance().showImage(zoomImage_big_picture_view, file)
                    }else {
                        XToast.toastShort(this, "下载大图失败！")
                    }
                }
            }
        }else {
            val file = File(localPath)
            tv_big_picture_title.text = file.name
            O2ImageLoaderManager.instance().showImage(zoomImage_big_picture_view, file)
        }

    }


    override fun onStop() {
        downloader.closeDownload()
        super.onStop()
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
