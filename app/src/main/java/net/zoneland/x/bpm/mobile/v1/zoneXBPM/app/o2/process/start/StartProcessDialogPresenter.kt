package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.start

import android.text.TextUtils
import com.google.gson.JsonElement
import com.xiaomi.push.it
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessStartWithDataBo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2023-04-12.
 * Copyright © 2023 o2android. All rights reserved.
 */
class StartProcessDialogPresenter: BasePresenterImpl<StartProcessDialogContract.View>(), StartProcessDialogContract.Presenter {

    override fun getProcess(processId: String) {
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            service.getProcess(processId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.getProcess(it.data)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.getProcess(null)
                    }
                }
        } else {
            mView?.getProcess(null)
        }
    }

    override fun loadCurrentPersonIdentityWithProcess(processId: String) {
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null)  {
            service.availableIdentityWithProcess(processId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.loadCurrentPersonIdentity(it?.data ?: ArrayList())
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.loadCurrentPersonIdentity(ArrayList())
                    }
                }
        } else {
            mView?.loadCurrentPersonIdentity(ArrayList())
        }
    }

    override fun startProcess(identity: String, processId: String, data: JsonElement?) {
        if (TextUtils.isEmpty(identity) || TextUtils.isEmpty(processId)) {
            val emptyMsg = mView?.getContext()?.getString(R.string.message_start_process_fail, identity, processId)
            mView?.startProcessFail(emptyMsg ?: "传入参数为空，无法启动流程, identity:$identity,processId:$processId")
            return
        }
        val body = ProcessStartWithDataBo()
        body.title = ""
        body.identity = identity
        body.data = data
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null)  {
            service.startProcess(processId, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        try {
                            mView?.startProcessSuccess(it.data[0].taskList[0])
                        } catch (e: Exception) {
                            XLog.error("", e)
                            val error = mView?.getContext()?.getString(R.string.message_start_process_back_data_error, e.message)
                            XLog.error( error ?: "返回数据异常， 没有待办！${e.message}")
                            //mView?.startProcessFail(error ?: "返回数据异常！${e.message}")
                            mView?.startProcessSuccessNoWork()
                        }
                    }
                    onError { e, isNetworkError ->
                        XLog.error("", e)
                        mView?.startProcessFail(e?.message ?: "")
                    }
                }
        } else {
            mView?.startProcessFail("")
        }
    }

    override fun startDraft(identity: String, processId: String, data: JsonElement?) {
        if (TextUtils.isEmpty(identity) || TextUtils.isEmpty(processId)) {
            val emptyMsg = mView?.getContext()?.getString(R.string.message_start_process_fail, identity, processId)
            mView?.startProcessFail(emptyMsg ?: "传入参数为空，无法启动流程, identity:$identity,processId:$processId")
            return
        }
        val body = ProcessStartWithDataBo()
        body.title = ""
        body.identity = identity
        body.data = data
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null)  {
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
        } else {
            mView?.startDraftFail( "")
        }
    }
}