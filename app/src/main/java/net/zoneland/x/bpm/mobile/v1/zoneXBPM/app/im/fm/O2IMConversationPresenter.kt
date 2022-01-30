package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.fm

import android.text.TextUtils
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.xiaomi.push.it
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2.PRE_IM_CONFIG_KEY
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2AppUpdateManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2AppUpdateBeanData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConfig
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import okhttp3.OkHttpClient
import okhttp3.Request
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class O2IMConversationPresenter : BasePresenterImpl<O2IMConversationContract.View>(), O2IMConversationContract.Presenter {


    override fun createConversation(type: String, users: ArrayList<String>) {
        val service = getMessageCommunicateService(mView?.getContext())
        if (service != null) {
            val info = IMConversationInfo()
            info.type = type
            info.personList = users
            service.createConversation(info)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext {
                            if (it.data!= null) {
                                mView?.createConvSuccess(it.data)
                            }else{
                                mView?.createConvFail(mView?.getContext()?.getString(R.string.message_create_conversation_fail) ?: "创建会话失败！")
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            mView?.createConvFail(mView?.getContext()?.getString(R.string.message_create_conversation_fail) ?: "创建会话失败！${e?.message}")
                        }
                    }
        }
    }

    override fun getMyInstantMessageList() {
        val service = getMessageCommunicateService(mView?.getContext())
        service?.let { ser ->
            ser.instantMessageList(100)
                    .subscribeOn(Schedulers.io())
                    .flatMap { res ->
                        val list = res.data
                        if (list != null && list.isNotEmpty()) {
                            val newList = list.sortedBy { it.createTime }
                            Observable.just(newList)
                        }else {
                            Observable.just(ArrayList())
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext { list->
                            if (list != null) {
                                mView?.myInstantMessageList(list)
                            }else{
                                mView?.myInstantMessageList(ArrayList())
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            mView?.myInstantMessageList(ArrayList())
                        }
                    }

        }
    }

    override fun getMyConversationList() {
        val service = getMessageCommunicateService(mView?.getContext())
        service?.let {
            it.myConversationList().subscribeOn(Schedulers.io())
                    .flatMap { res ->
                        val list = res.data
                        if ( list != null && list.isNotEmpty()) {
                            val newList = list.sortedByDescending { c -> c.lastMessage?.createTime  }
                            Observable.just(newList)
                        }else {
                            Observable.just(ArrayList())
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext { list->
                            if (list != null) {
                                mView?.myConversationList(list)
                            }else{
                                mView?.myConversationList(ArrayList())
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                            mView?.myConversationList(ArrayList())
                        }
                    }
        }
    }

    override fun loadImConfig() {
        val configUrl = APIAddressHelper.instance().getConfigJsonUrl()
        val ranStr = O2AppUpdateManager.instance().getRandomStringOfLength(7)
        Observable.just("$configUrl?$ranStr").subscribeOn(Schedulers.io())
            .map { url ->
                XLog.debug(url)
                val request = Request.Builder().url(url).build()
                try {
                    val response = OkHttpClient().newCall(request).execute()
                    val json = response.body()?.string()
                    XLog.debug("json: $json")
                    if (json != null && !TextUtils.isEmpty(json)) {
                        val jObj: JsonObject =  O2SDKManager.instance().gson.fromJson(json, JsonObject::class.java)
                        var imConfigJson = ""
                        jObj.entrySet().forEach { item->
                            if (item.key == "imConfig") {
                                imConfigJson = O2SDKManager.instance().gson.toJson(item.value)
                            }
                        }
                        val config = if (TextUtils.isEmpty(imConfigJson)) {
                            O2SDKManager.instance().prefs().edit {
                                putString(PRE_IM_CONFIG_KEY, "")
                            }
                            IMConfig()
                        } else {
                            O2SDKManager.instance().prefs().edit {
                                putString(PRE_IM_CONFIG_KEY, imConfigJson)
                            }
                            O2SDKManager.instance().gson.fromJson(imConfigJson, IMConfig::class.java)
                        }
                        config
                    }else {
                        throw Exception("没有获取到前端配置文件config.json的内容！")
                    }
                }catch (e: Exception) {
                    XLog.error("", e)
                    throw e
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext {
                    mView?.loadImConfig(it)
                }
                onError { e, _ ->
                    XLog.error("", e)
                    mView?.loadImConfig(null)
                }
            }
    }
}