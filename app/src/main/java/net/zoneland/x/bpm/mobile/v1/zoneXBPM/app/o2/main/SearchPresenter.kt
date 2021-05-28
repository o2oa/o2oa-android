package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchEntry
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchEntryForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchPageModel
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.Exception

/**
 * Created by fancyLou on 2021-05-27.
 * Copyright © 2021 O2. All rights reserved.
 */
class SearchPresenter : BasePresenterImpl<SearchContract.View>(), SearchContract.Presenter  {


    private var page = 1
    private var totalPage = 1
    private val idsList: ArrayList<String> = arrayListOf()


    override fun search(key: String) {
        val service = getQueryAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            service.segmentSearch(key).subscribeOn(Schedulers.io())
                .flatMap { res ->
                    val ids = res.data
                    idsList.clear()
                    idsList.addAll(ids.valueList)
                    totalPage = if (idsList.size > O2.DEFAULT_PAGE_NUMBER) {
                        val m = idsList.size % O2.DEFAULT_PAGE_NUMBER
                        if (m > 0) {
                            idsList.size / O2.DEFAULT_PAGE_NUMBER + 1
                        } else {
                            idsList.size / O2.DEFAULT_PAGE_NUMBER
                        }
                    } else {
                        1
                    }
                    page = 1
                    getEntryListByIds()
                }
                .observeOn(AndroidSchedulers.mainThread()).o2Subscribe {
                    onNext {
                        val pageModel = O2SearchPageModel()
                        pageModel.page = page
                        pageModel.totalPage = totalPage
                        pageModel.list = it.data
                        mView?.searchResult(pageModel)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(mView?.getContext()?.getString(R.string.search_error,  e?.localizedMessage) ?: "错误，"+ e?.localizedMessage)
                    }
                }
        } else {
            mView?.error(mView?.getContext()?.getString(R.string.search_service_error) ?: "查询接口异常，服务器不存在！")
        }
    }

    private fun getEntryListByIds(): Observable<ApiResponse<List<O2SearchEntry>>> {
        val ids = if (idsList.isNotEmpty()) {
            val start = (page - 1) * O2.DEFAULT_PAGE_NUMBER
            var end = start + O2.DEFAULT_PAGE_NUMBER
            if (end > totalPage) {
                end = totalPage
            }
            idsList.subList(start, end)
        } else {
            arrayListOf()
        }
        return Observable.just(ids).flatMap {
            val service = getQueryAssembleSurfaceServiceAPI(mView?.getContext())
            if (service != null) {
                val form = O2SearchEntryForm()
                form.entryList = it.toList()
                service.segmentListEntry(form)
            } else {
                throw Exception(mView?.getContext()?.getString(R.string.search_service_error) ?: "查询接口异常，服务器不存在！")
            }
        }
    }

    override fun nextPage() {
        if (page < totalPage) {
            page++
            getEntryListByIds().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        val pageModel = O2SearchPageModel()
                        pageModel.page = page
                        pageModel.totalPage = totalPage
                        pageModel.list = it.data
                        mView?.searchResult(pageModel)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(mView?.getContext()?.getString(R.string.search_error,  e?.localizedMessage) ?: "错误，"+ e?.localizedMessage)
                    }
                }
        }
    }

    override fun getTotalPage(): Int  = totalPage

    override fun getPage(): Int = page

    override fun hasNexPage(): Boolean {
        return page < totalPage
    }
}