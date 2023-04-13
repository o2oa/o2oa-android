package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessStartBo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessStartWithDataBo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class StartProcessStepTwoPresenter : BasePresenterImpl<StartProcessStepTwoContract.View>(), StartProcessStepTwoContract.Presenter {

    override fun loadCurrentPersonIdentityWithProcess(processId: String) {
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.availableIdentityWithProcess(processId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<List<ProcessWOIdentityJson>> { list ->
                        XLog.debug("identities: $list")
                        mView?.loadCurrentPersonIdentity(list)
                    },
                            ExceptionHandler(mView?.getContext(), { e -> mView?.loadCurrentPersonIdentityFail() })
                    )
        }
    }

    override fun startProcess(title: String, identity: String, processId: String) {
            if (TextUtils.isEmpty(identity) || TextUtils.isEmpty(processId)) {
                val emptyMsg = mView?.getContext()?.getString(R.string.message_start_process_fail, identity, processId)
                mView?.startProcessFail(emptyMsg ?: "传入参数为空，无法启动流程 identity:$identity,processId:$processId")
                return
            }
            val body = ProcessStartWithDataBo()
            body.title = ""
            body.identity = identity
            getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
                service.startProcess(processId, body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<List<ProcessWorkData>> { list ->
                        try {
                            mView?.startProcessSuccess(list[0].taskList[0].work)
                        } catch (e: Exception) {
                            XLog.error("", e)
                            val error = mView?.getContext()?.getString(R.string.message_start_process_back_data_error, e.message)
                            XLog.error( error ?: "返回数据异常， 没有待办！${e.message}")
                            //mView?.startProcessFail(error ?: "返回数据异常！${e.message}")
                            mView?.startProcessSuccessNoWork()
                        }
                    }, ExceptionHandler(mView?.getContext()) { e ->
                        mView?.startProcessFail(e.message ?: "")
                    })
        }
    }

    override fun startDraft(title: String, identity: String, processId: String) {
        if (TextUtils.isEmpty(identity) || TextUtils.isEmpty(processId)) {
            val emptyMsg = mView?.getContext()?.getString(R.string.message_start_process_fail, identity, processId)
            mView?.startProcessFail(emptyMsg ?: "传入参数为空，无法启动流程, identity:$identity,processId:$processId")
            return
        }
        val body = ProcessStartWithDataBo()
        body.title = ""
        body.identity = identity
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.startDraft(processId, body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext {
                            if (it.data != null) {
                                mView?.startDraftSuccess(it.data.work)
                            }else {
                                mView?.startDraftFail(mView?.getContext()?.getString(R.string.message_open_draft_error) ?: "打开草稿异常！")
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            mView?.startDraftFail(e?.message ?: "")
                        }
                    }
        }
    }
}
