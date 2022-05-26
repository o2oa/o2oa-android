package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.text.TextUtils
import net.muliba.accounting.app.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2App
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.WorkTypeEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.SearchWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskCompleteData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import okhttp3.MediaType
import okhttp3.RequestBody
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class TaskCompletedSearchPresenter : BasePresenterImpl<TaskCompletedSearchContract.View>(), TaskCompletedSearchContract.Presenter {

    override fun search (lastId: String, key: String, searchType: WorkTypeEnum) {
        if (TextUtils.isEmpty(lastId) || TextUtils.isEmpty(key)) {
            mView?.searchFail()
            XLog.error( "传入参数不正确！")
            return
        }
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            val map = HashMap<String, String>()
            map["key"] = key
            val json = O2SDKManager.instance().gson.toJson(map)
            XLog.debug("searchTask json : $json")
            val body = RequestBody.create(MediaType.parse("text/json"), json)
            val searchTask = when(searchType) {
                WorkTypeEnum.Task -> service.searchTaskListByPage(lastId, O2.DEFAULT_PAGE_NUMBER, body)
                WorkTypeEnum.TaskCompleted -> service.searchTaskCompleteListByPage(lastId, O2.DEFAULT_PAGE_NUMBER, body)
                WorkTypeEnum.READ -> service.searchReadListByPage(lastId, O2.DEFAULT_PAGE_NUMBER, body)
                WorkTypeEnum.ReadCompleted -> service.searchReadCompleteListByPage(lastId, O2.DEFAULT_PAGE_NUMBER, body)
            }
            searchTask
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ResponseHandler<List<SearchWorkData>>{list->mView?.searchResult(list)},
                    ExceptionHandler(mView?.getContext()){e->mView?.searchFail()})
        } else {
            mView?.searchFail()
        }
    }
}
