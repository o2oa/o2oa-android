package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.appeal

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2AppealInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2Config

/**
 * Created by fancyLou on 2023-04-12.
 * Copyright © 2023 o2android. All rights reserved.
 */
object AttendanceV2AppealContract {
    interface View : BaseView {
        fun config(config: AttendanceV2Config?)
        fun appealList(list: List<AttendanceV2AppealInfo>)
        fun checkAppealResult(value: Boolean, appealInfo: AttendanceV2AppealInfo)
        fun appealStartedProcess(value: Boolean)
    }
    interface Presenter : BasePresenter<View> {
        fun config()
        fun myAppealListByPage(page: Int)
        fun checkAppeal(appealInfo: AttendanceV2AppealInfo)
        fun appealStartedProcess(id: String)
    }
}