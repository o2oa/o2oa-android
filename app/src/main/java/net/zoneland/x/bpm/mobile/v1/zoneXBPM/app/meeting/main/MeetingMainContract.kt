package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.meeting.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.meeting.MeetingInfoJson


object MeetingMainContract {
    interface View : BaseView {
        fun getMeetingById(meetingInfo: MeetingInfoJson)
        fun error(errorMsg: String)
    }

    interface Presenter : BasePresenter<View> {
        fun getMeetingById(id: String)
    }
}
