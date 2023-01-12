package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.*
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import rx.Observable

/**
 * Created by fancyLou on 17/05/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


interface O2OAWWWService{


    // 官网服务获取演示环境的账号列表
    @Headers("Content-Type:application/json;charset=UTF-8")
    @POST("x_program_center/jaxrs/invoke/demo_app_get_login_accounts/execute")
    fun executeSampleAccountsShell(@Body body: WwwGetSampleAccountPost):Observable<ApiResponse<ExecuteShellResponse<WwwGetSampleAccounts>>>

}