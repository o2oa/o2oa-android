package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchV2Form
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchV2PageModel
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2021-05-27.
 * Copyright © 2021 O2. All rights reserved.
 */
class SearchV2Presenter : BasePresenterImpl<SearchV2Contract.View>(), SearchV2Contract.Presenter  {


    override fun search(post: O2SearchV2Form) {
        val service = getQueryAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            service.searchV2(post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        val result = it.data
                        if (result != null) {
                            mView?.searchResult(result)
                        } else {
                            mView?.searchResult(O2SearchV2PageModel())
                        }
                    }
                }
        } else {
            mView?.searchResult(O2SearchV2PageModel())
        }
    }

    override fun getWorkByJobId(jobId: String, title: String) {
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            service.getWorkOrWorkcompletedByJobId(jobId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.workOrWorkcompletedResult(it.data, title)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.workOrWorkcompletedResult(null, title)
                    }
                }
        } else {
            mView?.workOrWorkcompletedResult(null, title)
        }
    }
}