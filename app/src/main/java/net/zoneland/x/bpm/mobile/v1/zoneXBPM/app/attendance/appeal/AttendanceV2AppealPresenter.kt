package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.appeal

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.exception.O2ResponseException
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2AppealInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2AppealPageListFilter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2023-04-12.
 * Copyright © 2023 o2android. All rights reserved.
 */
class AttendanceV2AppealPresenter : BasePresenterImpl<AttendanceV2AppealContract.View>(),
    AttendanceV2AppealContract.Presenter {

    override fun config() {
        val service = getAttendanceAssembleControlService(mView?.getContext())
        service?.attendanceV2Config()?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
            ?.o2Subscribe {
                onNext {
                    mView?.config(it.data)
                }
                onError { e, _ ->
                    XLog.error("", e)
                }
            }
    }

    override fun appealStartedProcess(id: String) {
        val service = getAttendanceAssembleControlService(mView?.getContext())
        if (service != null) {
            service.attendanceV2AppealStartProcess(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.appealStartedProcess(it?.data?.isValue == true)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.appealStartedProcess(false)
                    }
                }
        } else {
            mView?.appealStartedProcess(false)
        }
    }

    override fun checkAppeal(appealInfo: AttendanceV2AppealInfo) {
        val service = getAttendanceAssembleControlService(mView?.getContext())
        if (service != null) {
            service.attendanceV2CheckCanAppeal(appealInfo.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.checkAppealResult(it?.data?.isValue == true, appealInfo)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        if (e is O2ResponseException) {
                            XToast.toastShort(mView?.getContext(), e.message ?: "无法处理！")
                        }
                        mView?.checkAppealResult(false, appealInfo)
                    }
                }
        } else {
            mView?.checkAppealResult(false, appealInfo)
        }
    }

    override fun myAppealListByPage(page: Int) {
        val service = getAttendanceAssembleControlService(mView?.getContext())
        if (service != null) {
            service.attendanceV2MyAppealListByPage(
                page,
                O2.DEFAULT_PAGE_NUMBER,
                AttendanceV2AppealPageListFilter()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it != null && it.data != null) {
                            mView?.appealList(it.data)
                        } else {
                            mView?.appealList(arrayListOf())
                        }
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.appealList(arrayListOf())
                    }
                }
        } else {
            mView?.appealList(arrayListOf())
        }
    }
}