package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.zone

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileZoneData
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
}