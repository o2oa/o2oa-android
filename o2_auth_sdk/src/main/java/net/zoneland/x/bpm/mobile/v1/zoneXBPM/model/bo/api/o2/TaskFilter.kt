package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2

/**
 * Created by fancyLou on 2020-09-07.
 * Copyright © 2020 O2. All rights reserved.
 */

data class TaskFilter(
        var applicationList: ArrayList<String> = ArrayList(),
        var processList: ArrayList<String> = ArrayList(),
)