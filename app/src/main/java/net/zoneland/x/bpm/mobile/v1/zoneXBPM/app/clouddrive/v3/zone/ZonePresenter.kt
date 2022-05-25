package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.zone

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileZoneData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.FavoritePost
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.ZonePost
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2022-05-19.
 * Copyright © 2022 o2android. All rights reserved.
 */
class ZonePresenter: BasePresenterImpl<ZoneContract.View>(), ZoneContract.Presenter {

    override fun loadZoneCreatorPermission() {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            service.isZoneCreator()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.canCreateZone((it != null && it.data!=null && it.data.isValue))
                    }
                    onError { e, _ ->
                        mView?.canCreateZone(false)
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.backError("")
        }
    }

    override fun loadZone() {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            Observable.zip(service.listMyFavorite(), service.listMyZone()) { favorite, zone ->
                val list = ArrayList<CloudFileZoneData>()
                if (favorite.data != null && favorite.data.isNotEmpty()) {
                    val header = CloudFileZoneData.GroupHeader(mView?.getContext()?.getString(R.string.cloud_file_my_favorite) ?: "我的收藏")
                    list.add(header)
                    list.addAll(favorite.data)
                }
                if (zone.data != null && zone.data.isNotEmpty()) {
                    val header = CloudFileZoneData.GroupHeader(mView?.getContext()?.getString(R.string.cloud_file_my_zone) ?: "共享工作区")
                    list.add(header)
                    list.addAll(zone.data)
                }
                list
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { mView?.zoneList(it) }
                    onError { e, _ ->
                        mView?.zoneList(ArrayList())
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.zoneList(ArrayList())
        }
    }

    override fun createZone(name: String, desc: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val body = ZonePost(name, desc)
            service.createZone(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.createZoneSuccess()
                    }
                    onError { e, _ ->
                        mView?.backError(e?.message ?: "")
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.backError("")
        }
    }

    override fun updateZone(id: String, name: String, desc: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val body = ZonePost(name, desc)
            service.updateZone(id, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.updateZoneSuccess()
                    }
                    onError { e, _ ->
                        mView?.backError(e?.message ?: "")
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.backError("")
        }
    }

    override fun deleteZone(id: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            service.deleteZone(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.deleteZoneSuccess()
                    }
                    onError { e, _ ->
                        mView?.backError(e?.message ?: "")
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.backError("")
        }
    }

    override fun addFavorite(name: String, zoneId: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val body = FavoritePost(name, zoneId, "")
            service.createFavorite(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.addFavoriteSuccess()
                    }
                    onError { e, _ ->
                        mView?.backError(e?.message ?: "")
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.backError("")
        }
    }

    override fun renameFavorite(name: String, id: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            val body = FavoritePost(name, "", "")
            service.updateFavorite(id, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.renameFavoriteSuccess()
                    }
                    onError { e, _ ->
                        mView?.backError(e?.message ?: "")
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.backError("")
        }
    }

    override fun cancelFavorite(id: String) {
        val service = getCloudFileV3ControlService(mView?.getContext())
        if (service != null) {
            service.deleteFavorite(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        mView?.cancelFavoriteSuccess()
                    }
                    onError { e, _ ->
                        mView?.backError(e?.message ?: "")
                        XLog.error("", e)
                    }
                }
        } else {
            mView?.backError("")
        }
    }
}