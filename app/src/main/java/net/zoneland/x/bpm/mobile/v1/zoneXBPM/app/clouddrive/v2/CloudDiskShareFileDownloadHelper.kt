package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2

import android.app.Activity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.APIDistributeTypeEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.O2FileDownloadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File


/**
 * 云盘文件下载工具类
 */
class CloudDiskShareFileDownloadHelper(val activity: Activity) {

    var showLoading: (()->Unit)? = null
    var hideLoading: (()->Unit)? = null

//    var downloader: Future<Unit>? = null
    var subscription: Subscription? = null

    /**
     * 开始下载文件
     */
    fun startDownload(shareId: String, fileId: String, extension: String, result: (file: File?)->Unit) {
        showLoading?.invoke()

        val path = FileExtensionHelper.getXBPMTempFolder(activity)+ File.separator + fileId + "." +extension
        XLog.debug("file path $path")
        val downloadUrl =  if (O2SDKManager.instance().appCloudDiskIsV3()) {
            APIAddressHelper.instance()
                .getCommonDownloadUrl(
                    APIDistributeTypeEnum.x_pan_assemble_control,
                    "jaxrs/share/download/share/$shareId/file/$fileId")
        } else {
            APIAddressHelper.instance()
                .getCommonDownloadUrl(
                    APIDistributeTypeEnum.x_file_assemble_control,
                    "jaxrs/share/download/share/$shareId/file/$fileId")
        }
        XLog.debug("下载 文件 url: $downloadUrl")
        subscription = O2FileDownloadHelper.download(downloadUrl, path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean>{
                    override fun onError(e: Throwable?) {
                        XLog.error("", e)
                        hideLoading?.invoke()
                        result(null)
                    }

                    override fun onNext(t: Boolean?) {
                        hideLoading?.invoke()
                        result(File(path))
                    }

                    override fun onCompleted() {

                    }

                })

    }

    /**
     * 关闭下载
     */
    fun closeDownload() {
        hideLoading?.invoke()
        subscription?.unsubscribe()
    }
}