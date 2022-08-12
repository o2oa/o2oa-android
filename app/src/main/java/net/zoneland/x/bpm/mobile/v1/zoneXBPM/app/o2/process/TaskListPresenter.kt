package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class TaskListPresenter : BasePresenterImpl<TaskListContract.View>(), TaskListContract.Presenter {

    override fun findTaskApplicationList() {
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.getTaskApplicationList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler { list -> mView?.findTaskApplicationList(list) },
                            ExceptionHandler(mView?.getContext()) { e -> mView?.findTaskApplicationListFail() })
        }
    }
}
