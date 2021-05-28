package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchPageModel

/**
 * Created by fancyLou on 2021-05-27.
 * Copyright © 2021 O2. All rights reserved.
 */
object SearchContract {
    interface View: BaseView {
        fun nextPage(model: O2SearchPageModel)
        fun searchResult(model: O2SearchPageModel)
        fun error(err: String)
    }
    interface Presenter: BasePresenter<View> {
        fun search(key: String)
        fun nextPage()

        fun getTotalPage(): Int
        fun getPage(): Int
        fun hasNexPage(): Boolean
    }
}