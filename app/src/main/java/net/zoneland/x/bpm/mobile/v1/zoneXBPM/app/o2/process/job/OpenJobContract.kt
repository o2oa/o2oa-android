package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.job

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkOrWorkcompletedList

/**
 * Created by fancyLou on 2023-04-13.
 * Copyright © 2023 o2android. All rights reserved.
 */
object OpenJobContract {
    interface View : BaseView {
        fun workOrWorkCompletedResult(list: WorkOrWorkcompletedList?)
    }
    interface Presenter : BasePresenter<View> {
        fun getWorkByJobId(jobId: String)
    }
}