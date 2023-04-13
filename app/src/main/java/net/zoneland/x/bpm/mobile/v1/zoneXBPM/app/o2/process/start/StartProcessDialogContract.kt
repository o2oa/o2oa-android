package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.start

import com.google.gson.JsonElement
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessDraftWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessInfoData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskData

/**
 * Created by fancyLou on 2023-04-12.
 * Copyright © 2023 o2android. All rights reserved.
 */
object StartProcessDialogContract {
    interface View : BaseView {
        fun getProcess(process: ProcessInfoData?)
        fun loadCurrentPersonIdentity(list:List<ProcessWOIdentityJson>)
        fun startProcessSuccess(task: TaskData)
        fun startProcessSuccessNoWork()
        fun startProcessFail(message:String)
        fun startDraftSuccess(work: ProcessDraftWorkData)
        fun startDraftFail(message:String)
    }
    interface Presenter : BasePresenter<View> {
        fun getProcess(processId: String)
        fun loadCurrentPersonIdentityWithProcess(processId: String)
        fun startProcess(identity: String, processId: String, data: JsonElement?)
        fun startDraft(identity: String, processId: String, data: JsonElement?)
    }
}