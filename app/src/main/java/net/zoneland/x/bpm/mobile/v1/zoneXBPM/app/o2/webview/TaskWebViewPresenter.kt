package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview

import android.graphics.BitmapFactory
import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ExceptionHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.ResponseHandler
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.APIDistributeTypeEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.IdData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMMessage
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.AttachmentInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ReadData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkPostResult
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.O2FileDownloadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.File

class TaskWebViewPresenter : BasePresenterImpl<TaskWebViewContract.View>(), TaskWebViewContract.Presenter {


    override fun getWorkInfoByWorkOrWorkCompletedId(workOrWorkCompletedId: String) {
        if (TextUtils.isEmpty(workOrWorkCompletedId)) {
            XLog.error("没有传入workOrWorkCompletedId ！")
            mView?.workOrWorkCompletedInfo(null)
            return;
        }
        val service = getProcessAssembleSurfaceServiceAPI(mView?.getContext())
        if (service != null) {
            service.getWorkInfo(workOrWorkCompletedId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { res->
                       if (res != null && res.data != null && res.data.work!=null) {
                           mView?.workOrWorkCompletedInfo(res.data.work)
                       } else {
                           XLog.error("getWorkInfo 返回结果为空。。。 ")
                           mView?.workOrWorkCompletedInfo(null)
                       }
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.workOrWorkCompletedInfo(null)
                    }
                }
        } else {
            XLog.error("服务为空。。。 ")
            mView?.workOrWorkCompletedInfo(null)
        }
    }

