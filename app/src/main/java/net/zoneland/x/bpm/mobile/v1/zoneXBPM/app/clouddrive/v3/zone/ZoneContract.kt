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
        fun backError(message: String)
        fun canCreateZone(flag: Boolean)
        fun createZoneSuccess()
        fun updateZoneSuccess()
        fun deleteZoneSuccess()
        fun addFavoriteSuccess()
        fun renameFavoriteSuccess()
        fun cancelFavoriteSuccess()
    }
    interface Presenter: BasePresenter<View>{
        fun loadZone()
        fun loadZoneCreatorPermission()
        fun createZone(name: String, desc: String)
        fun updateZone(id: String, name: String, desc: String)
        fun deleteZone(id: String)
        fun addFavorite(name: String, zoneId: String)
        fun renameFavorite(name: String, id: String)
        fun cancelFavorite(id: String)
    }
}