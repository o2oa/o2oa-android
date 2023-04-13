package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.attendance.appeal

import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_attendance_v2_appeal.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.start.StartProcessDialogFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2AppealInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2AppealInfoToProcessData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2AppealStatus
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.attendance.AttendanceV2Config
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.MiscUtilK
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2oaColorScheme
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.DividerItemDecoration

class AttendanceV2AppealActivity : BaseMVPActivity<AttendanceV2AppealContract.View, AttendanceV2AppealContract.Presenter>(), AttendanceV2AppealContract.View {

    private var page = 1 // 当前页码
    private var isLoadingMore = false // 加载更多
    private var isRefresh = false // 刷新
    private var hasMore = true // 更多数据
    private var appealList = ArrayList<AttendanceV2AppealInfo>()

    private var appealEnable = false // 是否允许申请
    private var processId = "" // 申请的流程id

    override var mPresenter: AttendanceV2AppealContract.Presenter = AttendanceV2AppealPresenter()

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar("考勤异常数据", setupBackButton = true)
        srl_att_v2_appeal_list.o2oaColorScheme()
        srl_att_v2_appeal_list.setOnRefreshListener {
            if (!isRefresh && !isLoadingMore) {
                XLog.debug("下拉刷新。。。。")
                isRefresh = true
                page = 1
                loadAppealList()
            }
        }
        MiscUtilK.swipeRefreshLayoutRun(srl_att_v2_appeal_list, this)
        //
        rvInit()

        mPresenter.config()
        loadAppealList()
    }

    override fun layoutResId(): Int = R.layout.activity_attendance_v2_appeal

    private val adapter: CommonRecycleViewAdapter<AttendanceV2AppealInfo> by lazy {
        object : CommonRecycleViewAdapter<AttendanceV2AppealInfo>(this, appealList, R.layout.item_attendance_v2_appeal_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: AttendanceV2AppealInfo?) {
                val time = t?.recordDate ?: ""
                val duty = t?.record?.checkInTypeText() ?: ""
                val showText = if (TextUtils.isEmpty(duty)) {
                    time
                } else {
                    "$time ($duty)"
                }
                var result = t?.record?.resultText() ?: ""
                if (t?.record?.fieldWork == true) {
                    result = getString(R.string.attendance_v2_fieldWork)
                }
                holder?.setText(R.id.tv_item_att_v2_appeal_recordDateString, showText)
                    ?.setText(R.id.tv_item_att_v2_appeal_result, result)
                    ?.setText(R.id.tv_item_att_v2_appeal_status, t?.statsText() ?: "")
                val processBtn = holder?.getView<LinearLayout>(R.id.ll_item_att_v2_appeal_process)
                val processTv = holder?.getView<TextView>(R.id.tv_item_att_v2_appeal_process)
                processBtn?.gone()
                if (t?.status == AttendanceV2AppealStatus.StatusInit.value) {
                    processBtn?.visible()
                    val content = SpannableString(getString(R.string.attendance_v2_appeal_process))
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    processTv?.text = content
                } else if (!TextUtils.isEmpty(t?.jobId)) {
                    processBtn?.visible()
                    val content = SpannableString(getString(R.string.attendance_v2_appeal_process_view))
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    processTv?.text = content
                }
            }
        }
    }

    private fun rvInit() {
        rv_att_v2_appeal_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_att_v2_appeal_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 滚动到顶部才启用下拉刷新功能
                val topRowVerticalPosition = rv_att_v2_appeal_list?.getChildAt(0)?.top ?: 0
                srl_att_v2_appeal_list.isEnabled = topRowVerticalPosition >= 0
                // 上拉加载
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition = lm.findLastVisibleItemPosition()
                if (lastPosition == lm.itemCount - 1 && !isRefresh && !isLoadingMore && hasMore) {
                    XLog.debug("加载更多。。。。")
                    refreshDataList()
                }
            }
        })
        rv_att_v2_appeal_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
        rv_att_v2_appeal_list.adapter = adapter
        adapter.setOnItemClickListener { _, position ->
            val item  = appealList[position]
            if (item.status == AttendanceV2AppealStatus.StatusInit.value && appealEnable) {
                startAppeal(item)
            } else if (!TextUtils.isEmpty(item.jobId)) {
                 openJob(item.jobId)
            }
        }
    }

    override fun appealList(list: List<AttendanceV2AppealInfo>) {
        XLog.debug("appealList.........$page")
        srl_att_v2_appeal_list.isRefreshing = false
        if (page == 1) {
            appealList.clear()
        }
        appealList.addAll(list)
        if (list.size < O2.DEFAULT_PAGE_NUMBER) { // 没有更多数据
            hasMore = false
        }
        isRefresh = false
        isLoadingMore = false
        adapter.notifyDataSetChanged()
    }

    override fun config(config: AttendanceV2Config?) {
        if (config != null && config.appealEnable && !TextUtils.isEmpty(config.processId)) {
            appealEnable = true
            processId = config.processId
        }
        adapter.notifyDataSetChanged()
    }

    override fun checkAppealResult(value: Boolean, appealInfo: AttendanceV2AppealInfo) {
        hideLoadingDialog()
        if (value) {
            XLog.debug("检查成功，开始启动流程")
            val data = AttendanceV2AppealInfoToProcessData(appealInfo.id, appealInfo.record)
            val dialog = StartProcessDialogFragment.createStartProcessDialog(processId, jsonData = O2SDKManager.instance().gson.toJson(data), dismiss = { result, jobId ->
                XLog.debug("dialog 关闭了。。。。result: $result job: $jobId")
                if (result) {
                    mPresenter.appealStartedProcess(appealInfo.id)
                }
            })
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, StartProcessDialogFragment.TAG)
        }
    }

    override fun appealStartedProcess(value: Boolean) {
        XLog.info("启动流程后更新状态的结果： $value")
        refreshDataList()
    }


    private fun refreshDataList() {
        isRefresh = true
        page = 1
        loadAppealList()
    }

    private fun loadAppealList() {
        XLog.debug("loadAppealList.........$page")
        mPresenter.myAppealListByPage(page)
    }

    /**
     * 开始申诉
     */
    private fun startAppeal(item: AttendanceV2AppealInfo) {
        if (!appealEnable || TextUtils.isEmpty(processId)) {
            XToast.toastShort(this, getString(R.string.attendance_v2_can_not_appeal))
            return
        }
        showLoadingDialog()
        mPresenter.checkAppeal(item)
    }

    private fun openJob(jobId: String) {

    }

}