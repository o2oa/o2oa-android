package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan


/**
 * Created by fancyLou on 2022-05-20.
 * Copyright © 2022 o2android. All rights reserved.
 */

// 企业网盘 分组列表
sealed class  CloudFileZoneData {

    class GroupHeader(var name: String = "",
                      var resId: Int = -1) : CloudFileZoneData()

    // 我的收藏
    class MyFavorite(var id: String = "",
                     var name: String = "",
                     var person: String = "",
                     var folder: String = "",
                     var zoneId: String = "",
                     var orderNumber: Int = 0,
                      var createTime: Boolean? = false,
                      var updateTime: Boolean? = false,
                      var isAdmin: Boolean? = false,
                      var isEditor: Boolean? = false
    ) : CloudFileZoneData()

    // 共享工作区
    class MyZone(var id: String = "",
                     var name: String = "",
                     var person: String = "",
                     var superior: String = "",
                     var folder: String = "",
                     var status: String = "",
                     var zoneId: String = "",
                     var lastUpdatePerson: String = "",
                     var lastUpdateTime: String = "",
                     var description: String = "",
                     var orderNumber: Int = 0,
                     var createTime: Boolean? = false,
                     var updateTime: Boolean? = false,
                     var isAdmin: Boolean? = false,
                     var isZone: Boolean? = false,
                     var isEditor: Boolean? = false
    ) : CloudFileZoneData()
}