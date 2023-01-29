package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchPageModel
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchV2Form
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchV2PageModel
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkOrWorkcompletedList

/**
 * Created by fancyLou on 2021-05-27.
 * Copyright © 2021 O2. All rights reserved.
 */
object SearchV2Contract {
    interface View: BaseView {
        fun searchResult(result: O2SearchV2PageModel)
        fun workOrWorkcompletedResult(list: WorkOrWorkcompletedList?, title: String)
    }
    interface Presenter: BasePresenter<View> {
        fun search(post: O2SearchV2Form)
        fun getWorkByJobId(jobId: String, title: String)
    }
}