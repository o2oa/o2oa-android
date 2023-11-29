package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.calendar

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import kotlinx.android.synthetic.main.fragment_calendar_month.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.calendar.vm.MonthCalendarViewModel
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.calendar.CalendarEventInfoData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.CalendarEventFilterVO
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.decorator.EventDecorator
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.decorator.SelectorDecorator
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.decorator.TodayDecorator
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.*

/**
 * Created by fancyLou on 19/06/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


class MonthCalendarViewModelFragment : CalendarBaseFragment(), OnDateSelectedListener, OnMonthChangedListener {

    private val selectorDecorator: SelectorDecorator by lazy {
        SelectorDecorator(
            activity
        )
    }
    private val list = ArrayList<CalendarEventInfoData>()
    private val adapter: CommonRecycleViewAdapter<CalendarEventInfoData> by lazy {
        object : CommonRecycleViewAdapter<CalendarEventInfoData>(activity, list, R.layout.item_fragment_calendar_month_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: CalendarEventInfoData?) {
                val allDayTv = holder?.getView<TextView>(R.id.tv_item_fragment_calendar_month_list_all_day)
                val timeBlock = holder?.getView<LinearLayout>(R.id.ll_item_fragment_calendar_month_list_time_block)
                val colorCircle = holder?.getView<CardView>(R.id.cv_item_fragment_calendar_month_list_color)
                val startTime = if (TextUtils.isEmpty(t?.startTimeStr)) "" else t?.startTimeStr!!.substring(11, 16)
                val endTime = if (TextUtils.isEmpty(t?.endTimeStr)) "" else t?.endTimeStr!!.substring(11, 16)
                holder?.setText(R.id.tv_item_fragment_calendar_month_list_name, t?.title ?: "")
                        ?.setText(R.id.tv_item_fragment_calendar_month_list_start_time, startTime)
                        ?.setText(R.id.tv_item_fragment_calendar_month_list_end_time, endTime)
                if (t?.isAllDayEvent == true) {
                    allDayTv?.visible()
                    timeBlock?.gone()
                }else {
                    allDayTv?.gone()
                    timeBlock?.visible()
                }
                @SuppressLint("Range")
                val color = try {
                    Color.parseColor(t?.color)
                } catch (e: Exception) {
                    XLog.error("transform color error ", e)
                    Color.RED
                }
                colorCircle?.setCardBackgroundColor(color)
            }
        }
    }
    private val monthViewModel: MonthCalendarViewModel by lazy { ViewModelProviders.of(this).get(MonthCalendarViewModel::class.java) }

    private val DAY_KEY = "DAY_KEY"
    private lateinit var selectDay:Calendar
    private val MY_FILTER_KEY = "MY_FILTER_KEY"
    private lateinit var myFilter: CalendarEventFilterVO

    override fun layoutResId(): Int = R.layout.fragment_calendar_month

    override fun bindViewModel() {
        monthViewModel.currentFilter().observe(this, androidx.lifecycle.Observer { filter->
            updateTitle(filter?.start)
        })
        monthViewModel.getEventMap().observe(this, androidx.lifecycle.Observer { map->
            val meetingDays = ArrayList<CalendarDay>()
            if (map!=null) {
                map.forEach { (day, events) ->
                    if (events.isNotEmpty()) {
                        val date = DateHelper.convertStringToDate("yyyy-MM-dd HH:mm:ss", day)
                        val localDate = CalendarDay.from(Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate())
                        meetingDays.add(localDate)
                    }
                }
                val selectDay = monthViewModel.getSelectDay().value
                if (selectDay!=null) {
                    val selectDayStr = DateHelper.getDate(selectDay.time)
                    val eventList = map[selectDayStr]
                    if (!eventList.isNullOrEmpty()) {
                        list.clear()
                        if (!eventList.isEmpty()) {
                            eventList.forEach {
                                list.add(it)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            //clear decorator
            mcv_fragment_calendar_month.removeDecorators()
            mcv_fragment_calendar_month.addDecorators(
                TodayDecorator(
                    activity
                ), selectorDecorator)
            //addDecorator
            mcv_fragment_calendar_month.addDecorator(
                EventDecorator(
                    FancySkinManager.instance().getColor(activity!!, R.color.z_color_primary),
                    meetingDays
                )
            )

        })
        monthViewModel.getSelectDay().observe(this, androidx.lifecycle.Observer { day->
            if (day!=null) {
                val dayStr = DateHelper.getDate(day.time)
                tv_fragment_calendar_month_current_day.text = dayStr
            }
        })
        monthViewModel.getDayEvenList().observe(this, androidx.lifecycle.Observer { eventList->
            list.clear()
            if (eventList!=null && !eventList.isEmpty()) {
                eventList.forEach {
                    list.add(it)
                }
            }
            adapter.notifyDataSetChanged()
        })

    }


    override fun initView() {
        val localNow = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        val date = DateTimeUtils.toDate(localNow.atStartOfDay(zoneId).toInstant())
        val now = Calendar.getInstance()
        now.time = date
        mcv_fragment_calendar_month.isDynamicHeightEnabled = true
        mcv_fragment_calendar_month.addDecorators(
            TodayDecorator(
                activity
            ), selectorDecorator)
        mcv_fragment_calendar_month.topbarVisible = false
        val selected = CalendarDay.from(localNow)
        mcv_fragment_calendar_month.selectedDate = selected
        mcv_fragment_calendar_month.setOnMonthChangedListener(this)
        mcv_fragment_calendar_month.setOnDateChangedListener(this)

        rv_fragment_calendar_month_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rv_fragment_calendar_month_list.adapter = adapter
        adapter.setOnItemClickListener { _, position ->
            val event = list[position]
            if (activity is CalendarMainActivity) {
                (activity as CalendarMainActivity).editEvent(event)
            }
        }
        if (!this::selectDay.isInitialized) {
            selectDay = now
        }
        if (!this::myFilter.isInitialized) {
            val day0= DateHelper.getMonthFirstDay(now)
            val day1 = DateHelper.getMonthLastDay(now)
            myFilter = CalendarEventFilterVO(
                    day0, day1, initCalendarIds()
            )
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val day = savedInstanceState?.getSerializable(DAY_KEY)
        if (day != null) {
            selectDay = day as Calendar
        }
        val save = savedInstanceState?.getSerializable(MY_FILTER_KEY)
        if (save!=null){
            myFilter = save as CalendarEventFilterVO
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(DAY_KEY, selectDay)
        outState.putSerializable(MY_FILTER_KEY, myFilter)
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
        showDayEvents()
    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        val zoneId = ZoneId.systemDefault()
        val d = DateTimeUtils.toDate(date.date.atStartOfDay(zoneId).toInstant())
        val newDay = Calendar.getInstance()
        newDay.time = d
        selectDay = newDay
        showDayEvents()
        selectorDecorator.setDate(date)
        widget.invalidateDecorators()
    }


    override fun onMonthChanged(widget: MaterialCalendarView?, date: CalendarDay?) {
        if (date!=null) {
            val zoneId = ZoneId.systemDefault()
            val d = DateTimeUtils.toDate(date.date.atStartOfDay(zoneId).toInstant())
            val newDay = Calendar.getInstance()
            newDay.time = d
            val day0= DateHelper.getMonthFirstDay(newDay)
            val day1 = DateHelper.getMonthLastDay(newDay)
            myFilter.start = day0
            myFilter.end = day1
        }
        loadEvents()
    }

    override fun setCalendarFilter(calendarIds: List<String>) {
        myFilter.calendarIds = calendarIds
        loadEvents()
    }


    override fun jump2Today() {
        val localNow = LocalDate.now()
        val calDate = CalendarDay.from(localNow)
        mcv_fragment_calendar_month.currentDate = calDate
        // 下面是选中今天
        val zoneId = ZoneId.systemDefault()
        val date = DateTimeUtils.toDate(localNow.atStartOfDay(zoneId).toInstant())
        val cal = Calendar.getInstance()
        cal.time = date
        selectDay = cal
        mcv_fragment_calendar_month.selectedDate = calDate
        showDayEvents()
        selectorDecorator.setDate(calDate)
        mcv_fragment_calendar_month.invalidateDecorators()
    }

    override fun updateTitle(cal: Calendar?) {
        if (cal != null) {
            val month = DateHelper.getDateTime("yyyy年M月", cal.time)
            if (activity is CalendarMainActivity && isSelfShow) {
                (activity as CalendarMainActivity).updateActivityTitle(month)
            }
        } else {
            XLog.error("月份数据为空！！！！！！")
        }
    }

    override fun initCalendarIds(): List<String> {
        return if (activity is CalendarMainActivity) {
            (activity as CalendarMainActivity).getCalendarIds()
        }else {
            ArrayList()
        }
    }

    private fun loadEvents(){
        monthViewModel.filter(myFilter)
    }
    private fun showDayEvents(){
        monthViewModel.selectDay(selectDay)
    }




}