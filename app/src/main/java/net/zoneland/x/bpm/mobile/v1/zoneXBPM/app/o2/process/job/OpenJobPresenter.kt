package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.job

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2023-04-13.
 * Copyright © 2023 o2android. All rights reserved.
 */
class OpenJobPresenter: BasePresenterImpl<OpenJobContract.View>(), OpenJobContract.Presenter  {
    override fun getWorkByJobId(jobId: String) {
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            service.getWorkOrWorkcompletedByJobId(jobId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.workOrWorkCompletedResult(it.data)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.workOrWorkCompletedResult(null)
                    }
                }
        } else {
            mView?.workOrWorkCompletedResult(null)
        }
    }
}