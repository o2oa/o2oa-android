package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v2.share

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FileJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FolderJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.ShareJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.CloudDiskItem
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2021-01-21.
 * Copyright © 2021 O2. All rights reserved.
 */
class CloudSharePresenter : BasePresenterImpl<CloudShareContract.View>(), CloudShareContract.Presenter {


    /**
     * 获取我分享的文件列表
     * 默认 获取我分享的列表
     * 传入 文件夹参数 获取分享的文件夹内的列表数据
     */
    override fun getMyShareItemList(parentId: String, shareId: String) {

            if (TextUtils.isEmpty(shareId)) { // 顶层
                if (O2SDKManager.instance().appCloudDiskIsV3()) {
                    val service = getCloudFileV3ControlService(mView?.getContext())
                    if (service != null) {
                        val attaObservable = service.listMyShare("member", "attachment")
                        val folderObservable = service.listMyShare("member", "folder")
                        shareTop(folderObservable, attaObservable)
                    } else {
                        mView?.error("Server error!")
                    }
                } else {
                    val service = getCloudFileControlService(mView?.getContext())
                    if (service != null) {
                        val attaObservable = service.listMyShare("member", "attachment")
                        val folderObservable = service.listMyShare("member", "folder")
                        shareTop(folderObservable, attaObservable)
                    }else {
                        mView?.error("Server error!")
                    }
                }

            } else if (!TextUtils.isEmpty(shareId) && !TextUtils.isEmpty(parentId))  { //文件夹下内容查询
                if (O2SDKManager.instance().appCloudDiskIsV3()) {
                    val service = getCloudFileV3ControlService(mView?.getContext())
                    if (service != null) {
                        val folderObservable = service.listShareFolderWithFolder(shareId, parentId)
                        val attaObservable = service.listShareAttachmentWithFolder(shareId, parentId)
                        shareOther(folderObservable, attaObservable)
                    } else {
                        mView?.error("Server error!")
                    }
                } else {
                    val service = getCloudFileControlService(mView?.getContext())
                    if (service != null) {
                        val folderObservable = service.listShareFolderWithFolder(shareId, parentId)
                        val attaObservable = service.listShareAttachmentWithFolder(shareId, parentId)
                        shareOther(folderObservable, attaObservable)
                    }else {
                        mView?.error("Server error!")
                    }
                }
            } else {
                mView?.error("Server error!")
            }

    }

    private fun shareTop(folderObservable: Observable<ApiResponse<List<ShareJson>>>, attaObservable: Observable<ApiResponse<List<ShareJson>>>) {
        Observable.zip(folderObservable, attaObservable) { folder, attachment ->
            val items = ArrayList<CloudDiskItem>()
            folder.data.forEach {
                items.add(it.copyToItem())
            }
            attachment.data.forEach {
                items.add(it.copyToItem())
            }
            items
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext {
                    mView?.itemList(it)
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.itemList(ArrayList())
                }
            }
    }

    private fun shareOther(folderObservable:  Observable<ApiResponse<List<FolderJson>>>, attaObservable: Observable<ApiResponse<List<FileJson>>>) {
        Observable.zip(folderObservable, attaObservable) { folder, attachment ->
            val items = ArrayList<CloudDiskItem>()
            folder.data.forEach {
                items.add(it.copyToVO2())
            }
            attachment.data.forEach {
                items.add(it.copyToVO2())
            }
            items
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext {
                    mView?.itemList(it)
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.itemList(ArrayList())
                }
            }
    }

    override fun getShareToMeItemList(parentId: String, shareId: String) {
        val service = getCloudFileControlService(mView?.getContext())
        if (service != null) {
            if (TextUtils.isEmpty(shareId)) { // 顶层
                val attaObservable = service.listShareToMe("attachment")
                val folderObservable = service.listShareToMe("folder")
                Observable.zip(folderObservable, attaObservable) { folder, attachment ->
                    val items = ArrayList<CloudDiskItem>()
                    folder.data.forEach {
                        items.add(it.copyToItem())
                    }
                    attachment.data.forEach {
                        items.add(it.copyToItem())
                    }
                    items
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .o2Subscribe {
                            onNext {
                                mView?.itemList(it)
                            }
                            onError { e, _ ->
                                XLog.error("", e)
                                mView?.itemList(ArrayList())
                            }
                        }
            } else if (!TextUtils.isEmpty(shareId) && !TextUtils.isEmpty(parentId)) { //文件夹下内容查询
                val folderObservable = service.listShareFolderWithFolder(shareId, parentId)
                val attaObservable = service.listShareAttachmentWithFolder(shareId, parentId)
                Observable.zip(folderObservable, attaObservable) { folder, attachment ->
                    val items = ArrayList<CloudDiskItem>()
                    folder.data.forEach {
                        items.add(it.copyToVO2())
                    }
                    attachment.data.forEach {
                        items.add(it.copyToVO2())
                    }
                    items
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .o2Subscribe {
                            onNext {
                                mView?.itemList(it)
                            }
                            onError { e, _ ->
                                XLog.error("", e)
                                mView?.itemList(ArrayList())
                            }
                        }
            } else {
                mView?.error("Server error!")
            }
        }else {
            mView?.error("Server error!")
        }
    }

    override fun deleteMyShare(shareIds: List<String>) {
        val service = getCloudFileControlService(mView?.getContext())
        if (service != null && shareIds.isNotEmpty()) {
            val obs = shareIds.map {
                service.deleteMyShare(it)
            }
            Observable.merge(obs).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onError { e, _ ->
                            XLog.error("", e)
                            mView?.error(e?.message ?: "未知错误")
                        }
                        onCompleted {
                            mView?.success()
                        }
                    }
        }else {
            mView?.error("Server error!")
        }
    }

    override fun shieldShare(shareIds: List<String>) {
        val service = getCloudFileControlService(mView?.getContext())
        if (service != null && shareIds.isNotEmpty()) {
            val obs = shareIds.map {
                service.shieldShare(it)
            }
            Observable.merge(obs).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onError { e, _ ->
                            XLog.error("", e)
                            mView?.error(e?.message ?: "未知错误")
                        }
                        onCompleted {
                            mView?.success()
                        }
                    }
        }else {
            mView?.error("Server error!")
        }
    }
}