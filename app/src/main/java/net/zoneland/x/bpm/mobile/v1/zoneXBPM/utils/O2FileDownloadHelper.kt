package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import rx.Observable
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


/**
 * Created by fancyLou on 2020-06-22.
 * Copyright © 2020 O2. All rights reserved.
 */

object O2FileDownloadHelper {

    /**
     * 文件是否需要下载
     * 比较附件的更新时间判断是否需要下载
     * @param updateTime 附件对象最后更新时间 yyyy-MM-dd HH:mm:ss
     * @param path 文件本地路径
     */
    fun fileNeedDownload(updateTime:String,  path: String): Boolean {
        XLog.debug("更新时间 $updateTime")
        val file = File(path)
        return if (file.exists()) {
            val time = file.lastModified()
            val date =  Date()
            date.time = time
            val dateString = DateHelper.getDateTime(date)
            XLog.debug("文件时间 $dateString")
            !DateHelper.lsOrEq(updateTime, dateString)
        }else {
            true
        }
    }

    fun download(downloadUrl: String, outputFilePath: String): Observable<Boolean> {
        XLog.info("准备下载文件 网络下载url: $downloadUrl 本地路径: $outputFilePath")
        return Observable.create { subscriber ->
            val file = File(outputFilePath)
            if (file.exists()) {
                subscriber.onNext(true)
                subscriber.onCompleted()
            }else {
                try {
                    SDCardHelper.generateNewFile(outputFilePath)
                    val url = URL(downloadUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("Accept-Encoding", "identity")
                    val newCookie = O2SDKManager.instance().tokenName() + ":" + O2SDKManager.instance().zToken
                    conn.setRequestProperty("Cookie", newCookie)
                    conn.setRequestProperty("x-client", O2.DEVICE_TYPE)
                    conn.setRequestProperty(O2SDKManager.instance().tokenName(), O2SDKManager.instance().zToken)
                    conn.connect()
                    val inputStream = conn.inputStream
                    var fileName = conn.getHeaderField("Content-Disposition")
                    if (fileName!=null) {
                        fileName = fileName.substringAfterLast("''")
                    }
                    XLog.debug("下载文件名称: $fileName")
                    val fos = FileOutputStream(file, true)
                    val buf = ByteArray(1024 * 8)
                    var currentLength = 0
                    while (true) {
                        val num = inputStream.read(buf)
                        currentLength += num
                        // 计算进度条位置
                        if (num <= 0) {
                            break
                        }
                        fos.write(buf, 0, num)
                        fos.flush()
                    }
                    XLog.debug("file length :$currentLength")
                    fos.flush()
                    fos.close()
                    inputStream.close()
                    subscriber.onNext(true)
                    subscriber.onCompleted()
                }catch (e: Exception){
                    try {
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {}
                    subscriber.onError(e)
                    subscriber.onCompleted()
                }
            }
        }
    }


}