package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2

/**
 * 根据job查询工作 返回的结果对象
 * Created by fancyLou on 2023-01-29.
 * Copyright © 2023 o2android. All rights reserved.
 */
class WorkOrWorkcompletedList (
    var workList: List<Work> = ArrayList(),
    var workCompletedList: List<Work> = ArrayList()
)