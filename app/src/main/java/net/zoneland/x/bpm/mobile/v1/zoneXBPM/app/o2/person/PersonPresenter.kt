package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.person

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.realm.RealmDataService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class PersonPresenter : BasePresenterImpl<PersonContract.View>(), PersonContract.Presenter {

    override fun loadPersonInfo(name: String) {
        getOrganizationAssembleControlApi(mView?.getContext())?.let { service ->
            service.person(name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler { person -> mView?.loadPersonInfo(person) },
                            ExceptionHandler(mView?.getContext()) { _ -> mView?.loadPersonInfoFail() })
        }
    }

    override fun collectionUsuallyPerson(owner: String, person: String, ownerDisplay: String, personDisplay: String, gender: String, mobile: String) {
        RealmDataService().saveUsuallyPerson(owner, person, ownerDisplay, personDisplay, gender, mobile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    override fun deleteUsuallyPerson(owner: String, person: String) {
        RealmDataService().deleteUsuallyPerson(owner, person).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    override fun isUsuallyPerson(owner: String, person: String) {
        mView?.let {
            RealmDataService().isUsuallyPerson(owner, person)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ data -> it.isUsuallyPerson(data) }, { e ->
                        XLog.error("", e)
                        it.isUsuallyPerson(false)
                    })
        }

    }

    override fun startSingleTalk(user: String) {
        val service = getMessageCommunicateService(mView?.getContext())
        if (service != null) {
            val info = IMConversationInfo()
            info.type = "single"
            info.personList = arrayListOf(user)
            service.createConversation(info)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext {
                            if (it.data!= null) {
                                mView?.createConvSuccess(it.data)
                            }else{
                                mView?.createConvFail(mView?.getContext()?.getString(R.string.message_create_conversation_fail) ?: "创建会话失败！")
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            mView?.createConvFail(mView?.getContext()?.getString(R.string.message_create_conversation_fail) ?: "创建会话失败！${e?.message}")
                        }
                    }
        }
    }
}
