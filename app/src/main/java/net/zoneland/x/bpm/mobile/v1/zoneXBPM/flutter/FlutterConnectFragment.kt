package net.zoneland.x.bpm.mobile.v1.zoneXBPM.flutter

import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIAssemblesData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIDistributeData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIWebServerData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.AuthenticationInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.CollectUnitData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog

/**
 * flutter 程序的容器
 * Created by fancyLou on 2022-06-16.
 * Copyright © 2022 o2android. All rights reserved.
 */
class FlutterConnectFragment: FlutterFragment(), MethodChannel.MethodCallHandler  {


    private var channel: MethodChannel? = null
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        initChannel(flutterEngine.dartExecutor.binaryMessenger)
    }

    // 初始化 消息通道
    private fun initChannel(messenger: BinaryMessenger) {
        channel = MethodChannel(messenger, FlutterO2Utils.nativeChannelName)
        channel?.setMethodCallHandler(this)
    }

    // 和 flutter 通信
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        XLog.debug("执行flutter通信")
        if (call.method == FlutterO2Utils.MethodNameO2Config) {
            val themeSuffix = FancySkinManager.instance().currentSkinSuffix()
            XLog.debug("theme:$themeSuffix")
            val map = HashMap<String, String>()
            if (themeSuffix != "blue") {
                map[FlutterO2Utils.parameterNameTheme] = "red"
            } else {
                map[FlutterO2Utils.parameterNameTheme] = "blue"
            }
            //user
            try {
                val user = AuthenticationInfoJson()
                user.id = O2SDKManager.instance().cId
                user.distinguishedName = O2SDKManager.instance().distinguishedName
                user.token = O2SDKManager.instance().zToken
                user.name = O2SDKManager.instance().cName
                val jsonUser = O2SDKManager.instance().gson.toJson(user)
                map[FlutterO2Utils.parameterNameUser] = jsonUser
            } catch (e: Exception) {
                XLog.error("$e")
            }
            //unit
            try {
                val unit = CollectUnitData()
                unit.name = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_UNIT_KEY, "")
                unit.centerContext =
                    O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_CONTEXT_KEY, "")
                unit.centerHost =
                    O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HOST_KEY, "")
                unit.centerPort = O2SDKManager.instance().prefs().getInt(O2.PRE_CENTER_PORT_KEY, 80)
                unit.httpProtocol = O2SDKManager.instance().prefs()
                    .getString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, "http")
                map[FlutterO2Utils.parameterNameUnit] = O2SDKManager.instance().gson.toJson(unit)
            } catch (e: Exception) {
                XLog.error("$e")
            }
            //centerServer
            try {
                val oldDataJson =
                    O2SDKManager.instance().prefs().getString(O2.PRE_ASSEMBLESJSON_KEY, "")
                val oldWebDataJson =
                    O2SDKManager.instance().prefs().getString(O2.PRE_WEBSERVERJSON_KEY, "")
                val data = O2SDKManager.instance().gson.fromJson<APIAssemblesData>(
                    oldDataJson,
                    APIAssemblesData::class.java
                )
                val webData = O2SDKManager.instance().gson.fromJson<APIWebServerData>(
                    oldWebDataJson,
                    APIWebServerData::class.java
                )
                val dis = APIDistributeData()
                dis.webServer = webData
                dis.assembles = data
                dis.tokenName = O2SDKManager.instance().tokenName() // 添加tokenName支持
                map[FlutterO2Utils.parameterNameCenterServer] =
                    O2SDKManager.instance().gson.toJson(dis)
            } catch (e: Exception) {
                XLog.error("$e")
            }
            result.success(map)
        } else {
            XLog.error("没有实现当前方法, method:${call.method}")
            result.error("没有实现当前方法", "", "")
        }
    }

}