package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo

/**
 * Created by fancyLou on 2021-01-13.
 * Copyright © 2021 O2. All rights reserved.
 */

object O2ChatGroupMemberContract {
    interface View : BaseView {
        fun updateSuccess(info: IMConversationInfo)
        fun updateFail(msg: String)
    }
    interface Presenter : BasePresenter<View> {
        fun updateConversationPeople(id: String, users: ArrayList<String>)
    }
}