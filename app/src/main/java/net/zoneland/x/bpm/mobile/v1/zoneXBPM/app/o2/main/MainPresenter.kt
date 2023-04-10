package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.OrganizationPermissionManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.RetrofitClient
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.PersonListData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancy on 2017/6/8.
 */

class MainPresenter : BasePresenterImpl<MainContract.View>(), MainContract.Presenter {


    /**
     * 检查服务器是否有V3版本的网盘应用
     */
    override fun checkCloudFileV3() {
        val service = getCloudFileV3ControlService(null)
        if (service != null) {
            service.echo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it?.data != null) {
                            O2SDKManager.instance().prefs().edit {
                                putString(O2.PRE_CLOUD_FILE_VERSION_KEY, "1");
                            }
                        } else {
                            O2SDKManager.instance().prefs().edit {
                                putString(O2.PRE_CLOUD_FILE_VERSION_KEY, "0");
                            }
                        }
                    }
                    onError { e, _ ->
                        O2SDKManager.instance().prefs().edit {
                            putString(O2.PRE_CLOUD_FILE_VERSION_KEY, "0");
                        }
                        XLog.error("V3网盘应用不存在", e)
                    }
                }
        } else {
            O2SDKManager.instance().prefs().edit {
                putString(O2.PRE_CLOUD_FILE_VERSION_KEY, "0");
            }
            XLog.error("V3网盘应用不存在")
        }
    }

    /**
     * 检查考勤接口
     */
    override fun checkAttendanceFeature() {
        getAttendanceAssembleControlService(mView?.getContext())?.let {
            service ->
            service.attendanceV2Check().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext {
                            val data = it.data
                            if (it.data?.version != null) {
                                O2SDKManager.instance().prefs().edit {
                                    putString(O2.PRE_ATTENDANCE_VERSION_KEY, "2");
                                }
                            } else {
                                O2SDKManager.instance().prefs().edit {
                                    putString(O2.PRE_ATTENDANCE_VERSION_KEY, "1");
                                }
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            O2SDKManager.instance().prefs().edit {
                                putString(O2.PRE_ATTENDANCE_VERSION_KEY, "1");
                            }
                        }
                    }
        }
    }

    override fun loadOrganizationPermission() {
        val service = try {
            RetrofitClient.instance().organizationPermissionApi()
        } catch (e: Exception) {
//            XLog.error("", e)
            null
        }
        if (service != null) {
            val view = O2SDKManager.instance().prefs().getString(O2CustomStyle.CUSTOM_STYLE_CONTACT_PERMISSION_PREF_KEY, O2CustomStyle.CUSTOM_STYLE_CONTACT_PERMISSION_DEFAULT) ?: O2CustomStyle.CUSTOM_STYLE_CONTACT_PERMISSION_DEFAULT
            service.getPermissionViewInfo(view)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        val data = it.data
                        if (data != null) {
                            OrganizationPermissionManager.instance().initData(data)
                            //
                            personTransfer2Identity(OrganizationPermissionManager.instance().excludePersons) { identityList ->
                                if (identityList.isNotEmpty()) {
                                    OrganizationPermissionManager.instance().excludePersons.addAll(identityList)
                                }
                            }
                            personTransfer2Identity(OrganizationPermissionManager.instance().hideMobilePersons) { identityList ->
                                if (identityList.isNotEmpty()) {
                                    OrganizationPermissionManager.instance().hideMobilePersons.addAll(
                                        identityList
                                    )
                                }
                            }
                            personTransfer2Identity(OrganizationPermissionManager.instance().limitAll) { identityList ->
                                if (identityList.isNotEmpty()) {
                                    OrganizationPermissionManager.instance().limitAll.addAll(
                                        identityList
                                    )
                                }
                            }
                            personTransfer2Identity(OrganizationPermissionManager.instance().limitOuter) { identityList ->
                                if (identityList.isNotEmpty()) {
                                    OrganizationPermissionManager.instance().limitOuter.addAll(
                                        identityList
                                    )
                                }
                            }
                        }
                    }
                    onError { e, isNetworkError ->
                        XLog.error("network: $isNetworkError", e)
                    }
                }
        }
    }

    /**
     * 人员DN 批量换取身份DN
     */
    private fun personTransfer2Identity(personList: List<String>, callback: (identityList: List<String>) -> Unit) {
        val service = getAssembleExpressApi(mView?.getContext())
        if (service != null && personList.isNotEmpty()) {
            val body = PersonListData()
            body.personList = personList
            service.personIdentityByPersonList(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        val data = it.data
                        if (data!=null) {
                            callback(data.identityList)
                        }
                    }
                    onError { e, isNetworkError ->
                        XLog.error("network $isNetworkError", e)
                    }
                }
        }
    }


}