package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.main

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.jzxiang.pickerview.listener.OnDateSetListener
import kotlinx.android.synthetic.main.fragment_attendance_statistic.*
import kotlinx.android.synthetic.main.picker_activity_map_picker.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPViewPagerFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.group.Group
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.group.GroupRecyclerViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceDetailInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceStatisticGroupHeader
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleTextView
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

    }

    override fun lazyLoad() {
    }


}