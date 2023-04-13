package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.main

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import com.xiaomi.push.go
import kotlinx.android.synthetic.main.fragment_attendance_statistic_v2.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.appeal.AttendanceV2AppealActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPViewPagerFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2StatisticResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import java.util.*

/**
 * Created by fancyLou on 28/05/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


class AttendanceStatisticV2Fragment : BaseMVPViewPagerFragment<AttendanceStatisticV2Contract.View, AttendanceStatisticV2Contract.Presenter>(),
    AttendanceStatisticV2Contract.View {
    override var mPresenter: AttendanceStatisticV2Contract.Presenter = AttendanceStatisticV2Presenter()


    override fun layoutResId(): Int = R.layout.fragment_attendance_statistic_v2



    override fun initUI() {
        val cal = Calendar.getInstance()
        tv_att_v2_stat_year.text = "${cal.get(Calendar.YEAR)}"
        tv_att_v2_stat_month.text = "${cal.get(Calendar.MONTH) + 1}月"
        val content = SpannableString(getString(R.string.attendance_v2_stat_to_appeal_label))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        tv_att_v2_stat_to_appeal.text = content
        tv_att_v2_stat_to_appeal.setOnClickListener {
            activity?.go<AttendanceV2AppealActivity>()
        }
    }

    override fun lazyLoad() {
        // 当前月份
        val cal = Calendar.getInstance()
        val first = DateHelper.getMonthFirstDay(cal).time
        val last = DateHelper.getMonthLastDay(cal).time
        mPresenter.loadMyStatistic(DateHelper.getDateTime("yyyy-MM-dd", first), DateHelper.getDateTime("yyyy-MM-dd", last))
    }

    override fun myStatistic(my: AttendanceV2StatisticResponse) {
        if (my.workTimeDuration > 0) {
            ll_att_v2_stat_averageWorkTimeDuration.alpha = 1.0f
        } else {
            ll_att_v2_stat_averageWorkTimeDuration.alpha = 0.5f
        }
        setAlpha(my.attendance, ll_att_v2_stat_attendance)
        setAlpha(my.rest, ll_att_v2_stat_rest)
        setAlpha(my.leaveDays, tv_att_v2_stat_leaveDays)
        setAlpha(my.absenteeismDays, ll_att_v2_stat_absenteeismDays)
        setAlpha(my.lateTimes, ll_att_v2_stat_lateTimes)
        setAlpha(my.leaveEarlierTimes, ll_att_v2_stat_leaveEarlierTimes)
        setAlpha(my.absenceTimes, ll_att_v2_stat_absenceTimes)
        setAlpha(my.fieldWorkTimes, ll_att_v2_stat_fieldWork)

        tv_att_v2_stat_averageWorkTimeDuration.text = my.averageWorkTimeDuration
        tv_att_v2_stat_attendance.text = "${my.attendance}"
        tv_att_v2_stat_rest.text = "${my.rest}"
        tv_att_v2_stat_leaveDays.text = "${my.leaveDays}"
        tv_att_v2_stat_absenteeismDays.text = "${my.absenteeismDays}"
        tv_att_v2_stat_lateTimes.text = "${my.lateTimes}"
        tv_att_v2_stat_leaveEarlierTimes.text = "${my.leaveEarlierTimes}"
        tv_att_v2_stat_absenceTimes.text = "${my.absenceTimes}"
        tv_att_v2_stat_fieldWork.text = "${my.fieldWorkTimes}"

    }
    private fun setAlpha(bigger:Int, view: View) {
        if (bigger > 0) {
            view.alpha = 1.0f
        } else {
            view.alpha = 0.5f
        }
    }

}