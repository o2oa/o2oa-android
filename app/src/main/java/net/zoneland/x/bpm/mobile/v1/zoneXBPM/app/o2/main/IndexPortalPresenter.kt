package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.CmsFilter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import okhttp3.MediaType
import okhttp3.RequestBody
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 21/03/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


class IndexPortalPresenter : BasePresenterImpl<IndexPortalContract.View>(), IndexPortalContract.Presenter {

//    override fun loadCmsCategoryListByAppId(appId: String) {
//        getCMSAssembleControlService(mView?.getContext())
//                ?.findCategorysByAppId(appId)
//                ?.subscribeOn(Schedulers.io())
//                ?.observeOn(AndroidSchedulers.mainThread())
//                ?.o2Subscribe {
//                    onNext {
//                        mView?.loadCmsCategoryListByAppId(it.data ?: arrayListOf())
//                    }
//                    onError { e, _ ->
//                        XLog.error("", e)
//                        mView?.loadCmsCategoryListByAppId(arrayListOf())
//                    }
//                }
//    }

    override fun findDocumentDraftWithCategory(categoryId: String) {
        val put = CmsFilter()
        val cateList = ArrayList<String>()
        cateList.add(categoryId)
        put.categoryIdList = cateList
        val personList = ArrayList<String>()
        personList.add(O2SDKManager.instance().distinguishedName)
        put.creatorList= personList
        val json = O2SDKManager.instance().gson.toJson(put)
        val body = RequestBody.create(MediaType.parse("text/json"), json)

        getCMSAssembleControlService(mView?.getContext())?.findDocumentDraftListWithCategory(body)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.o2Subscribe {
                onNext {
                    if (it.data!=null) {
                        mView?.documentDraft(it.data)
                    }else{
                        mView?.documentDraft(ArrayList())
                    }
                }
                onError { e, isNetworkError ->
                    XLog.error("查询草稿列表错误, netErr: $isNetworkError", e)
                    mView?.documentDraft(ArrayList())
                }
            }
    }


    override fun loadCmsApplication(appId: String) {
        val service = getCMSAssembleControlService(mView?.getContext())
        service
            ?.canPublishCategories(appId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.o2Subscribe {
                onNext {
                   mView?.cmsApplication(it.data)
                }
                onError { e, isNetworkError ->
                    XLog.error("查询发布列表出错, netErr: $isNetworkError", e)
                }
            }
    }

    override fun openCmsApplication(appId: String) {
        val service = getCMSAssembleControlService(mView?.getContext())
        if (service != null) {
            service.applicationList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        val list = it.data
                        if (list == null || list.isEmpty()) {
                            mView?.openCmsApplication(null)
                        } else {
                            val app = list.find { a -> a.id == appId }
                            mView?.openCmsApplication(app)
                        }
                    }
                }
        } else {
            mView?.openCmsApplication(null)
        }
    }
}