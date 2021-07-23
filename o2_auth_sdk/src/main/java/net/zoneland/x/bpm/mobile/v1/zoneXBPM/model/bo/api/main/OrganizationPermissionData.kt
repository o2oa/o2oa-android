package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main

/**
 * 应用市场 通讯录应用 可以管理通讯录权限
 * 本类是获取通讯录权限返回的对象
 * 字段内容是用,隔开的
 * 例： {
    "C895A1429510000180D9126FB580C060": "2020-03-20 11:01:16",
    "excludePerson": "程剑@chengjian@P",
    "excludeUnit": "",
    "hideMobilePerson": "蔡艳红@f2f5dc2a-6587-419f-bdb8-07d0f12fd0c9@P,罗晶@luojing@P",
    "limitQueryAll": "郑萍@zhengping@P",
    "limitQueryOuter": "李义@6041295d-3799-4693-b1a9-77ead8dee073@P,李四@0d97a917-529f-4fd6-88b1-d8f8c0fa8601@P,团队领导@b7e3a8d3-21d4-4802-babf-9fc85392333d@U"
    }
 * Created by fancyLou on 2021-07-20.
 * Copyright © 2021 O2. All rights reserved.
 */

data class OrganizationPermissionData (
    var excludePerson: String = "", // 不允许被查询个人 人员数据
    var excludeUnit: String = "", // 不允许被查询单位  组织数据
    var hideMobilePerson: String = "", // 隐藏手机号码的人员 人员数据
    var limitQueryAll: String = "", // 限制查看所有人 人员数据
    var limitQueryOuter: String = "" // 限制查看外部门 有人员数据和组织数据
)