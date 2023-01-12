package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base

import android.content.Context
import android.widget.TextView

/**
 * Created by fancy on 2017/6/5.
 */


interface BasePresenter<in V : BaseView> {
    fun attachView(view: V)
    fun detachView()
    fun jPushBindDevice()
    fun jPushUnBindDevice()
    // 查询待办数量
    fun getTaskNumber(context: Context?, tv: TextView, tvTag: String)
    // 查询待阅数量
    fun getReadNumber(context: Context?, tv: TextView, tvTag: String)
}