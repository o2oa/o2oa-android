package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model

/**
 * Created by fancyLou on 2023-01-16.
 * Copyright © 2023 o2android. All rights reserved.
 */
//
data class O2OAInnerUpdateBean (
    var id: String = "",
    var name: String = "",
    var storage: String = "",
    var extension: String = "",
    var lastUpdateTime: String = "",
    var length: Long = 0L,
    var packInfoId: String = "",
    var appVersionName: String = "",
    var appVersionNo: String = "",
    var status: Int = 0, // 状态，异步下载文件所以需要这个状态，0开启，1下载完成， 2过程有异常.
    var isPackAppIdOuter: String = "",
    var createTime: String = "",
    var updateTime: String = ""
)