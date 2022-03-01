package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.text.TextUtils
import net.muliba.accounting.app.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class StartProcessStepOnePresenter : BasePresenterImpl<StartProcessStepOneContract.View>(), StartProcessStepOneContract.Presenter {

    override fun loadApplicationListWithProcess() {

        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.getApplicationProcessList()
                .subscribeOn(Schedulers.io())
                .flatMap {// 过滤不是移动端的流程和应用
                    val oldList = it.data
                    var newList = ArrayList<ApplicationWithProcessData>()
                    if (oldList != null && oldList.isNotEmpty()) {
                        oldList.map { item ->
                            val pList = item.processList
                            if (pList != null &&  pList.isNotEmpty() ) {
                                val newPlist = pList.filter { p -> "client" != p.startableTerminal }.toList()
                                if (newPlist.isNotEmpty()) {
                                    item.processList = newPlist
                                    newList.add(item)
                                }
                            }
                        }
                    }
                    Observable.just(newList)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.loadApplicationListWithProcess(it)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.loadApplicationListFail()
                    }
                }
        }
    }

//    override fun loadApplicationList() {
//        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
//            service.getApplicationList()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(ResponseHandler<List<ApplicationData>>({list-> mView?.loadApplicationList(list)}),
//                            ExceptionHandler(mView?.getContext(), {e-> mView?.loadApplicationListFail()}))
//        }
//    }

    override fun loadProcessListByAppId(appId: String) {
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            val filter = ApplicationProcessFilter()
            //先用新接口查询
            service.getApplicationProcessFilter(appId, filter)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe { 
                        onNext { res ->
                            mView?.loadProcessList(res.data)
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            //用老接口查询
                            service.getApplicationProcess(appId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .o2Subscribe {
                                        onNext { res1 ->
                                            mView?.loadProcessList(res1.data)
                                        }
                                        onError { e, _ ->
                                            XLog.error("", e)
                                            mView?.loadProcessListFail()
                                        }
                                    }
                        }
                    }

        }
    }

    override fun loadCurrentPersonIdentityWithProcess(processId: String) {
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.availableIdentityWithProcess(processId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<List<ProcessWOIdentityJson>>({ list ->
                        XLog.debug("identities: $list")
                        mView?.loadCurrentPersonIdentity(list)
                    }),
                            ExceptionHandler(mView?.getContext(), { e -> mView?.loadCurrentPersonIdentityFail() }))
        }
    }

    override fun startProcess(identity: String, processId: String) {
        if (TextUtils.isEmpty(identity) || TextUtils.isEmpty(processId)) {
            val emptyMsg = mView?.getContext()?.getString(R.string.message_start_process_fail, identity, processId)
            mView?.startProcessFail(emptyMsg ?: "传入参数为空，无法启动流程, identity:$identity,processId:$processId")
            return
        }
        val body = ProcessStartBo()
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
                    }, ExceptionHandler(mView?.getContext(), { e ->
                        mView?.startProcessFail(e.message ?: "")
                    }))
        }
    }

    override fun startDraft(identity: String, processId: String) {
        if (TextUtils.isEmpty(identity) || TextUtils.isEmpty(processId)) {
            val emptyMsg = mView?.getContext()?.getString(R.string.message_start_process_fail, identity, processId)
            mView?.startProcessFail(emptyMsg ?: "传入参数为空，无法启动流程, identity:$identity,processId:$processId")
            return
        }
        val body = ProcessStartBo()
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
