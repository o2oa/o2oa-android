package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.EmpowerData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2023-05-18.
 * Copyright © 2023 o2android. All rights reserved.
 */
class EmpowerCreatePresenter: BasePresenterImpl<EmpowerCreateContract.View>(), EmpowerCreateContract.Presenter {

    override fun loadMyIdentity() {
        getAssemblePersonalApi(mView?.getContext())?.let { service->
            service.getCurrentPersonInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        val person = it.data
                        if (person != null) {
                            mView?.loadMyIdentity(person.woIdentityList)
                        } else {
                            mView?.loadMyIdentity(ArrayList())
                        }
                    }
                    onError { e, isNetworkError ->
                        XLog.error("", e)
                        mView?.loadMyIdentity(ArrayList())
                    }
                }
        }
    }

    override fun createEmpower(data: EmpowerData) {
        getAssemblePersonalApi(mView?.getContext())?.let { service ->
            service.postEmpower(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.createBack(true, null)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.createBack(false, e?.message)
                    }
                }
        }
    }
}