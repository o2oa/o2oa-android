package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2

/**
 * Created by fancyLou on 2022-05-26.
 * Copyright © 2022 o2android. All rights reserved.
 */
data class SearchWorkData(
    var createTime: String? = null,
    var updateTime: String? = null,
    var id: String? = null,
    var job: String? = null,
    var title: String? = null,
    var startTime: String? = null,
    var startTimeMonth: String? = null,
    var work: String? = null,
    var workCompleted: String? = null,
    var completed: Boolean? = null,
    var application: String? = null,
    var applicationName: String? = null,
    var process: String? = null,
    var processName: String? = null,
    var person: String? = null,
    var identity: String? = null,
    var department: String? = null,
    var company: String? = null,
    var activity: String? = null,
    var activityName: String? = null,
    var activityType: String? = null,
    var activityToken: String? = null,
    var creatorPerson: String? = null,
    var creatorIdentity: String? = null,
    var creatorDepartment: String? = null,
    var creatorCompany: String? = null
)