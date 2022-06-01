package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im

/**
 * Created by fancyLou on 2022-01-26.
 * Copyright © 2022 o2android. All rights reserved.
 */
data class  IMConfig(
    var enableClearMsg: Boolean = false, // 是否开启清除聊天记录功能
    var enableRevokeMsg: Boolean = false // 是否开启撤回消息功能
)