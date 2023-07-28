package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main

/**
 * Created by fancyLou on 2023-07-27.
 * Copyright © 2023 o2android. All rights reserved.
 */
enum class MainPagesEnum(val key: String, val displayName:String, val order: Int) {

    home("home", "首页", 1),
    im("im", "消息", 2),
    contact("contact", "通讯录", 3),
    app("app", "应用", 4),
    settings("settings", "设置", 5)


}