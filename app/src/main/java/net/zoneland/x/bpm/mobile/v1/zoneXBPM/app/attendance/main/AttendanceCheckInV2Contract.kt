package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.*


object AttendanceCheckInV2Contract {
    interface View : BaseView {
        fun preCheckData(data: AttendanceV2PreCheckData?)
        fun checkInPostResponse(result: Boolean, message: String?)
    }

    interface Presenter : BasePresenter<View> {
        fun preCheckDataLoad()
        fun checkInPost(body: AttendanceV2CheckInBody)
    }
}
