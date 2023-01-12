package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2

/**
 * 待办待阅数量
 * Created by fancyLou on 2022-12-14.
 * Copyright © 2022 o2android. All rights reserved.
 */
data class CountProcessNumberData(
    val task: Int?,
    val taskCompleted: Int?,
    val read: Int?,
    val readCompleted: Int?,
    val review: Int?,
)