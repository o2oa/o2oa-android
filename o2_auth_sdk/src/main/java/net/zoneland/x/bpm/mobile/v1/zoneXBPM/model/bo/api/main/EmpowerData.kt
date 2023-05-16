package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main

/**
 * 外出授权对象
 * Created by fancyLou on 2023-05-16.
 * Copyright © 2023 o2android. All rights reserved.
 */
data class EmpowerData(
       var id: String = "",
       var fromPerson: String = "",
       var fromIdentity: String = "",
       var toPerson: String = "",
       var toIdentity: String = "",
       var type: String = "", // all application process
       var startTime: String = "",
       var completedTime: String = "",
       var enable: Boolean = false,

              // 流程
       var edition: String = "", // 流程版本号
       var process: String = "",
       var processName: String = "",
       var processAlias: String = "",

       // 应用
       var application: String = "",
       var applicationName: String = "",
       var applicationAlias: String = "",
)
 
 