package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.logs

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

/**
 * Created by fancyLou on 2021-12-09.
 * Copyright © 2021 o2android. All rights reserved.
 */
class LogsPresenter: BasePresenterImpl<LogsContract.View>(), LogsContract.Presenter {


    override fun loadLogFileList() {
        XLog.debug("获取日志文件列表")
        Observable.just(true)
            .subscribeOn(Schedulers.io())
            .flatMap { _ ->
                val logPath = FileExtensionHelper.getXBPMLogFolder(mView?.getContext())
                XLog.debug("logpath $logPath")
                val logDir = File(logPath)
                if (logDir.exists() && logDir.isDirectory) {
                    XLog.debug("有文件夹。。。。")
                    val olst = logDir.listFiles()
                    XLog.debug("len " + olst?.size)
                    val lst = logDir.listFiles().toList()
                    XLog.debug("lst " + lst.size)
                    Observable.just(lst)
                } else {
                    XLog.debug("没有文件夹。。。。")
                    Observable.just(ArrayList())
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext { list ->
                    mView?.showLogList(list)
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.showLogList(ArrayList())
                }
            }
    }
}