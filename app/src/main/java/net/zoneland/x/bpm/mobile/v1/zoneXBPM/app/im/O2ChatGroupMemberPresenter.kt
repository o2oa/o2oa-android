package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationUpdateForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2021-01-13.
 * Copyright © 2021 O2. All rights reserved.
 */
class O2ChatGroupMemberPresenter : BasePresenterImpl<O2ChatGroupMemberContract.View>(), O2ChatGroupMemberContract.Presenter {

    override fun updateConversationPeople(id: String, users: ArrayList<String>) {
        if (id.isEmpty() || users.isEmpty()) {
            mView?.updateFail(mView?.getContext()?.getString(R.string.message_arg_error) ?:"参数不正确，无法修改")
            return
        }
        if (users.size < 3) {
            mView?.updateFail(mView?.getContext()?.getString(R.string.message_members_cannot_less_three) ?:"成员不能少于3人")
            return
        }
        val service = getMessageCommunicateService(mView?.getContext())
        val form = IMConversationUpdateForm()
        form.id = id
        form.personList = users
        service?.updateConversation(form)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.o2Subscribe {
                    onNext {
                        if (it.data != null) {
                            mView?.updateSuccess(it.data)
                        } else {
                            mView?.updateFail(it.message)
                        }
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.updateFail(e?.message ?: "修改失败")
                    }
                }
    }

}