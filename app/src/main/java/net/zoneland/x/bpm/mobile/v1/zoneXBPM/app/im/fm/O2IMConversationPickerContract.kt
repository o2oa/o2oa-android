package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.fm

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo

/**
 * Created by fancyLou on 2022-07-13.
 * Copyright © 2022 o2android. All rights reserved.
 */
object O2IMConversationPickerContract {
    interface View: BaseView {
        fun myConversationList(list: List<IMConversationInfo>)
    }

    interface Presenter: BasePresenter<View> {
        fun getMyConversationList()
    }
}