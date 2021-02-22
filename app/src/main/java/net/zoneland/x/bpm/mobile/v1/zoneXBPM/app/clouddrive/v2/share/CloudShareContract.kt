package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.share

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.CloudDiskItem

/**
 * Created by fancyLou on 2021-01-21.
 * Copyright © 2021 O2. All rights reserved.
 */
object CloudShareContract {
    interface View: BaseView {
        fun itemList(list: List<CloudDiskItem>)
        fun error(error: String)
        fun success()
    }

    interface Presenter: BasePresenter<View> {
        fun getMyShareItemList(parentId: String, shareId: String)
        fun getShareToMeItemList(parentId: String, shareId: String)
        //删除我的分享
        fun deleteMyShare(shareIds: List<String>)
        //屏蔽给我的分享
        fun shieldShare(shareIds: List<String>)
    }
}