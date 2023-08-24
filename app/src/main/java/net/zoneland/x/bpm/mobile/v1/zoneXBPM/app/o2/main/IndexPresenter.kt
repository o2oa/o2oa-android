package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import com.xiaomi.push.id
import com.xiaomi.push.it
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.ApplicationEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.realm.RealmDataService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchV2Form
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.cms.CMSDocumentFilter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.HotPictureOutData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskFilter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.persistence.MyAppListObject
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import okhttp3.MediaType
import okhttp3.RequestBody
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancy on 2017/6/9.
 */

class IndexPresenter : BasePresenterImpl<IndexContract.View>(), IndexContract.Presenter {

    override fun checkIsSearchV2() {
        val service = getQueryAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            service.searchV2(O2SearchV2Form())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.searchVersion(true)
                    }
                    onError { e, isNetworkError ->
                        XLog.error("", e)
                        mView?.searchVersion(false)
                    }
                }
        } else {
            mView?.searchVersion(false)
        }
    }

    override fun loadTaskListByPage(page: Int) {
        val processSet = O2SDKManager.instance().prefs().getStringSet(O2CustomStyle.CUSTOM_STYLE_INDEX_FILTER_PROCESS_KEY, null) ?: HashSet<String>()
        val filter = TaskFilter()
        val processList =  ArrayList<String>()
        processSet.forEach {
            processList.add(it)
        }
        filter.processList = processList
        val json = O2SDKManager.instance().gson.toJson(filter)
        XLog.debug(json)
        val body = RequestBody.create(MediaType.parse("application/json"), json)
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service ->
            service.getTaskListMyFilterPaging(page, O2.DEFAULT_PAGE_NUMBER, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ResponseHandler { list -> mView?.loadTaskList(list) },
                    ExceptionHandler(mView?.getContext()) { e ->
                        XLog.error("", e)
                        mView?.loadTaskListFail()
                    }
                )
        }
    }



    override fun loadNewsListByPage(page: Int) {
        XLog.debug("获取新闻列表 page $page")
        val status = ArrayList<String>()
        status.add("published")
        val categorySet = O2SDKManager.instance().prefs().getStringSet(O2CustomStyle.CUSTOM_STYLE_INDEX_FILTER_CATEGORY_KEY, null) ?: HashSet<String>()
        val categoryIdList =  ArrayList<String>()
        categorySet.forEach {
            categoryIdList.add(it)
        }
        val filter = CMSDocumentFilter()
        filter.statusList = status
        filter.categoryIdList = categoryIdList
        filter.justData = true
        val json = O2SDKManager.instance().gson.toJson(filter)
        XLog.debug(json)
        val body = RequestBody.create(MediaType.parse("application/json"), json)
        getCMSAssembleControlService(mView?.getContext())?.let { service ->
            service.filterDocumentListByPaging(body, page, O2.DEFAULT_PAGE_NUMBER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { response->
                        if (response.data!=null) {
                            mView?.loadNewsList(response.data)
                        }else{
                            mView?.loadNewsListFail()
                        }
                    }
                    onError { e, isNetworkError ->
                        XLog.error("获取新闻出错，$isNetworkError", e)
                        mView?.loadNewsListFail()
                    }
                }

        }
    }
    override fun loadNewsList(lastId: String) {
        XLog.debug("获取新闻列表 $lastId")
        val status = ArrayList<String>()
        status.add("published")
        val filter = CMSDocumentFilter()
        filter.statusList = status
        val json = O2SDKManager.instance().gson.toJson(filter)
        XLog.debug(json)
        val body = RequestBody.create(MediaType.parse("text/json"), json)
        getCMSAssembleControlService(mView?.getContext())?.let { service ->
            service.filterDocumentList(body, lastId, O2.DEFAULT_PAGE_NUMBER)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext { response->
                            if (response.data!=null) {
                                mView?.loadNewsList(response.data)
                            }else{
                                mView?.loadNewsListFail()
                            }
                        }
                        onError { e, isNetworkError ->
                            XLog.error("获取新闻出错，$isNetworkError", e)
                            mView?.loadNewsListFail()
                        }
                    }

        }
    }

    override fun loadHotPictureList() {
        val body = RequestBody.create(MediaType.parse("text/json"), "{}")
        getHotPicAssembleControlServiceApi(mView?.getContext())?.let { service ->
            service.findHotPictureList(1, O2.SETTING_HOT_PICTURE_DEFAULT_SHOW_NUMBER, body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<List<HotPictureOutData>>({ list -> mView?.loadHotPictureList(list) }),
                            ExceptionHandler(mView?.getContext(), { e -> mView?.loadHotPictureListFail() })
                    )
        }
    }

    override fun getMyAppList() {
        Observable.zip(RealmDataService().findAllNativeApp(), RealmDataService().findAllPortalList(), RealmDataService().findMyAppList()){ all, allPortal, my ->
            val list = ArrayList<MyAppListObject>()
            if (my.isEmpty()) {
                for (appItemOnlineVo  in all) {
                    if (list.size < 4 && appItemOnlineVo.enable) {
                        val myObj = MyAppListObject()
                        myObj.appId = appItemOnlineVo.key
                        myObj.appTitle = appItemOnlineVo.name
                        list.add(myObj)
                    }
                }
            }else {
                for (myAppListObject in my) {
                    if (all.any { vo -> vo.key == myAppListObject.appId && vo.enable } || allPortal.any{ p -> p.id == myAppListObject.appId && p.enable}) {
                        list.add(myAppListObject)
                    }
                }
//                list.addAll(my)
            }
            list
        }.subscribeOn(Schedulers.io())
//                .flatMap { result ->
//                    XLog.debug("getmyApplist..........................${result.size}.")
//                    val list = ArrayList<MyAppListObject>()
//                    if (result.isEmpty()) {
//                        ApplicationEnum.values().mapIndexed { index, applicationEnum ->
//                            if (index < 4) {
//                                val myObj = MyAppListObject()
//                                myObj.appId = applicationEnum.key
//                                myObj.appTitle = applicationEnum.appName
//                                list.add(myObj)
//                            }
//                        }
//                    } else {
//                        list.addAll(result)
//                    }
//                    Observable.just(list)
//                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            XLog.debug("success.................${result.size}")
                            mView?.setMyAppList(result ) },
                        { e -> XLog.error("", e)
                            val list = ArrayList<MyAppListObject>()
                            ApplicationEnum.values().mapIndexed { index, applicationEnum ->
                                if (index < 4) {
                                    val myObj = MyAppListObject()
                                    myObj.appId = applicationEnum.key
                                    myObj.appTitle = applicationEnum.appName
                                    list.add(myObj)
                                }
                            }
                            mView?.setMyAppList(list)
                        }
                )
    }

}