    override fun save(workId: String, formData: String) {
        XLog.debug("save ....... workid:$workId formData:$formData")
        if (TextUtils.isEmpty(workId) || TextUtils.isEmpty(formData)) {
            mView?.invalidateArgs()
            XLog.error("arguments is null  workid:$workId， formData:$formData")
            mView?.finishLoading()
            return
        }
        val body = RequestBody.create(MediaType.parse("text/json"), formData)
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.saveTaskForm(body, workId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<IdData> { _ -> mView?.saveSuccess() },
                            ExceptionHandler(mView?.getContext()) { e ->
                                XLog.error("", e)
                                mView?.finishLoading() })
        }
    }

    override fun delete(workId: String) {
        if (TextUtils.isEmpty(workId)) {
            mView?.invalidateArgs()
            return
        }
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.deleteWorkForm(workId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext { response->
                            XLog.info("删除工作，${response.data?.id}")
                            mView?.deleteSuccess()
                        }
                        onError { e, isNetworkError ->
                            XLog.error("删除work error, isNet:$isNetworkError", e)
                            mView?.deleteFail()
                        }
                    }
        }

    }

    override fun submit(data: TaskData?, workId: String, formData: String?) {
        if (data == null || TextUtils.isEmpty(workId) || TextUtils.isEmpty(formData)) {
            mView?.invalidateArgs()
            XLog.error("arguments is null  workid:$workId， formData:$formData")
            mView?.finishLoading()
            return
        }
        val json = O2SDKManager.instance().gson.toJson(data)
        XLog.debug("task:$json")
        val body = RequestBody.create(MediaType.parse("text/json"), formData)
        XLog.debug("formData:$formData")
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.saveTaskForm(body, workId)
                    .subscribeOn(Schedulers.io())
                    .flatMap { _ ->
                        val taskBody = RequestBody.create(MediaType.parse("text/json"), json)
                        service.postTask(taskBody, data.id)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<WorkPostResult> { _ -> mView?.submitSuccess() },
                            ExceptionHandler(mView?.getContext()) { e ->
                                XLog.error("", e)
                                mView?.finishLoading() })
        }
    }

    override fun setReadComplete(read: ReadData?) {
        if (read==null) {
            mView?.invalidateArgs()
            return
        }
        val body = RequestBody.create(MediaType.parse("text/json"), O2SDKManager.instance().gson.toJson(read))
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.setReadComplete(read.id, body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<IdData>{id->mView?.setReadCompletedSuccess()},
                            ExceptionHandler(mView?.getContext()) { e ->
                                XLog.error("", e)
                                mView?.finishLoading() })
        }
    }

    override fun retractWork(workId: String) {
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service ->
            service.retractWork(workId).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<IdData> { id -> mView?.retractSuccess() },
                            ExceptionHandler(mView?.getContext()) { e ->
                                XLog.error("", e)
                                mView?.retractFail() })
        }
    }

    override fun uploadAttachment(attachmentFilePath: String, site: String, workId: String, datagridParam:String) {
        if (TextUtils.isEmpty(attachmentFilePath) || TextUtils.isEmpty(site) || TextUtils.isEmpty(workId)) {
            mView?.invalidateArgs()
            XLog.error("arguments is null  workid:$workId， site:$site, attachmentFilePath:$attachmentFilePath")
            mView?.finishLoading()
            return
        }
        val file = File(attachmentFilePath)
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val siteBody = RequestBody.create(MediaType.parse("text/plain"), site)
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.uploadAttachment(body, siteBody, workId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<IdData> { id -> mView?.uploadAttachmentSuccess(id.id, site, datagridParam) },
                            ExceptionHandler(mView?.getContext()) { e ->
                                XLog.error("$e")
                                mView?.finishLoading() })
        }
    }



    override fun uploadAttachmentList(attachmentFilePaths: List<String>, site: String, workId: String, datagridParam:String) {
        if (attachmentFilePaths.isEmpty() || TextUtils.isEmpty(site) || TextUtils.isEmpty(workId)) {
            mView?.invalidateArgs()
            XLog.error("uploadAttachmentList arguments is null  workid:$workId， site:$site ")
            mView?.finishLoading()
            return
        } else {

            if (attachmentFilePaths.size > 9) {
                mView?.uploadMaxFiles()
                XLog.error("太多附件了，超过9个。。。。。。")
                return
            }

            val list: List<Observable<ApiResponse<IdData>>> = attachmentFilePaths.mapNotNull { path ->
                uploadAttachmentObservable(
                    path,
                    site,
                    workId
                )
            }
            Observable.zip(list) { results ->
                val idList = ArrayList<String>()
                for (result in results) {
                    val s = result as? ApiResponse<IdData>
                    if (s != null && s.data != null ) {
                        idList.add(s.data.id)
                    }
                }
                XLog.debug("uploadAttachmentList idList: ${idList.size}")
                idList
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { d ->
                        for (s in d) {
                            mView?.uploadAttachmentSuccess(s, site, datagridParam)
                        }
                    }
                    onError { e, _ ->
                        XLog.error("$e")
                        mView?.finishLoading()
                    }
                }

        }
    }


    private fun uploadAttachmentObservable(attachmentFilePath: String, site: String, workId: String) : Observable<ApiResponse<IdData>>? {
        val file = File(attachmentFilePath)
        if (!file.exists()) {
            return null
        }
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val siteBody = RequestBody.create(MediaType.parse("text/plain"), site)
        return getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.uploadAttachment(body, siteBody, workId)
    }


    override fun replaceAttachment(attachmentFilePath: String, site: String, attachmentId: String, workId: String, datagridParam:String) {
        if (TextUtils.isEmpty(attachmentFilePath) || TextUtils.isEmpty(site) || TextUtils.isEmpty(attachmentId) || TextUtils.isEmpty(workId)) {
            mView?.invalidateArgs()
            XLog.error("arguments is null att:$attachmentId, workid:$workId， site:$site, attachmentFilePath:$attachmentFilePath")
            mView?.finishLoading()
            return
        }
        val file = File(attachmentFilePath)
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.replaceAttachment(body, attachmentId, workId)
                    .subscribeOn(Schedulers.io())
                    .flatMap { response ->

                        Observable.create(object : Observable.OnSubscribe<String> {
                            override fun call(t: Subscriber<in String>?) {
                                try {
                                    val idData: IdData? = response.data
                                    if (idData == null || TextUtils.isEmpty(idData.id)) {
                                        t?.onError(Exception("没有返回附件id"))
                                    } else {
                                        val parentFolder = FileExtensionHelper.getXBPMWORKAttachmentFolder(mView?.getContext())
                                        val folder = File(parentFolder)
                                        if (folder.exists()) {
                                            folder.listFiles().filter { (it != null && it.exists() && it.isFile) }.map(File::delete)
                                        }
                                        t?.onNext(idData.id)
                                    }
                                } catch (e: Exception) {
                                    t?.onError(e)
                                }
                                t?.onCompleted()
                            }
                        })
                    }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Action1<String> { id -> mView?.replaceAttachmentSuccess(id, site, datagridParam) },
                            ExceptionHandler(mView?.getContext()) { e ->
                                XLog.error("", e)
                                mView?.finishLoading() })
        }
    }

    override fun downloadAttachment(attachmentId: String, workId: String) {
        if (TextUtils.isEmpty(attachmentId) || TextUtils.isEmpty(workId)) {
            mView?.invalidateArgs()
            XLog.error("arguments is null att:$attachmentId, workid:$workId")
            mView?.finishLoading()
            return
        }
        XLog.info("下载附件， attId ：$attachmentId , workId: $workId")
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.getWorkAttachmentInfo(attachmentId, workId)
                    .subscribeOn(Schedulers.io())
                    .flatMap { response ->
                        val info: AttachmentInfo? = response.data
                        if (info != null) {
                            XLog.info("获取到附件对象，${info.name}")
                            // 防止文件名相同 附件id作为文件夹名称
                            val path = FileExtensionHelper.getXBPMWORKAttachmentFileByName(info.id + File.separator + info.name, mView?.getContext())
                            if (O2FileDownloadHelper.fileNeedDownload(info.updateTime, path)) {
                                val downloadUrl = APIAddressHelper.instance()
                                        .getCommonDownloadUrl(APIDistributeTypeEnum.x_processplatform_assemble_surface, "jaxrs/attachment/download/$attachmentId/work/$workId/stream")
                                O2FileDownloadHelper.download(downloadUrl, path)
                                        .flatMap {
                                            Observable.just(File(path))
                                        }
                            } else {
                                XLog.info("文件存在 无需下载，path $path")
                                Observable.just(File(path))
                            }

                        } else {
                            Observable.create(object : Observable.OnSubscribe<File> {
                                override fun call(t: Subscriber<in File>?) {
                                    t?.onError(Exception("没有获取到附件信息，无法下载附件！"))
                                    t?.onCompleted()
                                }
                            })
                        }

                    }
                    .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {  file ->
                        mView?.downloadAttachmentSuccess(file)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.downloadFail(mView?.getContext()?.getString(R.string.message_download_fail) ?:  "下载附件失败，${e?.message}")
                    }
                }
