package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api

/**
 * Created by fancyLou on 17/05/2018.
 * Copyright © 2018 O2. All rights reserved.
 */

data class WwwGetSampleAccountPost  (
        var serverId: String?
)
data class WwwGetSampleAccount  (
        var name: String?,
        var account: String?
)

data class WwwGetSampleAccounts  (
        var password: String?,
        var accountList: List<WwwGetSampleAccount>?
        )