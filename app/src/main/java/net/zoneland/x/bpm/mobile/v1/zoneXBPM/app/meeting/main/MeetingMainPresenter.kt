package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.meeting.main

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MeetingMainPresenter : BasePresenterImpl<MeetingMainContract.View>(), MeetingMainContract.Presenter {
    override fun getMeetingById(id: String) {
        if (TextUtils.isEmpty(id)) {
            mView?.error("参数不能为空！")
            return
        }
        getMeetingAssembleControlService(mView?.getContext())?.let { service ->
            service.getMeetingById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.getMeetingById(it.data)
                    }
                    onError { e, isNetworkError ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "请求异常！")
                    }
                }
        }
    }

}
