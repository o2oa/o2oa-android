package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.provider.Settings
import android.text.TextUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2App
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.RetrofitClient
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service.AttendanceAssembleControlService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * Created by fancyLou on 2023-04-14.
 * Copyright © 2023 o2android. All rights reserved.
 */
class FastCheckInManager() {

    //定位
    private val mLocationClient: LocationClient by lazy { LocationClient(O2App.instance) }
    private var myLocation: BDLocation? = null // 当前我的位置
    private var checkInPosition: AttendanceV2WorkPlace? = null// 离的最近的工作地点位置
    private var isInCheckInPositionRange = false

    private val workplaceList = ArrayList<AttendanceV2WorkPlace>()
//    private val recordList = ArrayList<AttendanceV2CheckItemData>()
    private var nextCheckInRecord: AttendanceV2CheckItemData? = null // 需要打卡的数据
    private var needCheckIn = false //是否可打卡
    private var allowFieldWork = false // 是否允许外勤
    private var onDutyFastCheckInEnable = false // 上班极速打卡
    private var offDutyFastCheckInEnable = false // 下班极速打卡

    // 倒计时timer 持续定位时间不超过5分钟
    private var countDownTimer: CountDownTimer? = null

    private val service: AttendanceAssembleControlService? = try {
        RetrofitClient.instance().attendanceAssembleControlApi()
    } catch (e: Exception) {
        XLog.error("", e)
        null
    }


    // 是否已经结束 防止 多次 start的情况
    private var isChecking = false

