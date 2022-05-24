package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.picker

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FolderItemForPicker
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class CloudDiskFolderPickerPresenter : BasePresenterImpl<CloudDiskFolderPickerContract.View>(), CloudDiskFolderPickerContract.Presenter {

    override fun getItemList(parentId: String) {

        val listFolderObservable = if (parentId.isEmpty()) {
            if (O2SDKManager.instance().appCloudDiskIsV3()) {
                val service = getCloudFileV3ControlService(mView?.getContext())
                service?.listFolderTop()
            }else {
                val service = getCloudFileControlService(mView?.getContext())
                service?.listFolderTop()
            }
        }else {
            if (O2SDKManager.instance().appCloudDiskIsV3()) {
                val service = getCloudFileV3ControlService(mView?.getContext())
                service?.listFolderByFolderId(parentId)
            }else {
                val service = getCloudFileControlService(mView?.getContext())
                service?.listFolderByFolderId(parentId)
            }
        }
        if (listFolderObservable == null) {
            mView?.error("服务为空！")
            return
        }
        listFolderObservable.subscribeOn(Schedulers.io())
                .flatMap {
                    val list = ArrayList<FolderItemForPicker>()
                    val folderList = it.data
                    if (folderList != null && folderList.isNotEmpty()) {
                        folderList.forEach { folder ->
                            val vo = FolderItemForPicker(folder.id, folder.name, folder.updateTime)
                            list.add(vo)
                        }
                    }
                    Observable.just(list)
                }
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

    override fun getFolderListV3(parentId: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            service.listFolderByFolderIdV3(parentId, "updateTime")
                .subscribeOn(Schedulers.io())
                .flatMap {
                    val list = ArrayList<FolderItemForPicker>()
                    val folderList = it.data
                    if (folderList != null && folderList.isNotEmpty()) {
                        folderList.forEach { folder ->
                            val vo = FolderItemForPicker(folder.id, folder.name, folder.updateTime)
                            list.add(vo)
                        }
                    }
                    Observable.just(list)
                }.observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.itemList(it)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        mView?.error(e?.message ?: "错误！")
                    }
                }
        } else {
            mView?.itemList(ArrayList())
        }
    }
}