package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.main

import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import kotlinx.android.synthetic.main.fragment_attendance_check_in_new.*
import kotlinx.android.synthetic.main.fragment_attendance_check_in_v2.*
import kotlinx.android.synthetic.main.fragment_attendance_check_in_v2.image_attendance_check_in_new_location_check_icon
import kotlinx.android.synthetic.main.fragment_attendance_check_in_v2.rl_attendance_check_in_new_knock_btn
import kotlinx.android.synthetic.main.fragment_attendance_check_in_v2.rv_attendance_check_in_new_schedules
import kotlinx.android.synthetic.main.fragment_attendance_check_in_v2.tv_attendance_check_in_new_check_in
import kotlinx.android.synthetic.main.fragment_attendance_check_in_v2.tv_attendance_check_in_new_now_time
import kotlinx.android.synthetic.main.fragment_attendance_check_in_v2.tv_attendance_check_in_new_workplace
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPViewPagerFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.MessageType
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.CheckButtonDoubleClick
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.GridLayoutItemDecoration
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import org.jetbrains.anko.dip
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by fancyLou on 2020-07-17.
 * Copyright © 2020 O2. All rights reserved.
 */

class AttendanceCheckInV2NewFragment : BaseMVPViewPagerFragment<AttendanceCheckInV2Contract.View, AttendanceCheckInV2Contract.Presenter>(),
    AttendanceCheckInV2Contract.View {
    override var mPresenter: AttendanceCheckInV2Contract.Presenter = AttendanceCheckInV2Presenter()
    override fun layoutResId(): Int = R.layout.fragment_attendance_check_in_v2


    private val workplaceList = ArrayList<AttendanceV2WorkPlace>()
    private val recordList = ArrayList<AttendanceV2CheckItemData>()
    private var nextCheckInRecord: AttendanceV2CheckItemData? = null
    private var needCheckIn = false //是否可打卡
    private var allowFieldWork = false // 是否允许外勤
    private var requiredFieldWorkRemarks = false // 外勤是否必须打卡

    private val recordAdapter: CommonRecycleViewAdapter<AttendanceV2CheckItemData> by lazy {
        object : CommonRecycleViewAdapter<AttendanceV2CheckItemData>(activity, recordList, R.layout.item_attendance_check_in_schdule_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: AttendanceV2CheckItemData?) {
                if (holder != null && t != null) {
                    val status = if(t.isRecord) {
                        t.recordTime
                    } else {
                        getString(R.string.attendance_v2_need_check_in)
                    }
                    holder.setText(R.id.tv_item_attendance_check_in_schedule_list_type, t.checkInTypeString)
                        .setText(R.id.tv_item_attendance_check_in_schedule_list_time, t.preDutyTime)
                        .setText(R.id.tv_item_attendance_check_in_schedule_list_status, status)
                    val updateBtn = holder.getView<TextView>(R.id.tv_item_attendance_check_in_schedule_list_update_btn)
                    updateBtn.gone()
                    if (t.isLastRecord) {
                        updateBtn.visible()
                    }
                }
            }
        }
    }


    //定位
    private val mLocationClient: LocationClient by lazy { LocationClient(activity) }
    private var myLocation: BDLocation? = null //当前我的位置
    private var checkInPosition: AttendanceV2WorkPlace? = null//离的最近的工作地点位置
    private var isInCheckInPositionRange = false


    //刷新打卡按钮的时间
    private val handler = Handler { msg ->
        if (msg.what == 1) {
            val nowTime = DateHelper.nowByFormate("HH:mm:ss")
            tv_attendance_check_in_new_now_time?.text = nowTime
        }
        return@Handler true
    }
    private val timerTask = object : TimerTask() {
        override fun run() {
            val message = Message()
            message.what = 1
            handler.sendMessage(message)
        }
    }
    private val timer: Timer by lazy { Timer() }


    override fun lazyLoad() {
        mPresenter.preCheckDataLoad()
    }

    override fun initUI() {
        LocationClient.setAgreePrivacy(true)
        //定位
        mLocationClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation?) {
                XLog.debug("onReceive locType:${location?.locType}, latitude:${location?.latitude}, longitude:${location?.longitude}")
                if (location != null) {
                    myLocation = location
                    //计算
                    calNearestWorkplace()
                }
            }
        })
        initBaiduLocation()
        mLocationClient.start()

        //打卡班次
        rv_attendance_check_in_new_schedules.layoutManager = GridLayoutManager(activity, 2)
        rv_attendance_check_in_new_schedules.addItemDecoration(GridLayoutItemDecoration(activity?.dip(10) ?: 10, activity?.dip(10) ?: 10, 2))
        rv_attendance_check_in_new_schedules.adapter = recordAdapter
        recordAdapter.setOnItemClickListener { _, position ->
            // 点击更新打卡
            clickUpdateRecord(recordList[position])
        }

        //打卡按钮
        rl_attendance_check_in_new_knock_btn.setOnClickListener {
            if (CheckButtonDoubleClick.isFastDoubleClick(R.id.rl_attendance_check_in_new_knock_btn)) {
                return@setOnClickListener
            }
            // 打卡
            clickCheckIn()
        }
        //时间
        timer.schedule(timerTask, 0, 1000)
    }

    private fun setCheckInBtnEnable(enable: Boolean) {
        needCheckIn = enable
        val draw = rl_attendance_check_in_new_knock_btn.background as? GradientDrawable
        if (enable) {
            activity?.let {
                draw?.setColor(ContextCompat.getColor(it, R.color.z_color_primary))
            }
        } else {
            activity?.let {
                draw?.setColor(ContextCompat.getColor(it, R.color.disabled))
            }
        }
    }


    override fun onDestroyView() {
        timer.cancel()
        timerTask.cancel()
        super.onDestroyView()
    }

    override fun onDestroy() {
        // 退出时销毁定位
        mLocationClient.stop()
        super.onDestroy()
    }

    override fun preCheckData(data: AttendanceV2PreCheckData?) {
         if (data == null) {
             setCheckInBtnEnable(false)
             return
         }
        XLog.debug("$data")
        needCheckIn = data.canCheckIn   //今天是否还需要打卡
        allowFieldWork = data.allowFieldWork
        requiredFieldWorkRemarks = data.requiredFieldWorkRemarks
        workplaceList.clear()
        workplaceList.addAll(data.workPlaceList ?: ArrayList())
        // 检查是否在范围内
        calNearestWorkplace()
        if (needCheckIn) {
            // 打卡记录
            val checkItemList = data.checkItemList ?: ArrayList()
            // 先排序 防止顺序错乱
            checkItemList.sortBy { it.preDutyTime }
            // 是否最后一条已经打卡过的数据
            nextCheckInRecord = checkItemList.firstOrNull { element -> element.checkInResult == AttendanceV2RecordResult.PreCheckIn.value }
            needCheckIn = nextCheckInRecord != null
            for ((index, item) in checkItemList.withIndex()) {
                var isRecord = false
                var recordTime = ""
                if (item.checkInResult != AttendanceV2RecordResult.PreCheckIn.value) {
                    isRecord = true
                    var signTime = item.recordDate
                    if (signTime.length > 16) {
                        signTime = signTime.substring(11, 16);
                    }
                    var status = getString(R.string.attendance_v2_check_in_completed)
                    if (item.checkInResult != AttendanceV2RecordResult.Normal.value) {
                        status = item.resultText()
                    }
                    recordTime = "$status $signTime"
                }
                item.recordTime = recordTime
                item.isRecord = isRecord // 是否已经打卡
                item.checkInTypeString =  if(item.checkInType == AttendanceV2RecordCheckInType.OnDuty.value) { AttendanceV2RecordCheckInType.OnDuty.label }else{ AttendanceV2RecordCheckInType.OffDuty.label }
                var preDutyTime = item.preDutyTime
                if (TextUtils.isEmpty(item.shiftId)) {
                    preDutyTime = "" // 如果没有班次信息 表示 自由工时 或者 休息日 不显示 打卡时间
                }
                item.preDutyTime = preDutyTime
                // 处理是否是最后一个已经打卡的记录
                if (item.checkInResult != AttendanceV2RecordResult.PreCheckIn.value) {
                    if (index == checkItemList.size - 1) { // 最后一条
                        item.isLastRecord = true // 最后一条已经打卡的记录
                    } else {
                        val nextItem = checkItemList[index+1]
                        if (nextItem.checkInResult == AttendanceV2RecordResult.PreCheckIn.value) {
                            item.isLastRecord = true
                        }
                    }
                }
                checkItemList[index] = item
            }
            recordList.clear()
            recordList.addAll(checkItemList)
        }
        // 刷新页面
        setCheckInBtnEnable(needCheckIn)
        recordAdapter.notifyDataSetChanged()
    }

    override fun checkInPostResponse(result: Boolean, message: String?) {
        tv_attendance_check_in_new_check_in?.setText(R.string.attendance_check_in_knock)
        tv_attendance_check_in_new_now_time?.visible()
        if (result) {
            XToast.toastShort(R.string.attendance_v2_check_in_success)
        } else if (!TextUtils.isEmpty(message)) {
            XToast.toastShort(message!!)
        }
        mPresenter.preCheckDataLoad()
    }

    /**
     * 点击更新打卡
     */
    private fun clickUpdateRecord(record: AttendanceV2CheckItemData) {
        if (myLocation == null || TextUtils.isEmpty(myLocation?.addrStr)) {
            XLog.error("没有定位到信息，可能是定位权限没开！！！")
            XToast.toastShort(activity!!, R.string.attendance_message_no_location_info)
            return
        }

        if (record.isLastRecord) { // 只有最后一条可以更新
            tv_attendance_check_in_new_check_in.text = getString(R.string.attendance_check_in_knock_loading)
            tv_attendance_check_in_new_now_time.gone()
            if (isInCheckInPositionRange && checkInPosition != null) { // 正常打卡
                postCheckIn(record, checkInPosition!!.id, false, null)
            } else {
                // 外勤
                outSide(record)
            }
        } else {
            XLog.info("不是最后一条，不能更新打卡，怎么点击到的？？？")
        }
    }
    /**
     * 点击打卡
     */
    private fun clickCheckIn() {
        if (myLocation == null || TextUtils.isEmpty(myLocation?.addrStr)) {
            XLog.error("没有定位到信息，可能是定位权限没开！！！")
            XToast.toastShort(activity!!, R.string.attendance_message_no_location_info)
            return
        }
        if (needCheckIn && nextCheckInRecord != null) {
            // 是否在打卡限制时间内
            val preBeforeTime = nextCheckInRecord?.preDutyTimeBeforeLimit ?: ""
            val preAfterTime = nextCheckInRecord?.preDutyTimeAfterLimit ?: ""
            if (!checkLimitTime(preBeforeTime, preAfterTime)) {
                return
            }
            tv_attendance_check_in_new_check_in.text = getString(R.string.attendance_check_in_knock_loading)
            tv_attendance_check_in_new_now_time.gone()
            if (isInCheckInPositionRange && checkInPosition != null) { // 正常打卡
                postCheckIn(nextCheckInRecord!!, checkInPosition!!.id, false, null)
            } else {
                // 外勤
                outSide(nextCheckInRecord!!)
            }
        } else {
            XLog.info("不允许打卡或nextCheckInRecord is null")
        }
    }


    // 是否有打卡时间限制
    private fun checkLimitTime(preDutyTimeBeforeLimit: String, preDutyTimeAfterLimit: String): Boolean {
        if (!TextUtils.isEmpty(preDutyTimeBeforeLimit)) {
            val now = Date()
            val today = DateHelper.nowByFormate("yyyy-MM-dd")
            val beforeTime = DateHelper.convertStringToDate("$today $preDutyTimeBeforeLimit:00")
            if (beforeTime != null && now.time <  beforeTime.time) {
                XToast.toastShort(getString(R.string.attendance_v2_not_in_check_in_time, preDutyTimeBeforeLimit, preDutyTimeAfterLimit))
                return false
            }
        }
        if (!TextUtils.isEmpty(preDutyTimeAfterLimit)) {
            val now = Date()
            val today = DateHelper.nowByFormate("yyyy-MM-dd")
            var afterTime = DateHelper.convertStringToDate("$today $preDutyTimeAfterLimit:00");
            if (afterTime != null && now.time > afterTime.time) {
                XToast.toastShort(getString(R.string.attendance_v2_not_in_check_in_time, preDutyTimeBeforeLimit, preDutyTimeAfterLimit))
                return false
            }
        }
        return true
    }

    /**
     * 外勤打卡处理
     */
    private fun outSide(record: AttendanceV2CheckItemData) {
        if (allowFieldWork) {
            if (requiredFieldWorkRemarks) {
                if (activity != null) {
                    val dialog = O2DialogSupport.openCustomViewDialog(
                        activity!!,
                        getString(R.string.attendance_message_work_out),
                        R.layout.dialog_name_modify
                    ) { dialog ->
                        val text = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
                        if (TextUtils.isEmpty(text.text.toString())) {
                            XToast.toastShort(activity!!, R.string.attendance_message_work_out_hint)
                        } else {
                            postCheckIn(record, null, true, text.text.toString())
                        }
                    }
                    val text = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
                    text.hint = getString(R.string.attendance_message_work_out_hint)
                }
            } else {
                postCheckIn(record, null, true, null)
            }
        } else {
            XToast.toastShort(activity, "不在工作地点的打卡范围内！")
        }
    }

    /**
     * 提交打卡信息
     */
    private fun postCheckIn(record: AttendanceV2CheckItemData, workPlaceId: String?, outSide: Boolean, outSideDesc:String?) {
        val body = AttendanceV2CheckInBody()
        body.recordId = record.id
        body.checkInType = record.checkInType
        body.latitude = myLocation!!.latitude.toString()
        body.longitude =  myLocation!!.longitude.toString()
        body.recordAddress = myLocation!!.addrStr
        body.workPlaceId = workPlaceId ?: ""
        body.fieldWork = outSide
        body.signDescription = outSideDesc ?: ""
        XLog.debug(body.toString())
        mPresenter.checkInPost(body)
    }

    /**
     * 检查是否进入打卡范围
     */
    private fun checkIsInWorkplace() {
        XLog.info("checkIsInWorkplace.....${checkInPosition?.placeName}, ${myLocation?.addrStr}")
        if (checkInPosition != null && myLocation != null) {
            val workplacePosition = LatLng(checkInPosition!!.latitude.toDouble(), checkInPosition!!.longitude.toDouble())
            val position = LatLng(myLocation!!.latitude, myLocation!!.longitude)
            val distance = DistanceUtil.getDistance(position, workplacePosition)
            XLog.info("distance:$distance")
            if (distance < checkInPosition!!.errorRange) {
                isInCheckInPositionRange = true
                activity?.runOnUiThread {
                    tv_attendance_check_in_new_workplace.text = checkInPosition?.placeName
                    image_attendance_check_in_new_location_check_icon.setImageResource(R.mipmap.list_selected)
                }
            } else {
                isInCheckInPositionRange = false
                activity?.runOnUiThread {
                    tv_attendance_check_in_new_workplace.text = myLocation?.addrStr
                    image_attendance_check_in_new_location_check_icon.setImageResource(R.mipmap.icon_delete_people)
                }
            }
        }
    }

    /**
     * 找到最近的打卡地点
     */
    private fun calNearestWorkplace() {
        if ( myLocation!=null) {
            if (workplaceList.isNotEmpty()) {
                var minDistance: Double = -1.0
                XLog.debug("calNearestWorkplace...................")
                workplaceList.map {
                    val p2 = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                    val position = LatLng(myLocation!!.latitude, myLocation!!.longitude)
                    val distance = DistanceUtil.getDistance(position, p2)
                    if (minDistance == -1.0) {
                        minDistance = distance
                        checkInPosition = it
                    } else {
                        if (minDistance > distance) {
                            minDistance = distance
                            checkInPosition = it
                        }
                    }
                }
                XLog.info("checkInposition:${checkInPosition?.placeName}")
                checkIsInWorkplace()
            } else {
                activity?.runOnUiThread {
                    tv_attendance_check_in_new_workplace.text = myLocation?.addrStr
                }
            }
        }
    }

    private fun initBaiduLocation() {
        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll")//百度坐标系 可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(5000)//5秒一次定位 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true)//可选，设置是否需要地址信息，默认不需要
        option.isOpenGps = true//可选，默认false,设置是否使用gps
        option.isLocationNotify = true//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true)//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true)//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false)//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false)//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false)//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.locOption = option
    }
}