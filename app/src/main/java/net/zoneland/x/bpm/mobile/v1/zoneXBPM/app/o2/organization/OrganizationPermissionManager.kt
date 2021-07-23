package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.OrganizationPermissionData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog


/**
 * 应用市场 通讯录应用 可以管理通讯录权限
 * Created by fancyLou on 2021-07-20.
 * Copyright © 2021 O2. All rights reserved.
 */
class OrganizationPermissionManager private constructor()  {
    companion object {
        private var INSTANCE: OrganizationPermissionManager? = null

        fun instance(): OrganizationPermissionManager {
            if (INSTANCE == null) {
                synchronized(OrganizationPermissionManager::class) {
                    if (INSTANCE == null) {
                        INSTANCE = OrganizationPermissionManager()
                    }
                }
            }
            return INSTANCE!!
        }
    }

    var data: OrganizationPermissionData? = null
    // 隐藏手机号码的人员列表
    var hideMobilePersons  = ArrayList<String>()
    // 不查询的人员列表
    var excludePersons = ArrayList<String>()
    // 不查询的组织列表
    var excludeUnits = ArrayList<String>()
    // 不允许查询通讯录的人员
    var limitAll = ArrayList<String>()
    // 不允许查看外部门 包含人员和组织
    var limitOuter = ArrayList<String>()


    /**
     * 查询权限接口返回数据进行
     */
    fun initData(data: OrganizationPermissionData) {
        XLog.info("$data")
        this.data  = data
        hideMobilePersons.clear()
        hideMobilePersons.addAll( data.hideMobilePerson.split(",").toList())
        excludePersons.clear()
        excludePersons.addAll(data.excludePerson.split(",").toList())
        excludeUnits.clear()
        excludeUnits.addAll(data.excludeUnit.split(",").toList())
        limitAll.clear()
        limitAll.addAll(data.limitQueryAll.split(",").toList())
        limitOuter.clear()
        limitOuter.addAll(data.limitQueryOuter.split(",").toList())
    }



    /**
     * 判断 传入的人员是否需要隐藏手机号码
     * @param person 程剑@chengjian@P
     */
    fun isHiddenMobile(person: String): Boolean {
        return hideMobilePersons.contains(person)
    }

    /**
     * 判断 传入的人员是否要排除
     * @param person 程剑@chengjian@P
     */
    fun isExcludePerson(person: String): Boolean {
        return excludePersons.contains(person)
    }

    /**
     * 判断 传入的组织是否要排除
     * @param unit 团队领导@b7e3a8d3-21d4-4802-babf-9fc85392333d@U
     */
    fun isExcludeUnit(unit: String) : Boolean {
        return excludeUnits.contains(unit)
    }

    /**
     * 判断 当前用户是否不能查询通讯录
     */
    fun isCurrentPersonCannotQueryAll(): Boolean {
        val currentDN = O2SDKManager.instance().distinguishedName
        if (TextUtils.isEmpty(currentDN)) {
            return false
        }
        return limitAll.contains(currentDN)
    }

    /**
     * 判断 当前用户是否不能查询外部门
     */
    fun isCurrentPersonCannotQueryOuter(): Boolean {
        val currentPersonDN = O2SDKManager.instance().distinguishedName
        if (TextUtils.isEmpty(currentPersonDN)) {
            return false
        }
        // todo limitOuter还包含了部门数据
        return limitOuter.contains(currentPersonDN)
    }
}