package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.JPushDeviceForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.PushTypeData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ValueData
import retrofit2.http.*
import rx.Observable


interface JPushControlService {
    /**
     * 绑定设备到个人属性中
     * @param body
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @POST("jaxrs/device/bind")
    fun deviceBind(@Body body: JPushDeviceForm) : Observable<ApiResponse<ValueData>>


    /**
     * 删除 绑定的设备号
     * @param deviceName
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @DELETE("jaxrs/device/unbind/{deviceName}/android")
    fun deviceUnBind(@Path("deviceName") deviceName: String): Observable<ApiResponse<ValueData>>


    /**
     * 删除 绑定的设备号 新版
     * @param deviceName
     * @param pushType 推送通道类型：jpush|huawei
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @GET("jaxrs/device/unbind/new/{deviceName}/android/{pushType}")
    fun deviceUnBindNew(@Path("deviceName") deviceName: String, @Path("pushType") pushType: String): Observable<ApiResponse<ValueData>>


    /**
     * 消息推送的发送通道 推送通道类型：jpush|huawei
     */
    @GET("jaxrs/device/config/push/type")
    fun deviceConfigPushType(): Observable<ApiResponse<PushTypeData>>



}

