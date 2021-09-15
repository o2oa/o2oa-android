package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api

/**
 * Created by fancyLou on 2021-09-14.
 * Copyright © 2021 o2android. All rights reserved.
 */
data class PushTypeData(var pushType: String = "")



object PushType {
    const val JPUSH_TYPE = "jpush"
    const val HUAWEI_TYPE = "huawei"
}