//                    .subscribe({ file -> mView?.downloadAttachmentSuccess(file) }, { e ->
//                        mView?.downloadFail(mView?.getContext()?.getString(R.string.message_download_fail) ?:  "下载附件失败，${e.message}")
//                    })
        }
    }


    override fun downloadWorkCompletedAttachment(attachmentId: String, workCompleted: String) {
        if (TextUtils.isEmpty(attachmentId) || TextUtils.isEmpty(workCompleted)) {
            mView?.invalidateArgs()
            XLog.error("arguments is null att:$attachmentId, workCompleted:$workCompleted")
            mView?.finishLoading()
            return
        }
        XLog.info("下载附件， attId ：$attachmentId , workCompleted: $workCompleted")
        getProcessAssembleSurfaceServiceAPI(mView?.getContext())?.let { service->
            service.getWorkCompletedAttachmentInfo(attachmentId, workCompleted)
                    .subscribeOn(Schedulers.io())
                    .flatMap { response ->
                        val info: AttachmentInfo? = response.data
                        if (info != null) {
                            XLog.info("获取到附件对象，${info.name}")
                            // 防止文件名相同 附件id作为文件夹名称
                            val path = FileExtensionHelper.getXBPMWORKAttachmentFileByName( info.id + File.separator + info.name, mView?.getContext())
                            if (O2FileDownloadHelper.fileNeedDownload(info.updateTime, path)) {
                                val downloadUrl = APIAddressHelper.instance()
                                        .getCommonDownloadUrl(APIDistributeTypeEnum.x_processplatform_assemble_surface, "jaxrs/attachment/download/$attachmentId/workcompleted/$workCompleted/stream")
                                O2FileDownloadHelper.download(downloadUrl, path)
                                        .flatMap {
                                            Observable.just(File(path))
                                        }
                            }else {
                                XLog.info("文件存在 无需下载，path $path")
                                Observable.just(File(path))
                            }



//                            val file = File(path)
//                            if (!file.exists()) { //下载
//                                try {
//                                    SDCardHelper.generateNewFile(path)
//                                    val call = service.downloadWorkCompletedAttachment(attachmentId, workCompleted)
//                                    val downloadRes = call.execute()
//                                    val headerDisposition = downloadRes.headers().get("Content-Disposition")
//                                    XLog.debug("header disposition: $headerDisposition")
//                                    val dataInput = DataInputStream(downloadRes.body()?.byteStream())
//                                    val fileOut = DataOutputStream(FileOutputStream(file))
//                                    val buffer = ByteArray(4096)
//                                    var count = 0
//                                    do {
//                                        count = dataInput.read(buffer)
//                                        if (count > 0) {
//                                            fileOut.write(buffer, 0, count)
//                                        }
//                                    } while (count > 0)
//                                    fileOut.close()
//                                    dataInput.close()
//                                } catch (e: Exception) {
//                                    XLog.error("下载附件失败！", e)
//                                    if (file.exists()) {
//                                        file.delete()
//                                    }
//                                }
//                            }
//                            Observable.create { t ->
//                                val thisfile = File(path)
//                                if (file.exists()) {
//                                    t?.onNext(thisfile)
//                                } else {
//                                    t?.onError(Exception("附件下载异常，找不到文件！"))
//                                }
//                                t?.onCompleted()
//                            }


                        } else {
                            Observable.create { t ->
                                t?.onError(Exception("没有获取到附件信息，无法下载附件！"))
                                t?.onCompleted()
                            }
                        }

                    }
                    .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { file ->
                        mView?.downloadAttachmentSuccess(file)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.downloadFail( mView?.getContext()?.getString(R.string.message_download_fail) ?: "下载附件失败，${e?.message}")
                    }
                }
//                    .subscribe({ file -> mView?.downloadAttachmentSuccess(file) }, { e ->
//                        mView?.downloadFail( mView?.getContext()?.getString(R.string.message_download_fail) ?: "下载附件失败，${e.message}")
//                    })
        }
    }


    override fun upload2FileStorage(filePath: String, referenceType: String, reference: String, scale: Int) {
        XLog.debug("上传图片，filePath:$filePath, referenceType:$referenceType, reference:$reference, scale:$scale")
        if (filePath.isEmpty() || reference.isEmpty() || referenceType.isEmpty()) {
            mView?.upload2FileStorageFail(mView?.getContext()?.getString(R.string.message_arg_error) ?: "传入参数不正确！")
            return
        }
        val file = File(filePath)
        if (!file.exists()) {
            mView?.upload2FileStorageFail(mView?.getContext()?.getString(R.string.message_file_not_exist) ?: "文件不存在！！！")
            return
        }
        val fileService = getFileAssembleControlService(mView?.getContext())
        if (fileService!=null) {
            val mediaType = FileUtil.getMIMEType(file)
            val requestBody = RequestBody.create(MediaType.parse(mediaType), file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
            Observable.just(scale).subscribeOn(Schedulers.io())
                .flatMap { s ->
                    var newScale = s
                    val bit = BitmapFactory.decodeFile(filePath)
                    val width = bit.width
                    // 比较图片宽度
                    newScale = when {
                        width <= 0 -> {
                            s
                        }
                        width > s -> {
                            s
                        }
                        else -> {
                            width
                        }
                    }
                    XLog.debug("图片的width: $newScale")
                    fileService.uploadFile2ReferenceZone(body, referenceType, reference, newScale)
                }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ResponseHandler<IdData> {
                        id -> mView?.upload2FileStorageSuccess(id.id)
                    },
                            ExceptionHandler(mView?.getContext()) { e ->
                                XLog.error("$e")
                                mView?.upload2FileStorageFail(mView?.getContext()?.getString(R.string.message_upload_fail) ?: "文件上传异常") })
        }else {
            mView?.upload2FileStorageFail(mView?.getContext()?.getString(R.string.message_file_server_error) ?: "文件模块接入异常！")
        }

    }


    override fun sendImMessage(message: IMMessage) {
        val service = getMessageCommunicateService(mView?.getContext())
        service?.sendMessage(message)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
            ?.o2Subscribe {
                onNext {
                    mView?.sendImMessageSuccess(message.conversationId)
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.sendImMessageFail(e?.localizedMessage ?: "发送消息异常！")
                }
            }
    }
}
