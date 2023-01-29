package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2

/**
 * Created by fancyLou on 2021-05-28.
 * Copyright © 2021 O2. All rights reserved.
 */


/***********v2版本***********/
class O2SearchV2Form(
    var page: Int = 1,
    var query: String = "",
    var size: Int = O2.DEFAULT_PAGE_NUMBER
)

class O2SearchV2PageModel(
    var documentList:List<O2SearchV2Entry> = arrayListOf(),
    var count: Int = 0,
)
class O2SearchV2Entry(
    var id: String = "", // 业务id
    var category: String = "", // cms processPlatform
    var title: String = "",
    var highlighting: String = "", // html
    var summary: String = "", // 文字
    var creatorPerson: String = "",
    var creatorUnit: String = "",
    var indexTime: String = "",
    var createTime: String = "",
    var updateTime: String = "",
)





class O2SearchIdsEntry (
    var count: Int = 0,
    var valueList: List<String> = arrayListOf()
)

class O2SearchEntryForm(
    var entryList: List<String> = arrayListOf()
)

class O2SearchEntry(
    var id: String = "",
    var type: String = "",
    var title: String = "",
    var summary: String = "",
    var creatorPerson: String = "",
    var creatorUnit: String = "",
    var reference: String = "",
    var createTime: String = "",
    var updateTime: String = "",

    var appId: String = "", // cms栏目id
    var appName: String = "", // cms栏目名称
    var categoryId: String = "",
    var categoryName: String = "", // cms分类名称
    var application: String = "", // 流程应用id
    var applicationName: String = "", // 流程应用名称
    var process: String = "",
    var processName: String = "" // 流程名称
)

class O2SearchPageModel(
    var list:List<O2SearchEntry> = arrayListOf(),
    var page: Int = 1,
    var totalPage: Int = 1
)


