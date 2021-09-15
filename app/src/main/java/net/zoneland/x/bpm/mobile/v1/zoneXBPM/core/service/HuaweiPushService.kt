package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.service

import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager

/**
 * Created by fancyLou on 2021-09-13.
 * Copyright © 2021 o2android. All rights reserved.
 */
class HuaweiPushService : HmsMessageService() {

    // 透传消息 这里才会接收到内容
    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)

        Log.i("HuaweiPushService", "接收到华为推送消息。。。。。。")
        Log.i("HuaweiPushService", O2SDKManager.instance().gson.toJson(message))
    }
}