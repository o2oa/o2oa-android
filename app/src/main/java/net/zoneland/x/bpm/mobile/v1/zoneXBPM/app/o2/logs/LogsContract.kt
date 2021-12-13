package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.logs

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import java.io.File

/**
 * Created by fancyLou on 2021-12-09.
 * Copyright © 2021 o2android. All rights reserved.
 */
object LogsContract {

    interface View : BaseView {
        fun showLogList(list: List<File>)
    }
    interface Presenter: BasePresenter<View> {
        fun loadLogFileList()
    }
}