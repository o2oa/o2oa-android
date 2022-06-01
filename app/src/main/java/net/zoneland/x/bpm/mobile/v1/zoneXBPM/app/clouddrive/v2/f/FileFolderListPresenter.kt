package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.f

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.IdData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudDiskShareForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FileJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FolderJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.CloudDiskItem
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

class FileFolderListPresenter : BasePresenterImpl<FileFolderListContract.View>(), FileFolderListContract.Presenter {



    override fun move(files: List<CloudDiskItem.FileItem>, folders: List<CloudDiskItem.FolderItem>, destFolderId: String) {
        val all : ArrayList<Observable<ApiResponse<IdData>>> = ArrayList()
        if (O2SDKManager.instance().appCloudDiskIsV3()) {
            val service = getCloudFileV3ControlService(mView?.getContext())
            if (service == null) {
                mView?.error("服务为空！")
                return
            }
            if (files.isNotEmpty()) {
                all.addAll(
                    files.map {
                        val json = FileJson(
                            it.id,
                            it.createTime,
                            it.updateTime,
                            it.name,
                            it.person,
                            "",
                            it.fileName,
                            it.extension,
                            it.contentType,
                            it.storageName,
                            it.fileId,
                            it.storage,
                            it.type,
                            it.length,
                            destFolderId,
                            it.lastUpdateTime,
                            it.lastUpdatePerson
                        )
                        service.updateFile(json, it.id)
                    }
                )
            }
            if (folders.isNotEmpty()) {
                all.addAll(
                    folders.map {
                        val json = FolderJson(
                            it.id,
                            it.createTime,
                            it.updateTime,
                            it.name,
                            it.person,
                            "",
                            destFolderId,
                            it.attachmentCount,
                            it.size,
                            it.folderCount,
                            it.status,
                            it.fileId
                        )
                        service.updateFolder(it.id, json)
                    }
                )
            }
        } else {
            val service = getCloudFileControlService(mView?.getContext())
            if (service == null) {
                mView?.error("服务为空！")
                return
            }
            if (files.isNotEmpty()) {
                all.addAll(
                    files.map {
                        val json = FileJson(
                            it.id,
                            it.createTime,
                            it.updateTime,
                            it.name,
                            it.person,
                            "",
                            it.fileName,
                            it.extension,
                            it.contentType,
                            it.storageName,
                            it.fileId,
                            it.storage,
                            it.type,
                            it.length,
                            destFolderId,
                            it.lastUpdateTime,
                            it.lastUpdatePerson
                        )
                        service.updateFile(json, it.id)
                    }
                )
            }
            if (folders.isNotEmpty()) {
                all.addAll(
                    folders.map {
                        val json = FolderJson(
                            it.id,
                            it.createTime,
                            it.updateTime,
                            it.name,
                            it.person,
                            "",
                            destFolderId,
                            it.attachmentCount,
                            it.size,
                            it.folderCount,
                            it.status,
                            it.fileId
                        )
                        service.updateFolder(it.id, json)
                    }
                )
            }
        }
        Observable.merge(all).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.moveSuccess()
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }

    }


    override fun share(ids: List<String>, users: List<String>, orgs: List<String>) {
        val all : ArrayList<Observable<ApiResponse<IdData>>> = ArrayList()
        if (O2SDKManager.instance().appCloudDiskIsV3()) {
            val service = getCloudFileV3ControlService(mView?.getContext())
            if (service == null) {
                mView?.error("服务为空！")
                return
            }
            all.addAll(ids.map {
                val form = CloudDiskShareForm()
                form.fileId = it
                form.shareOrgList = orgs
                form.shareUserList = users
                service.share(form)
            })
        } else {
            val service = getCloudFileControlService(mView?.getContext())
            if (service == null) {
                mView?.error("服务为空！")
                return
            }
            all.addAll(ids.map {
                val form = CloudDiskShareForm()
                form.fileId = it
                form.shareOrgList = orgs
                form.shareUserList = users
                service.share(form)
            })
        }
        Observable.merge(all).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.shareSuccess()
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }

    }


    override fun deleteBatch(fileIds: List<String>, folderIds: List<String>) {
        val all : ArrayList<Observable<ApiResponse<IdData>>> = ArrayList()
        if (O2SDKManager.instance().appCloudDiskIsV3()) {
            val service = getCloudFileV3ControlService(mView?.getContext())
            if (service == null) {
                mView?.error("服务为空！")
                return
            }
            if (fileIds.isNotEmpty()) {
                val deletes = fileIds.map { service.deleteFile(it) }
                all.addAll(deletes)
            }
            if (folderIds.isNotEmpty()) {
                val deleteFolders = folderIds.map { service.deleteFolder(it) }
                all.addAll(deleteFolders)
            }
        } else {
            val service = getCloudFileControlService(mView?.getContext())
            if (service == null) {
                mView?.error("服务为空！")
                return
            }
            if (fileIds.isNotEmpty()) {
                val deletes = fileIds.map { service.deleteFile(it) }
                all.addAll(deletes)
            }
            if (folderIds.isNotEmpty()) {
                val deleteFolders = folderIds.map { service.deleteFolder(it) }
                all.addAll(deleteFolders)
            }
        }

        Observable.merge(all).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.deleteSuccess()
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }
    }


    override fun updateFolder(folder: FolderJson) {
        val service = if (O2SDKManager.instance().appCloudDiskIsV3()) {
            getCloudFileV3ControlService(mView?.getContext())?.updateFolder(folder.id, folder)
        } else {
            getCloudFileControlService(mView?.getContext())?.updateFolder(folder.id, folder)
        }
        if (service == null) {
            mView?.error("服务为空！")
            return
        }
        service.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.updateSuccess()
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }
    }


    override fun updateFile(file: FileJson) {
        val service = if (O2SDKManager.instance().appCloudDiskIsV3()) {
            getCloudFileV3ControlService(mView?.getContext())?.updateFile(file, file.id)
        } else {
            getCloudFileControlService(mView?.getContext())?.updateFile(file, file.id)
        }
        if (service == null) {
            mView?.error("服务为空！")
            return
        }
        service.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.updateSuccess()
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }
    }


    override fun uploadFile(parentId: String, file: File) {
        var folderId = parentId
        if (parentId.isEmpty()) {
            folderId = O2.FIRST_PAGE_TAG
        }
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val service = if (O2SDKManager.instance().appCloudDiskIsV3()) {
            getCloudFileV3ControlService(mView?.getContext())?.uploadFile2Folder(body, folderId)
        } else {
            getCloudFileControlService(mView?.getContext())?.uploadFile2Folder(body, folderId)
        }
        if (service == null) {
            mView?.error("服务为空！")
            return
        }

        service.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.uploadSuccess()
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }
    }

    override fun uploadFileList(parentId: String, files: List<String>) {
        var folderId = parentId
        if (parentId.isEmpty()) {
            folderId = O2.FIRST_PAGE_TAG
        }
        if (files.isEmpty() || files.size > 9) {
            mView?.error("错误，上传附件个数太多！")
            return
        }
        val list = files.mapNotNull { path -> uploadFileObservable(folderId, path) }
        Observable.zip(list) {iddatas ->
            val idList = ArrayList<String>()
            for (iddata in iddatas) {
                val d = iddata as? ApiResponse<IdData>
                if (d != null && d.data != null) {
                    idList.add(d.data.id ?: "")
                }
            }
            idList
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext {
                    mView?.uploadSuccess()
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.error(e?.message ?: "错误！")
                }
            }
    }

    private fun uploadFileObservable(folderId: String, path: String) :  Observable<ApiResponse<IdData>>? {
        val file = File(path)
        if (!file.exists()) {
            return null
        }
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return if (O2SDKManager.instance().appCloudDiskIsV3()) {
            getCloudFileV3ControlService(mView?.getContext())?.uploadFile2Folder(
                body,
                folderId
            )
        } else {
            getCloudFileControlService(mView?.getContext())?.uploadFile2Folder(
                body,
                folderId
            )
        }
    }

    override fun createFolder(params: HashMap<String, String>) {
        val service = if (O2SDKManager.instance().appCloudDiskIsV3()) {
            getCloudFileV3ControlService(mView?.getContext())?.createFolder(params)
        } else {
            getCloudFileControlService(mView?.getContext())?.createFolder(params)
        }
        if (service == null) {
            mView?.error("服务为空！")
            return
        }
        service.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.createFolderSuccess()
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }
    }


    override fun getItemList(parentId: String) {
        if (O2SDKManager.instance().appCloudDiskIsV3()) {
            val serviceV3 = getCloudFileV3ControlService(mView?.getContext())
            if (serviceV3 == null) {
                mView?.error("服务为空！")
                return
            }
            if (parentId.isEmpty()) {
                getItemListObservable(serviceV3.listFolderTop(), serviceV3.listFileTop())
            } else {
                getItemListObservable(serviceV3.listFolderByFolderId(parentId), serviceV3.listFileByFolderId(parentId))
            }
        } else {
            val service = getCloudFileControlService(mView?.getContext())
            if (service == null) {
                mView?.error("服务为空！")
                return
            }
            if (parentId.isEmpty()) {
                getItemListObservable(service.listFolderTop(), service.listFileTop())
            } else {
                getItemListObservable(service.listFolderByFolderId(parentId), service.listFileByFolderId(parentId))
            }
        }
    }

    private fun getItemListObservable(folderObservable: Observable<ApiResponse<List<FolderJson>>>, fileObservable: Observable<ApiResponse<List<FileJson>>>) {
        Observable
            .zip(folderObservable, fileObservable) { r1, r2 ->
                val list = ArrayList<CloudDiskItem>()
                val folderList = r1.data
                val fileList = r2.data
                if (folderList != null && folderList.isNotEmpty()) {
                    folderList.forEach {
                        list.add(it.copyToVO2())
                    }
                }
                if (fileList!=null && fileList.isNotEmpty()) {
                    fileList.forEach {
                        list.add(it.copyToVO2())
                    }
                }
                list
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext {
                    mView?.itemList(it)
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.error(e?.message ?: "错误！")
                }
            }
    }

}