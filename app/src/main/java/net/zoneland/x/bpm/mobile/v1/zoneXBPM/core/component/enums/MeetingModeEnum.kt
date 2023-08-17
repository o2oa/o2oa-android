package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums

/**
 * Created by fancyLou on 2023-08-17.
 * Copyright © 2023 o2android. All rights reserved.
 */
enum class MeetingModeEnum(
    val key: String, val display: String
) {
    online("online", "线上会议"),
    offline("offline", "线下会议")
}