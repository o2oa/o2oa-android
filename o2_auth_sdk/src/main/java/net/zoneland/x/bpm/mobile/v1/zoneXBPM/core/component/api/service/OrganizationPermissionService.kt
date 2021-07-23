package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.OrganizationPermissionData
import retrofit2.http.GET
import retrofit2.http.Path
import rx.Observable


/**
 * custom模块 通讯录 需要到应用市场下载安装
 * Created by fancy on 2017/6/6.
 */
interface OrganizationPermissionService {


    /**
     * 获取当前用户的考勤周期
     */
    @GET("jaxrs/permission/view/{view}")
    fun getPermissionViewInfo(@Path("view") view: String): Observable<ApiResponse<OrganizationPermissionData>>


}