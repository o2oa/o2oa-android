package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service.OrganizationAssembleControlAlphaService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.UnitDutyIdentityForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.person.PersonList
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.unit.UnitJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.unit.UnitListForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.NewContactListVO
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2019-08-20.
 * Copyright © 2019 O2. All rights reserved.
 */

class ContactUnitAndIdentityPickerPresenter: BasePresenterImpl<ContactUnitAndIdentityPickerContract.View>(), ContactUnitAndIdentityPickerContract.Presenter {

    override fun loadUnitWithParent(parent: String, isLoadIdentity: Boolean, topList: List<String>, orgType: String, dutyList: List<String>) {
        XLog.debug("loadUnitWithParent parent:$parent , isLoadIdentity:$isLoadIdentity ,topList:$topList orgType:$orgType dutyList:$dutyList")
        val service = getOrganizationAssembleControlApi(mView?.getContext())
        val assService = getAssembleExpressApi(mView?.getContext())
        if(service!=null && assService!=null) {
            if (parent == "-1") {
                if (TextUtils.isEmpty(orgType)) {
                    val observable = if (topList.isNotEmpty()) {
                        service.unitList(UnitListForm(topList))
                    }else {
                        service.unitListTop()
                    }
                    observable.flatMap { response ->
                        val retList = ArrayList<NewContactListVO>()
                        val list = response.data
                        if (list != null && list.isNotEmpty()) {
                            list.map { retList.add(it.copyToOrgVO()) }
                        }
                        Observable.just(retList)
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Action1<ArrayList<NewContactListVO>> { list -> mView?.callbackResult(list) },
                                    ExceptionHandler(mView?.getContext()) { e -> mView?.backError(e.message ?: "") })
                } else {
//                    if (topList.isNotEmpty()) {
//                        service.unitList(UnitListForm(topList)).flatMap { response ->
//                            val retList = ArrayList<NewContactListVO>()
//                            val list = response.data
//                            if (list != null && list.isNotEmpty()) {
//                                list.map {
//                                    if (it.typeList.contains(orgType)) {
//                                        retList.add(it.copyToOrgVO())
//                                    }
//                                }
//                            }
//                            Observable.just(retList)
//                        }.subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(Action1<ArrayList<NewContactListVO>> { list -> mView?.callbackResult(list) },
//                                        ExceptionHandler(mView?.getContext()) { e -> mView?.backError(e.message ?: "") })
//                    }else {
                        searchUnitWithType(service, orgType, topList)
//                    }
                }
            } else {
                if (isLoadIdentity) {
                    val unitObservable = service.unitSubDirectList(parent).map { response ->
                        val retList = ArrayList<NewContactListVO>()
                        val list = response.data
                        if (list != null && list.isNotEmpty()) {
                            list.map { retList.add(it.copyToOrgVO()) }
                        }
                        retList
                    }
                    val identityObservable = if (dutyList.isEmpty()){
                        service.identityListWithUnit(parent).map { response ->
                            val retList = ArrayList<NewContactListVO>()
                            val idList = ArrayList<String>()
                            val list = response.data
                            if (list != null && list.isNotEmpty()) {
                                list.map {
                                    idList.add(it.person)
                                    retList.add(it.copyToOrgVO())
                                }
                            }
                            //这里需要把person的dn查询出来
                            if (idList.isNotEmpty()) {
                                assService.searchPersonDNList(PersonList(idList)).observeOn(Schedulers.immediate()).o2Subscribe {
                                    onNext { assRes ->
                                        val dnList = assRes.data.personList
                                        if (dnList.isNotEmpty()) {
                                            retList.forEachIndexed { index, identity ->
                                                (identity as NewContactListVO.Identity).person = dnList[index]
                                            }
                                        }
                                    }
                                    onError { e, isNetworkError ->
                                        XLog.error("$isNetworkError", e)
                                    }
                                }
//                                XLog.debug("查询personDN完成。。。。$retList")
                            }
                            retList
                        }
                    } else {
                        val form = UnitDutyIdentityForm()
                        form.unit = parent
                        form.nameList = dutyList
                        assService.identityListByUnitAndDuty(form).map { response ->
                            val retList = ArrayList<NewContactListVO>()
                            val idList = ArrayList<String>()
                            val list = response.data
                            if (list != null && list.isNotEmpty()) {
                                list.map { retList.add(it.copyToOrgVO()) }
                            }
                            //这里需要把person的dn查询出来
                            if (idList.isNotEmpty()) {
                                assService.searchPersonDNList(PersonList(idList)).observeOn(Schedulers.immediate()).o2Subscribe {
                                    onNext { assRes ->
                                        val dnList = assRes.data.personList
                                        if (dnList.isNotEmpty()) {
                                            retList.forEachIndexed { index, identity ->
                                                (identity as NewContactListVO.Identity).person = dnList[index]
                                            }
                                        }
                                    }
                                    onError { e, isNetworkError ->
                                        XLog.error("$isNetworkError", e)
                                    }
                                }
//                                XLog.debug("查询personDN完成。。。。$retList")
                            }
                            retList
                        }
                    }
                    Observable.zip(unitObservable, identityObservable) { t1, t2 ->
                        val retList = ArrayList<NewContactListVO>()
                        retList.addAll(t1)
                        retList.addAll(t2)
                        retList
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Action1<ArrayList<NewContactListVO>> { list -> mView?.callbackResult(list) },
                                    ExceptionHandler(mView?.getContext()) { e -> mView?.backError(e.message ?: "") })
                }else {
                    if (TextUtils.isEmpty(orgType)) {
                        service.unitSubDirectList(parent).map { response ->
                            val retList = ArrayList<NewContactListVO>()
                            val list = response.data
                            if (list != null && list.isNotEmpty()) {
                                list.map { retList.add(it.copyToOrgVO()) }
                            }
                            retList
                        }.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Action1<ArrayList<NewContactListVO>> { list -> mView?.callbackResult(list) },
                                        ExceptionHandler(mView?.getContext()) { e -> mView?.backError(e.message ?: "") })
                    } else {
                        searchUnitWithType(service, orgType, arrayListOf())
                    }
                }
            }
        }else {
            mView?.backError(mView?.getContext()?.getString(R.string.contact_message_org_server_not_exist) ?: "组织服务异常！")
        }
    }

    /**
     * 2021-10-19 原来第三个参数 parentList无效 改成topList
     * unitListByType查询的是有包含orgType unitList:搜索组织范围 不是上级
     */
    private fun searchUnitWithType(service: OrganizationAssembleControlAlphaService, orgType: String, topList: List<String>) {
        val form = UnitListForm()
        form.type = orgType
        //form.unitList = parentList
        // 搜索出来的是全量数据 要自行剔除 作为一层数据 没有下级
        service.unitListByType(form).flatMap { response ->
            val retList = ArrayList<NewContactListVO>()
            val list = response.data
            if (list != null && list.isNotEmpty()) {
//                list.map { retList.add(NewContactListVO.Department(it.id, it.name, it.unique, it.distinguishedName,
//                        it.typeList, it.shortName, it.level, it.levelName, it.woSubDirectUnitList.size, 0)) }
                var newList = list
                if (topList.isNotEmpty()) {
                    newList = list.filter { topList.contains(it.distinguishedName) || topList.contains(it.id) }.toList()
                }
                subDirectFilterType(orgType, newList, retList)
            }
            Observable.just(retList)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action1<ArrayList<NewContactListVO>> { list -> mView?.callbackResult(list) },
                        ExceptionHandler(mView?.getContext()) { e -> mView?.backError(e.message ?: "") })
    }


    /**
     * 递归查询包含orgType的组织
     */
    private fun subDirectFilterType(orgType: String, list: List<UnitJson>, retList: ArrayList<NewContactListVO>) {
        if (list.isNotEmpty()) {
            list.map {
                if (it.typeList.contains(orgType)) {
                    retList.add(NewContactListVO.Department(it.id, it.name, it.unique, it.distinguishedName,
                        it.typeList, it.shortName, it.level, it.levelName, 0, 0))
                }
                if (it.woSubDirectUnitList.isNotEmpty()) {
                    subDirectFilterType(orgType, it.woSubDirectUnitList, retList)
                }
            }
        }
    }

}