    /**
     * 启动
     */
    fun start(activity: Activity) {
        if (isChecking) {
            XLog.info("还没有结束 不执行！！！！")
            return
        }
        isChecking = true
        // 首先加载配置文件
        if (service != null) {
            service.attendanceV2Config()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        val config = it.data
                        if (config != null && (config.onDutyFastCheckInEnable || config.offDutyFastCheckInEnable)) {
                            onDutyFastCheckInEnable= config.onDutyFastCheckInEnable
                            offDutyFastCheckInEnable = config.offDutyFastCheckInEnable
                            PermissionRequester(activity).request(Manifest.permission.ACCESS_FINE_LOCATION)
                                .o2Subscribe {
                                    onNext {  (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
                                        if (!granted){
                                            O2DialogSupport.openAlertDialog(activity, "需要定位权限, 去设置", {
                                                isChecking = false
                                                val packageUri = Uri.parse("package:${activity.packageName}")
                                                activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
                                            })
                                        }else{
                                            startAfterPermission()
                                        }
                                    }
                                    onError { e, _ ->
                                        XLog.error( "检查权限出错", e)
                                    }
                                }

                        }
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                    }
                }
        } else {
            XLog.error("配置文件加载失败，没有极速打开功能！！！！！！！")
        }
    }

    /**
     * 结束
     */
    fun stopAll() {
        stopLocation()
        countDownTimer?.cancel()
        XLog.info("极速打卡，全部结束了。。。。")
        isChecking = false
    }

    private fun startAfterPermission() {
        // 开始计算逻辑 进行定位和打卡
        startLocation()
        startTimer()
        preCheckDataLoad()
    }

    /**
     * 开始倒计时 5分钟后没有结果直接停止
     */
    private fun startTimer() {
        // 由于CountDownTimer并不是准确计时，在onTick方法调用的时候，time会有1-10ms左右的误差，这会导致最后一秒不会调用onTick()
        // 因此，设置间隔的时候，默认减去了10ms，从而减去误差。
        // 经过以上的微调，最后一秒的显示时间会由于10ms延迟的积累，导致显示时间比1s长max*10ms的时间，其他时间的显示正常,总时间正常
        if (countDownTimer == null) {
            // 2分钟倒计时
            countDownTimer = object : CountDownTimer((60 * 2) * 1000,   1000 - 10) {
                override fun onTick(time: Long) {
                    XLog.debug( "极速打卡倒计时, time = $time text = ${(time + 15) / 1000}"  )
                }

                override fun onFinish() {
                    XLog.info("倒计时结束， 全部停止。")
                    stopAll()
                }
            }
        }
        countDownTimer?.start()
        XLog.info("开始倒计时")
    }

    /**
     * 尝试打卡
     */
    private fun tryCheckIn() {
        if (myLocation == null || TextUtils.isEmpty(myLocation?.addrStr)) {
            XLog.error("没有定位到信息，可能是定位权限没开！！！")
            return
        }
        if (needCheckIn && nextCheckInRecord != null && isInCheckInPositionRange && checkInPosition != null) {
            // 上班打卡
            if ((nextCheckInRecord?.checkInType == AttendanceV2RecordCheckInType.OnDuty.value && onDutyFastCheckInEnable)
                ||
                (nextCheckInRecord?.checkInType == AttendanceV2RecordCheckInType.OffDuty.value && offDutyFastCheckInEnable)) {
                // 是否在打卡限制时间内
                val dutyTime = nextCheckInRecord?.preDutyTime ?: ""
                val preBeforeTime = nextCheckInRecord?.preDutyTimeBeforeLimit ?: ""
                val preAfterTime = nextCheckInRecord?.preDutyTimeAfterLimit ?: ""
                if (!checkLimitTime(nextCheckInRecord!!.checkInType, dutyTime, preBeforeTime, preAfterTime)) {
                    XLog.error("不在限制时间内！！！！")
                    return
                }
                postCheckIn(nextCheckInRecord!!, checkInPosition!!.id)
            } else {
                XLog.info("当前打卡类型：${nextCheckInRecord?.checkInType} 不允许极速打卡！")
                stopAll()
            }
        }
    }
    // 是否有打卡时间限制
    private fun checkLimitTime(checkInType: String,  dutyTime: String, preDutyTimeBeforeLimit: String, preDutyTimeAfterLimit: String): Boolean {
        val now = Date()
        val today = DateHelper.getDateTime("yyyy-MM-dd", now)
        val dutyTimeDate = DateHelper.convertStringToDate("$today $dutyTime:00")
        // 极速打卡开始时间
        var fastCheckInBeforeLimit = if (checkInType == AttendanceV2RecordCheckInType.OnDuty.value) {
            DateHelper.addMinute(dutyTimeDate, -60) //上班前一个小时
        } else {
            dutyTimeDate
        }
        // 极速打卡结束时间
        var fastCheckInAfterLimit = if (checkInType == AttendanceV2RecordCheckInType.OnDuty.value) {
            dutyTimeDate
        } else {
            DateHelper.addMinute(dutyTimeDate, 60) // 下班后1个小时
        }

        if (!TextUtils.isEmpty(preDutyTimeBeforeLimit)) { // 考勤组配置了开始打卡时间的
            val beforeTime = DateHelper.convertStringToDate("$today $preDutyTimeBeforeLimit:00")
            if (beforeTime != null && beforeTime.after(fastCheckInBeforeLimit)) {
                fastCheckInBeforeLimit = beforeTime
            }
        }
        if (!TextUtils.isEmpty(preDutyTimeAfterLimit)) {
            val afterTime = DateHelper.convertStringToDate("$today $preDutyTimeAfterLimit:00");
            if (afterTime != null && afterTime.before(fastCheckInAfterLimit)) {
                fastCheckInAfterLimit = afterTime
            }
        }
        // 当前时间在 限制时间内
        XLog.info("打卡时间，$dutyTimeDate 极速打卡开始时间：$fastCheckInBeforeLimit 极速打卡结束时间： $fastCheckInAfterLimit")
        if (now.after(fastCheckInBeforeLimit) && now.before(fastCheckInAfterLimit)) {
            return true
        }
        return false
    }

    /**
     * 开始定位 包含初始化sdk
     */
    private fun startLocation() {
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
        mLocationClient.start() // 开始定位
    }
    // 结束定位
    private fun stopLocation() {
        mLocationClient.stop()
    }

    /**
     * 获取预打卡请求数据
     */
    private fun preCheckDataLoad() {
        if (service != null) {
            service.attendanceV2PreCheck()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        preCheckData(it?.data)
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        XLog.info("没有数据， 全部停止。")
                        stopAll()
                    }
                }
        } else {
            XLog.error("考勤服务模块加载失败！！！！！！！！")
            stopAll()
        }
    }
    /**
     * 提交打卡信息
     */
    private var isPosting = false // 防止重复提交
    private fun postCheckIn(record: AttendanceV2CheckItemData, workPlaceId: String) {
        if (isPosting) {
            XLog.info("正在提交中。。。。。。。")
            return
        }
        isPosting = true
        val body = AttendanceV2CheckInBody()
        body.recordId = record.id
        body.checkInType = record.checkInType
        body.latitude = myLocation!!.latitude.toString()
        body.longitude =  myLocation!!.longitude.toString()
        body.recordAddress = myLocation!!.addrStr
        body.workPlaceId = workPlaceId
        body.fieldWork = false
        body.signDescription = ""
        body.sourceType = "FAST_CHECK" // 极速打卡
        if (service != null) {
            service.attendanceV2CheckIn(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext {
                        if (it != null && it.data != null) {
                            XLog.info("极速打卡成功！")
                            stopAll()
                            // 发送通知
                            val intent = Intent(O2.FAST_CHECK_IN_RECEIVER_ACTION)
                            intent.putExtra(O2.FAST_CHECK_IN_RECORD_TIME_KEY, it.data.recordDate)
                            LocalBroadcastManager.getInstance(O2App.instance.applicationContext).sendBroadcast(intent)
                        } else {
                            isPosting = false
                        }
                    }
                    onError { e, _ ->
                        XLog.error("", e)
                        isPosting = false
                    }
                }
        } else {
            isPosting = false
        }
    }

    private fun preCheckData(data: AttendanceV2PreCheckData?) {
        if (data == null) {
            XLog.error("获取打卡信息错误，结束极速打卡！！！！")
            stopAll()
            return
        }
        needCheckIn = data.canCheckIn   //今天是否还需要打卡
        allowFieldWork = data.allowFieldWork
        workplaceList.clear()
        workplaceList.addAll(data.workPlaceList ?: ArrayList())
        // 检查是否在范围内
        calNearestWorkplace()
        if (needCheckIn) {
            // 打卡记录
            val checkItemList = data.checkItemList ?: ArrayList()
            // 是否最后一条已经打卡过的数据
            nextCheckInRecord = checkItemList.firstOrNull { element -> element.checkInResult == AttendanceV2RecordResult.PreCheckIn.value }
            needCheckIn = nextCheckInRecord != null
//            for ((index, item) in checkItemList.withIndex()) {
//                var isRecord = false
//                var recordTime = ""
//                if (item.checkInResult != AttendanceV2RecordResult.PreCheckIn.value) {
//                    isRecord = true
//                    var signTime = item.recordDate
//                    if (signTime.length > 16) {
//                        signTime = signTime.substring(11, 16);
//                    }
//                    var status = O2App.instance.getString(R.string.attendance_v2_check_in_completed)
//                    if (item.checkInResult != AttendanceV2RecordResult.Normal.value) {
//                        status = item.resultText()
//                    }
//                    recordTime = "$status $signTime"
//                }
//                item.recordTime = recordTime
//                item.isRecord = isRecord // 是否已经打卡
//                item.checkInTypeString =  if(item.checkInType == AttendanceV2RecordCheckInType.OnDuty.value) { AttendanceV2RecordCheckInType.OnDuty.label }else{ AttendanceV2RecordCheckInType.OffDuty.label }
//                var preDutyTime = item.preDutyTime
//                if (TextUtils.isEmpty(item.shiftId)) {
//                    preDutyTime = "" // 如果没有班次信息 表示 自由工时 或者 休息日 不显示 打卡时间
//                }
//                item.preDutyTime = preDutyTime
//                // 处理是否是最后一个已经打卡的记录
//                if (item.checkInResult != AttendanceV2RecordResult.PreCheckIn.value) {
//                    if (index == checkItemList.size - 1) { // 最后一条
//                        item.isLastRecord = true // 最后一条已经打卡的记录
//                    } else {
//                        val nextItem = checkItemList[index+1]
//                        if (nextItem.checkInResult == AttendanceV2RecordResult.PreCheckIn.value) {
//                            item.isLastRecord = true
//                        }
//                    }
//                }
//                checkItemList[index] = item
//            }
//            recordList.clear()
//            recordList.addAll(checkItemList)
        }

        // 如果不能打卡了 就结束 固定班制 才有打卡时间 才能进行极速打卡
        if (!needCheckIn && nextCheckInRecord?.groupCheckType != "1") {
            XLog.info("今天无需打卡， 全部停止。")
            stopAll()
        } else {
            tryCheckIn()
        }
    }


    /**
     * 检查是否进入打卡范围
     */
    private fun checkIsInWorkplace() {
        XLog.info("检查是否进入打卡范围.....${checkInPosition?.placeName}, ${myLocation?.addrStr}")
        if (checkInPosition != null && myLocation != null) {
            val workplacePosition = LatLng(checkInPosition!!.latitude.toDouble(), checkInPosition!!.longitude.toDouble())
            val position = LatLng(myLocation!!.latitude, myLocation!!.longitude)
            val distance = DistanceUtil.getDistance(position, workplacePosition)
            XLog.info("distance:$distance")
            isInCheckInPositionRange = distance < checkInPosition!!.errorRange
            if (isInCheckInPositionRange) {
                tryCheckIn()
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
                XLog.info("打卡的工作场所: ${checkInPosition?.placeName}")
                checkIsInWorkplace()
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