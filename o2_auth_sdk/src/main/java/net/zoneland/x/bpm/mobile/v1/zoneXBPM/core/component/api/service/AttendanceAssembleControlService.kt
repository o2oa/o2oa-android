package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.IdData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ValueData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.*
import okhttp3.RequestBody
import retrofit2.http.*
import rx.Observable


/**
 * Created by fancy on 2017/6/6.
 */
interface AttendanceAssembleControlService {



    ////////////////v2/////////////////

    /**
     * v2版本检查
     */
    @GET("jaxrs/v2/my/version")
    fun attendanceV2Check():Observable<ApiResponse<AttendanceV2CheckData>>

    /**
     * 预打卡接口
     * 打卡之前调用
     */
    @GET("jaxrs/v2/mobile/check/pre")
    fun attendanceV2PreCheck():Observable<ApiResponse<AttendanceV2PreCheckData>>

    /**
     * 打卡
     */
    @POST("jaxrs/v2/mobile/check")
    fun attendanceV2CheckIn(@Body body: AttendanceV2CheckInBody):Observable<ApiResponse<AttendanceV2CheckResponse>>

    /**
     * 我的统计
     */
    @POST("jaxrs/v2/my/statistic")
    fun attendanceV2MyStatistic(@Body body: AttendanceV2StatisticBody):Observable<ApiResponse<AttendanceV2StatisticResponse>>

    /**
     * 分页查询我的异常打卡数据
     */
    @POST("jaxrs/v2/appeal/list/{page}/size/{size}")
    fun attendanceV2MyAppealListByPage(@Path("page") page: Int, @Path("size") size: Int, @Body body: AttendanceV2AppealPageListFilter):Observable<ApiResponse<List<AttendanceV2AppealInfo>>>

    /**
     * 配置文件
     */
    @GET("jaxrs/v2/config")
    fun attendanceV2Config():Observable<ApiResponse<AttendanceV2Config>>

    /**
     * 当前是否能够申诉
     * @param id 异常数据id
     */
    @GET("jaxrs/v2/appeal/{id}/start/check")
    fun attendanceV2CheckCanAppeal(@Path("id") id: String):Observable<ApiResponse<ValueData>>

    /**
     * 启动流程后 更新申诉数据
     */
    @GET("jaxrs/v2/appeal/{id}/start/process")
    fun attendanceV2AppealStartProcess(@Path("id") id: String):Observable<ApiResponse<ValueData>>


    //////////////////////v1/////////////

    /**
     * 获取当前用户的考勤周期
     */
    @GET("jaxrs/attendancestatisticalcycle/cycleDetail/{year}/{month}")
    fun myAttendanceStatisticCycle(@Path("year") year: String, @Path("month") month: String):
            Observable<ApiResponse<AttendanceStatisticCycle>>

    /**
     * 当月考勤明细列表
     * @param filter
     * *
     * @param id
     * *
     * @param count
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/attendancedetail/filter/list/{id}/next/{count}")
    fun myAttendanceDetailListByMonth(@Body filter: AttendanceDetailQueryFilterJson,
                                      @Path("id") id: String, @Path("count") count: String): Observable<ApiResponse<List<AttendanceDetailInfoJson>>>

    /**
     * 当月考勤饼图数据
     * @param filter
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/attendancedetail/filter/list/user")
    fun myAttendanceDetailChartList(@Body filter: AttendanceDetailQueryFilterJson): Observable<ApiResponse<List<AttendanceDetailInfoJson>>>

    /**
     * 申诉审批状态启用开关.png
     * @return  configValue = true
     */
    @GET("jaxrs/attendancesetting/code/APPEALABLE")
    fun getAppealableValue(): Observable<ApiResponse<SettingInfoJson>>

    /**
     * 考勤审核人确定方式
     * @return configValue = 所属部门职务 申诉的时候需要选择身份
     */
    @GET("jaxrs/attendancesetting/code/APPEAL_AUDITOR_TYPE")
    fun getAppealAuditorType(): Observable<ApiResponse<SettingInfoJson>>

    /**
     * 申诉申请表单提交
     * @param data
     * *
     * @param id
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/attendanceappealInfo/appeal/{id}")
    fun submitAppeal(@Body data: AttendanceDetailInfoJson, @Path("id") id: String): Observable<ApiResponse<BackInfoJson>>

    /**
     * 申诉审批列表 分页查询
     * @param id    (0)
     * *
     * @param limit 每页显示数据
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/attendanceappealInfo/filter/list/{id}/next/{limit}")
    fun findAttendanceAppealInfoListByPage(@Path("id") id: String, @Path("limit") limit: Int, @Body filter: AppealApprovalQueryFilterJson): Observable<ApiResponse<List<AppealInfoJson>>>

    /**
     * 考勤申诉审批提交
     * @param id
     * *
     * @param body  同意：{"status":"1"} 不同意：{"status":"-1"}
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/attendanceappealInfo/process/{id}")
    fun approvalAppealInfo(@Path("id") id: String, @Body body: Map<String, String>): Observable<ApiResponse<BackInfoJson>>


    /**
     * 考勤申诉审批提交
     * @param body
     */
    @PUT("jaxrs/attendanceappealInfo/audit")
    fun approvalAppealInfo(@Body body: AppealApprovalFormJson): Observable<ApiResponse<BackInfoJson>>

    /**
     * 打卡
     * @param body  MobileCheckInJson
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @POST("jaxrs/attendancedetail/mobile/recive")
    fun attendanceDetailCheckIn(@Body body: RequestBody): Observable<ApiResponse<IdData>>

    /**
     * 预打卡接口
     * 判断当前应该打哪个卡，以及打卡状态
     *
     */
    @GET("jaxrs/attendancedetail/mobile/mobilepreview")
    fun mobilePreviewCheckIn(): Observable<ApiResponse<AttendancePreCheckInJson>>

    /**
     * 分页查询手机端打卡记录

     * @param body  MobileCheckInQueryFilterJson
     * *
     * @param page
     * *
     * @param count
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/attendancedetail/mobile/filter/list/page/{page}/count/{count}")
    fun findAttendanceDetailMobileByPage(@Body body: RequestBody, @Path("page") page: Int, @Path("count") count: Int): Observable<ApiResponse<List<MobileCheckInJson>>>


    /**
     * 获取所有工作场所
     * @return
     */
    @GET("jaxrs/workplace/list/all")
    fun findAllWorkplace(): Observable<ApiResponse<List<MobileCheckInWorkplaceInfoJson>>>

    /**
     * 新增工作场所
     * @param body  { "placeName":"场所名称", "longitude":"经度", "latitude":"纬度", "errorRange":100, "description":"说明备注"}
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @POST("jaxrs/workplace")
    fun attendanceWorkplace(@Body body: RequestBody): Observable<ApiResponse<IdData>>

    /**
     * 删除工作场所
     * @param id
     * *
     * @return
     */
    @DELETE("jaxrs/workplace/{id}")
    fun deleteAttendanceWorkplace(@Path("id") id: String): Observable<ApiResponse<IdData>>

    /**
     * 考勤管理员列表
     * @return
     */
    @GET("jaxrs/attendanceadmin/list/all")
    fun attendanceAdmin(): Observable<ApiResponse<List<AdministratorInfoJson>>>

    /**
     *
     * @return
     */
    @GET("jaxrs/attendancedetail/mobile/my")
    fun listMyRecords(): Observable<ApiResponse<MobileMyRecords>>
}