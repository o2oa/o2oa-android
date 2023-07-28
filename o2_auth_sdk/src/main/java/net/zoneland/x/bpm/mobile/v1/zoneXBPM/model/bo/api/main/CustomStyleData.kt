package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.portal.PortalData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.AppItemOnlineVo

/**
 * Created by fancyLou on 16/04/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


data class CustomStyleData(
    var needGray: Boolean = false, // 是否启用灰色 默哀
    var indexType: String = "default", // 首页是默认default 还是门户
//        var indexId: String = "",
    var indexPortal: String = "", // 首页门户ID
    var simpleMode: Boolean = false, // 简易模式
    var appIndexPages: List<String> = ArrayList(), // 首页展现的 tab 列表
    var systemMessageSwitch: Boolean = true, // 消息列表中 是否显示系统通知
    var systemMessageCanClick: Boolean = true, // 系统消息是否可点击打开
    var appExitAlert: String = "", // app退出提示，有提示信息就弹出窗
    var contactPermissionView: String = O2CustomStyle.CUSTOM_STYLE_CONTACT_PERMISSION_DEFAULT, // 通讯录权限使用的视图
    var portalList: List<PortalData> = ArrayList(), // 门户应用列表
    var nativeAppList: List<AppItemOnlineVo> = ArrayList(), // 原生应用列表
    var images: ArrayList<ImageValue> = ArrayList() // 替换的图片列表
) {

    data class ImageValue(var name: String = "",
                          var value: String = "")
}