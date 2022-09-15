package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMMessage
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ReadData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkInfoRes
import java.io.File


object TaskWebViewContract {
    interface View : BaseView {
        fun workOrWorkCompletedInfo(info: WorkInfoRes?)
        fun finishLoading()
        fun submitSuccess()
        fun saveSuccess()
        fun setReadCompletedSuccess()
        fun uploadAttachmentSuccess(attachmentId:String, site:String, datagridParam:String )
        fun replaceAttachmentSuccess(attachmentId:String, site:String, datagridParam:String )
        fun downloadAttachmentSuccess(file:File)
        fun invalidateArgs()
        fun uploadMaxFiles()
        fun downloadFail(message:String)
        fun retractSuccess()
        fun retractFail()
        fun deleteSuccess()
        fun deleteFail()
        fun upload2FileStorageFail(message: String)
        fun upload2FileStorageSuccess(id: String)
        fun sendImMessageSuccess(convId: String)
        fun sendImMessageFail(err: String)
    }

    interface Presenter : BasePresenter<View> {
        fun getWorkInfoByWorkOrWorkCompletedId(workOrWorkCompletedId: String)
        fun uploadAttachment(attachmentFilePath: String, site: String, workId: String, datagridParam:String )
        fun uploadAttachmentList(attachmentFilePaths: List<String>, site: String, workId: String, datagridParam:String) //多附件上传
        fun replaceAttachment(attachmentFilePath: String, site: String, attachmentId: String, workId: String, datagridParam:String )
        fun downloadAttachment(attachmentId: String, workId: String)
        fun downloadWorkCompletedAttachment(attachmentId: String, workCompleted: String)
        fun save(workId: String, formData: String)
        fun submit(data: TaskData?, workId: String, formData: String?)
        fun delete(workId: String)
        fun setReadComplete(read: ReadData?)
        fun retractWork(workId: String)
        fun upload2FileStorage(filePath: String, referenceType: String , reference: String , scale: Int = 2000)
        fun sendImMessage(message: IMMessage)
    }
}