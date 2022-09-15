package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.fm

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2022-07-13.
 * Copyright © 2022 o2android. All rights reserved.
 */
class O2IMConversationPickerPresenter: BasePresenterImpl<O2IMConversationPickerContract.View>(), O2IMConversationPickerContract.Presenter {


    override fun getMyConversationList() {
        val service = getMessageCommunicateService(mView?.getContext())
        if (service == null) {
            mView?.myConversationList(ArrayList())
            return
        }
        service.myConversationList().subscribeOn(Schedulers.io())
            .flatMap { res ->
                val list = res.data
                if ( list != null && list.isNotEmpty()) {
                    val newList = list.sortedByDescending { c -> c.lastMessage?.createTime  }
                    Observable.just(newList)
                }else {
                    Observable.just(ArrayList())
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext { list->
                    if (list != null) {
                        mView?.myConversationList(list)
                    }else{
                        mView?.myConversationList(ArrayList())
                    }
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.myConversationList(ArrayList())
                }
            }
    }
}