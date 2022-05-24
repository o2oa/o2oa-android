package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.folder

import android.text.TextUtils
import com.xiaomi.push.it
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.IdData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ValueData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileV3Data
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.MoveToMyPanPost
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.MoveV3Post
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.RenamePost
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.HashMap

/**
 * Created by fancyLou on 2022-05-20.
 * Copyright © 2022 o2android. All rights reserved.
 */
class FolderFileListPresenter: BasePresenterImpl<FolderFileListContract.View>(), FolderFileListContract.Presenter {

    override fun getFolderFileItemList(parentId: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            // 获取目录下的文件夹列表和文件列表
            Observable.zip(
                service.listFolderByFolderIdV3(parentId, "updateTime"),
                service.listFileByFolderIdV3(parentId, "updateTime")) { r1, r2 ->
                val list = ArrayList<CloudFileV3Data>()
                if (r1 != null && r1.data != null && r1.data.isNotEmpty()) {
                    list.addAll(r1.data.map { it.copyToVO3() })
                }
                if (r2 != null && r2.data != null && r2.data.isNotEmpty()) {
                    list.addAll(r2.data.map { it.copyToVO3() })
                }
                list
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { result ->
                        mView?.folderFileItemList(result)
                    }
                    onError { e, _ ->
                        mView?.folderFileItemList(ArrayList())
                        XLog.error("查询${parentId}目录下文件失败", e)
                    }
                }
        } else {
            mView?.folderFileItemList(ArrayList())
        }
    }

    override fun updateFolderName(folderId: String, newName: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val body = RenamePost(newName)
            service.updateFolderNameV3(folderId, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it != null && it.data != null && it.data.isValue) {
                            mView?.updateName(true, null)
                        } else {
                            mView?.updateName(false, null)
                        }
                    }
                    onError { e, _ ->
                        mView?.updateName(false, e?.message)
                        XLog.error("重命名文件夹失败， folderId： $folderId , newName: $newName", e)
                    }
                }
        } else {
            mView?.updateName(false, null)
        }
    }

    override fun updateFileName(fileId: String, newName: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val body = RenamePost(newName)
            service.updateFileNameV3(fileId, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it != null && it.data != null && !TextUtils.isEmpty(it.data.id) ) {
                            mView?.updateName(true, null)
                        } else {
                            mView?.updateName(false, null)
                        }
                    }
                    onError { e, _ ->
                        mView?.updateName(false, e?.message)
                        XLog.error("重命名文件失败， fileId： $fileId , newName: $newName", e)
                    }
                }
        } else {
            mView?.updateName(false, null)
        }
    }

    override fun deleteFolderOrFile(item: CloudFileV3Data) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            if (item is CloudFileV3Data.FileItem) {
                service.deleteFileV3(item.id)
            } else {
                service.deleteFolderV3(item.id)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it != null && it.data != null && it.data.isValue ) {
                            mView?.delete(true, null)
                        } else {
                            mView?.delete(false, null)
                        }
                    }
                    onError { e, _ ->
                        mView?.delete(false, e?.message)
                        XLog.error("删除失败， id： ${item.id} , name: ${item.name}", e)
                    }
                }
        } else {
            mView?.delete(false, null)
        }
    }

    override fun moveToMyPan(parentId: String,
        files: List<CloudFileV3Data.FileItem>,
        folders: List<CloudFileV3Data.FolderItem>
    ) {

        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val body = MoveToMyPanPost(
                files.map { it.id },
                folders.map { it.id }
            )
            XLog.debug("保存到我的网盘， parentId: $parentId body: $body")
            service.moveToMyPan(parentId, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.moveToMyPan(true, null)
                    }
                    onError { e, _ ->
                        mView?.moveToMyPan(false, e?.message)
                        XLog.error("保存到网盘出错， parentId: $parentId", e)
                    }
                }
        } else {
            mView?.moveToMyPan(false, null)
        }
    }

    override fun move(
        parentId: String,
        files: List<CloudFileV3Data.FileItem>,
        folders: List<CloudFileV3Data.FolderItem>
    ) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {

            val moves : ArrayList<Observable<Boolean>> = ArrayList()
            if (files.isNotEmpty()) {
                val mf = files.map {
                    val body =  MoveV3Post(it.name, parentId, parentId)
                    service.moveFileV3(it.id, body).flatMap { moveback->
                        XLog.debug("执行完成 file，${moveback.type}")
                        Observable.just(true)
                    }
                }
                moves.addAll(mf)
            }
            if (folders.isNotEmpty()) {
                val mfo = folders.map {
                    val body =  MoveV3Post(it.name, parentId, parentId)
                    service.moveFolderV3(it.id, body).flatMap { moveback->
                        XLog.debug("执行完成 folder，${moveback.type}")
                        Observable.just(true)
                    }
                }
                moves.addAll(mfo)
            }
            XLog.debug("moves: ${moves.size}")
            Observable.merge(moves).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        XLog.info("merge 返回 $it")
                    }
                    onCompleted {
                        XLog.debug("move 成功！")
                        mView?.move(true, null)
                    }
                    onError { e, _ ->
                        mView?.move(false, e?.message)
                        XLog.error("移动企业文件失败", e)
                    }
                }

        }else {
            mView?.move(false, null)
        }
    }

    override fun uploadFileList(parentId: String, files: List<String>) {
        if (files.isEmpty() || files.size > 9) {
            mView?.uploadFile(false,"错误，上传附件个数太多！")
            return
        }
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val list = files.mapNotNull { path -> uploadFileObservable(parentId, path) }
            Observable.zip(list) {iddatas ->
                XLog.info("上传文件成功，size:${iddatas.size}")
                true
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.uploadFile(true, null)
                    }
                    onError { e, _ ->
                        mView?.uploadFile(false, e?.message ?: "错误！")
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.uploadFile(false, null)
        }

    }

    private fun uploadFileObservable(folderId: String, path: String) :  Observable<ApiResponse<IdData>>? {
        val file = File(path)
        if (!file.exists()) {
            return null
        }
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return getCloudFileV3ControlService(mView?.getContext())?.uploadFile2FolderV3(body, folderId)
    }

    override fun createFolder(parentId: String, name: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val params = HashMap<String, String>()
            params["name"] = name
            params["superior"] = parentId
            service.createFolderV3(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.createFolder(true, null)
                    }
                    onError { e, _ ->
                        mView?.createFolder(false, e?.message)
                        XLog.error("创建文件夹失败", e)
                    }
                }
        } else {
            mView?.createFolder(false, null)
        }

    }
}