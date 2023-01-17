package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.O2OAInnerUpdateBean
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.EchoData
import retrofit2.http.GET
import rx.Observable


interface PackingClientAssembleSurfaceService {

    /**
     * 服务器应答
     */
    @GET("jaxrs/echo")
    fun echo(): Observable<ApiResponse<EchoData>>


    /**
     * 最新的打包信息
     */
    @GET("jaxrs/apppackanony/file/type/android/last")
    fun androidPackLastAPk(): Observable<ApiResponse<O2OAInnerUpdateBean>>
}