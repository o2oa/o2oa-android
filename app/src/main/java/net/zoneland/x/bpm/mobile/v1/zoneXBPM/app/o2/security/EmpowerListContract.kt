package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.EmpowerData

/**
 * Created by fancyLou on 2023-05-16.
 * Copyright © 2023 o2android. All rights reserved.
 */
object EmpowerListContract {
    interface View: BaseView {
        fun myEmpowerList(myList: List<EmpowerData>)
        fun myEmpowerListTo(myListTo: List<EmpowerData>)
        fun error(errorMsg: String)
    }
    interface Presenter: BasePresenter<View>{
        /**
         * 我的委托
         */
        fun myEmpowerList()

        /**
         * 我收到的委托
         */
        fun myEmpowerListTo()

    }
}