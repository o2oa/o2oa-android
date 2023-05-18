package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.EmpowerData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.WoIdentityListItem

/**
 * Created by fancyLou on 2023-05-18.
 * Copyright © 2023 o2android. All rights reserved.
 */
object EmpowerCreateContract {

    interface View: BaseView {
        fun loadMyIdentity(identityList:List<WoIdentityListItem>)
        fun createBack(result: Boolean, msg: String?)
    }
    interface Presenter: BasePresenter<View> {
        fun loadMyIdentity()
        fun createEmpower(data: EmpowerData)
    }
}