package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.exception.O2ResponseException
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2023-05-16.
 * Copyright © 2023 o2android. All rights reserved.
 */
class EmpowerListPresenter: BasePresenterImpl<EmpowerListContract.View>(), EmpowerListContract.Presenter {
    override fun myEmpowerList() {
        getAssemblePersonalApi(mView?.getContext())?.let { service->
            service.myEmpowerList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.myEmpowerList(it.data)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        val msg = if (e is O2ResponseException) {
                            e.message ?: "请求错误"
                        } else {
                            "请求错误"
                        }
                        mView?.error( msg )
                    }
                }
        }
    }

    override fun myEmpowerListTo() {
        getAssemblePersonalApi(mView?.getContext())?.let { service ->
            service.myEmpowerListTo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.myEmpowerListTo(it.data)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        val msg = if (e is O2ResponseException) {
                            e.message ?: "请求错误"
                        } else {
                            "请求错误"
                        }
                        mView?.error( msg )
                    }
                }
        }
    }

}