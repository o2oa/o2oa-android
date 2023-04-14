package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2CheckInBody
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class AttendanceCheckInV2Presenter : BasePresenterImpl<AttendanceCheckInV2Contract.View>(), AttendanceCheckInV2Contract.Presenter {
    override fun preCheckDataLoad() {
        val service = getAttendanceAssembleControlService(mView?.getContext())
        if (service != null) {
            service.attendanceV2PreCheck()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.preCheckData(it?.data)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.preCheckData(null)
                    }
                }
        } else {
            mView?.preCheckData(null)
        }
    }

    override fun checkInPost(body: AttendanceV2CheckInBody) {
        val service = getAttendanceAssembleControlService(mView?.getContext())
        if (service != null) {
            service.attendanceV2CheckIn(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it != null && it.data != null ) {
                            mView?.checkInPostResponse(true, null)
                        } else {
                            mView?.checkInPostResponse(false, null)
                        }
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.checkInPostResponse(false, e?.message)
                    }
                }
        } else {
            mView?.checkInPostResponse(false, null)
        }
    }
}
