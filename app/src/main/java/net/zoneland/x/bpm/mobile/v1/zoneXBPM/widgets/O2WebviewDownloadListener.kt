package net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets

import android.app.Activity
import android.text.TextUtils
import android.webkit.DownloadListener
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.viewer.BigImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.tbs.FileReaderActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.cache.MD5Util
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.LoadingDialog
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by fancyLou on 2021-11-26.
 * Copyright © 2021 o2android. All rights reserved.
 */
class O2WebviewDownloadListener(val activity: Activity) : DownloadListener {

    val weakActivity: WeakReference<Activity> = WeakReference(activity)

    override fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {
        XLog.info("webview 开始下载文件")
        XLog.info("url:$url")
        XLog.info("userAgent:$userAgent")
        XLog.info("contentDisposition: $contentDisposition ")
        XLog.info("mimetype: $mimetype ")
        XLog.info("contentLength: $contentLength")

        if (!TextUtils.isEmpty(url)) {
            downloadFile(url!!, contentDisposition, mimetype)
        }

    }

    private fun downloadFile(url: String, contentDisposition: String?, mimetype: String?) {
        val fileName = getFileName(contentDisposition, mimetype)
        val ac = weakActivity.get() ?: return
        // 防止文件名相同，md5 url作为文件夹名称
        val dirUrl = MD5Util.getMD5String(url) ?: "null"
        val path = FileExtensionHelper.getXBPMTempFolder(ac) + File.separator + dirUrl + File.separator + fileName
        XLog.info("下载文件到本地， filePath : $path")
        showLoadingDialog()
        Observable.just(DownloadFileForm(url, path))
            .subscribeOn(Schedulers.io())
            .flatMap { form ->
                O2FileDownloadHelper.download(form.downloadUrl, form.filePath)
            }.observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext {
                    hideLoadingDialog()
                    openFile(path)
                }
                onError { e, _ ->
                    XLog.error("", e)
                    hideLoadingDialog()
                    val aci = weakActivity.get()
                    if (aci != null) {
                        XToast.toastShort(ac, "下载文件失败！${e?.message ?: ""}")
                    }
                }
            }
    }

    private fun openFile(filePath: String) {
        val file = File(filePath)
        val ac = weakActivity.get()
        ac?.runOnUiThread {
            if (file.exists()) {
                if (FileExtensionHelper.isImageFromFileExtension(file.extension)) {
//                    ac.go<LocalImageViewActivity>(LocalImageViewActivity.startBundle(file.absolutePath))
                    BigImageViewActivity.startLocalFile(ac, file.absolutePath)
                } else {
                    ac.go<FileReaderActivity>(FileReaderActivity.startBundle(file.absolutePath))
                }
            }
        }
    }

    /**
     *
     */
    private fun getFileName(contentDisposition: String?, mimetype: String?): String {
        var fileName = ""
        if (!TextUtils.isEmpty(contentDisposition)) {
            fileName = when {
                contentDisposition!!.contains("''") -> {
                    contentDisposition.substringAfterLast("''")
                }
                contentDisposition.contains("=") -> {
                    contentDisposition.substringAfterLast("=")
                }
                else -> {
                    contentDisposition
                }
            }
        }
        if (TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(mimetype)) {
            val ext = FileUtil.getFileExtensionByMimetype(mimetype!!)
            if (!TextUtils.isEmpty(ext)) {
                val now = Date().time
                fileName = "$now$ext"
            }
        }
        if (TextUtils.isEmpty(fileName)) {
            val now = Date().time
            fileName = "$now"
        }
        return fileName
    }

    private var loadingDialog: LoadingDialog? = null
    private fun showLoadingDialog() {
        val ac = weakActivity.get()
        ac?.runOnUiThread {
            if (loadingDialog==null) {
                loadingDialog = LoadingDialog(ac)
            }
            loadingDialog?.show()
        }
    }
    private fun hideLoadingDialog() {
        val ac = weakActivity.get()
        ac?.runOnUiThread {
            loadingDialog?.dismiss()
        }
    }


    inner class DownloadFileForm(
        val downloadUrl:String,
        val filePath: String
    )
}