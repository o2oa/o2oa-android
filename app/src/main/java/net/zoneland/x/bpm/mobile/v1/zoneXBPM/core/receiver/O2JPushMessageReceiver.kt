package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.receiver

import android.content.Context
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2App
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog

/**
 * Created by fancyLou on 2022-01-11.
 * Copyright © 2022 o2android. All rights reserved.
 */
class O2JPushMessageReceiver: JPushMessageReceiver() {


    override fun onNotifyMessageArrived(p0: Context?, p1: NotificationMessage?) {
        super.onNotifyMessageArrived(p0, p1)
        XLog.info("接收到极光通知，${p1?.notificationId}")
        if (p1?.notificationId != null) {
            O2App.instance.addNotification(p1.notificationId)
        }
    }
}