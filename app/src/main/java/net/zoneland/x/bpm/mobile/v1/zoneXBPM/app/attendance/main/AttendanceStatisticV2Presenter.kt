package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2StatisticBody
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 28/05/2018.
 * Copyright © 2018 O2. All rights reserved.
 */

class AttendanceStatisticV2Presenter : BasePresenterImpl<AttendanceStatisticV2Contract.View>(),
    AttendanceStatisticV2Contract.Presenter {


    override fun loadMyStatistic(startDate: String, endDate: String) {
        XLog.debug("查询日期，start: $startDate end: $endDate")
        val service = getAttendanceAssembleControlService(mView?.getContext())
        service?.attendanceV2MyStatistic(AttendanceV2StatisticBody(startDate, endDate))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())?.o2Subscribe {
                onNext {
                    if (it != null && it.data != null) {
                        mView?.myStatistic(it.data)
                    }
                }
                onError { e, _ ->
                    XLog.error("", e)
                }
            }
    }
}