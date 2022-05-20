package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.zone

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileZoneData

/**
 * Created by fancyLou on 2022-05-19.
 * Copyright © 2022 o2android. All rights reserved.
 */
object ZoneContract {
    interface View: BaseView {
        fun zoneList(list: List<CloudFileZoneData>)
    }
    interface Presenter: BasePresenter<View>{
        fun loadZone()
    }
}