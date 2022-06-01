package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.bbs.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.realm.RealmDataService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class BBSMainPresenter : BasePresenterImpl<BBSMainContract.View>(), BBSMainContract.Presenter {

    override fun whetherThereHasCollections() {
        mView?.let {
            RealmDataService().hasAnyBBSCollection()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ aBoolean ->
                        it.whetherThereHasAnyCollections(aBoolean)
                    }, { e ->
                        XLog.error("", e)
                        it.whetherThereHasAnyCollections(false)
                    })
        }
    }

    override fun checkHasMute() {
        getBBSAssembleControlService(mView?.getContext())?.let { service ->
            service.getMuteInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it != null && it.data!=null) {
                            O2SDKManager.instance().bbsMuteInfo = it.data
                        } else {
                            O2SDKManager.instance().bbsMuteInfo = null
                        }
                    }
                    onError { e, _ ->
                        O2SDKManager.instance().bbsMuteInfo = null
                        XLog.error("", e)
                    }
                }
        }
    }
}
