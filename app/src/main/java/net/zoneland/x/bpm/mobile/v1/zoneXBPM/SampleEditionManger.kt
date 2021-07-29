package net.zoneland.x.bpm.mobile.v1.zoneXBPM

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.reflect.TypeToken
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.CollectUnitData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit

/**
 * 演示版本 管理器
 * Created by fancyLou on 2021-07-29.
 * Copyright © 2021 O2. All rights reserved.
 */
class SampleEditionManger {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: SampleEditionManger? = null

        fun instance(): SampleEditionManger {
            if (INSTANCE == null) {
                synchronized(SampleEditionManger::class) {
                    if (INSTANCE == null) {
                        INSTANCE = SampleEditionManger()
                    }
                }
            }
            return INSTANCE!!
        }
    }

    private lateinit var context: Context
    private var unitList = ArrayList<CollectUnitData>()
    private var currentUnit: CollectUnitData? = null

    fun initConfig(context: Context) {
        this.context = context
        readAssetsServers()
        readCurrentServer()
    }

    /**
     * 获取能访问的环境
     */
    fun getUnits(): ArrayList<CollectUnitData> {
        return unitList
    }

    /**
     * 获取当前环境信息
     */
    fun getCurrent(): CollectUnitData {
        if (currentUnit == null) {
            currentUnit = CollectUnitData()
            currentUnit!!.id = "sample"
            currentUnit!!.name = "演示环境"
            currentUnit!!.centerHost = "sample.o2oa.net"
            currentUnit!!.centerContext = "/x_program_center"
            currentUnit!!.httpProtocol = "https"
            currentUnit!!.centerPort = 40030
        }
        val json = O2SDKManager.instance().gson.toJson(currentUnit)
        Log.i("SampleEditionManger", "当前连接环境。。。。。。unit: $json")
        return currentUnit!!
    }

    /**
     * 设置新环境
     */
    fun setCurrent(unit: CollectUnitData) {
        currentUnit = unit
        val json = O2SDKManager.instance().gson.toJson(unit)
        O2SDKManager.instance().prefs().edit {
            putString(O2.PRE_SAMPLE_EDITION_CURRENT_SERVER_INFO_KEY, json)
        }
        Log.i("SampleEditionManger", "切换连接环境。。。。。。unit: $json")
    }

    /**
     * 读取 assets/servers.json 文件
     */
    private fun readAssetsServers() {
        val json = context.resources?.assets?.open("servers.json")
        if (json != null) {
            val len = json.available()
            val buffer = ByteArray(len)
            json.read(buffer)
            val jsonString = String(buffer, Charsets.UTF_8)
            Log.i("SampleEditionManger", jsonString)
            val servers: List<CollectUnitData> = O2SDKManager.instance().gson.fromJson(jsonString, object : TypeToken<List<CollectUnitData>>(){}.type)
            unitList.clear()
            unitList.addAll(servers)
        } else {
            Log.e("SampleEditionManger", "SampleEditionManger fail ，读取servers.json失败！！！")
        }
    }

    /**
     * 读取当前连接的服务器信息
     */
    private fun readCurrentServer() {
        val serverJson = O2SDKManager.instance().prefs().getString(O2.PRE_SAMPLE_EDITION_CURRENT_SERVER_INFO_KEY, "")
        if (TextUtils.isEmpty(serverJson)) {
            if (unitList.isNotEmpty()) {
                currentUnit = unitList[0]
                val json = O2SDKManager.instance().gson.toJson(currentUnit)
                O2SDKManager.instance().prefs().edit {
                    putString(O2.PRE_SAMPLE_EDITION_CURRENT_SERVER_INFO_KEY, json)
                }
            }
        } else {
            currentUnit = O2SDKManager.instance().gson.fromJson(serverJson, CollectUnitData::class.java)
        }
    }


}