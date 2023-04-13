package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.job

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_open_job_dialog.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.TaskCompletedListActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.start.StartProcessDialogFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.SwipeRefreshCommonRecyclerViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.service.PictureLoaderService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskCompleteData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.Work
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkOrWorkcompletedList
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.WorkVO
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleImageView

/**
 * Created by fancyLou on 2023-04-13.
 * Copyright © 2023 o2android. All rights reserved.
 */
class OpenJobDialogFragment : DialogFragment(), OpenJobContract.View {


    companion object {
        const val TAG = "OpenJobDialogFragment"
        private const val jobIdKey = "jobIdKey"

        fun openJobDialog(jobId: String, dismiss: () -> Unit): OpenJobDialogFragment {
            val dialog = OpenJobDialogFragment()
            val bundle = Bundle()
            bundle.putString(jobIdKey, jobId)
            dialog.arguments = bundle
            dialog.dismissListener = object : OpenJobDialogDismissListener {
                override fun onDismiss() {
                    dismiss()
                }
            }
            return dialog
        }
    }

    interface OpenJobDialogDismissListener {
        fun onDismiss()
    }

    private val mPresenter = OpenJobPresenter()
    var pictureLoaderService: PictureLoaderService? = null
    var dismissListener: OpenJobDialogDismissListener? = null

    private val mWorkList: ArrayList<Work> = ArrayList()

    private val adapter: SwipeRefreshCommonRecyclerViewAdapter<Work> by lazy {
        object : SwipeRefreshCommonRecyclerViewAdapter<Work>(activity, mWorkList, R.layout.item_todo_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, work: Work?) {
                val time = work?.startTime?.substring(0, 10) ?:""
                var activityName = work?.activityName ?: ""
                if (work?.isCompleted == true) {
                    activityName = getString(R.string.process_work_completed)
                }
                holder?.setText(R.id.todo_card_view_title_id, work?.title)
                    ?.setText(R.id.todo_card_view_content_id, "【${work?.processName}】")
                    ?.setText(R.id.todo_card_view_node_id, activityName)
                    ?.setText(R.id.todo_card_view_time_id, time)
                val icon = holder?.getView<CircleImageView>(R.id.todo_card_view_icon_id)
                val bitmap = BitmapFactory.decodeFile(O2CustomStyle.processDefaultImagePath(activity))
                icon?.setImageBitmap(bitmap)
                icon?.tag = work?.application
                loadApplicationIcon(holder?.convertView, work?.application)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            DialogFragment.STYLE_NO_FRAME,
            R.style.customStyleDialogStyle
        ) //NO_FRAME就是dialog无边框，0指的是默认系统Theme
        pictureLoaderService = PictureLoaderService(activity!!)
        mPresenter.attachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        pictureLoaderService?.close()
        mPresenter.detachView()
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        window?.setGravity(Gravity.TOP)
        window?.setWindowAnimations(R.style.DialogEmptyAnimation)//取消过渡动画 , 使DialogSearch的出现更加平滑
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_open_job_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_open_job_dialog_close.setOnClickListener {
            closeSelf()
        }
        rv_open_job_dialog_work_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rv_open_job_dialog_work_list.adapter = adapter
        adapter.setOnItemClickListener { _, position ->
            val work = mWorkList[position]
           openWork(work)
        }
        val jobId = arguments?.getString(jobIdKey)
        if (arguments == null || TextUtils.isEmpty(jobId)) {
            XToast.toastShort(activity, getString(R.string.start_job_id_empty))
            closeSelf()
        } else {
            mPresenter.getWorkByJobId(jobId!!)
        }
    }

    override fun workOrWorkCompletedResult(list: WorkOrWorkcompletedList?) {
        if (list != null) {
            val newList: ArrayList<Work> = ArrayList()
            for (work in list.workList) {
                newList.add(work)
            }
            for (workCompleted in list.workCompletedList) {
                workCompleted.isCompleted = true
                newList.add(workCompleted)
            }
            mWorkList.clear()
            mWorkList.addAll(newList)
            if (mWorkList.size == 1) {
                openWork(mWorkList[0])
            } else {
                rv_open_job_dialog_work_list.visible()
                cpb_open_job_dialog_loading.gone()
                adapter.notifyDataSetChanged()
            }
        }
    }

   private fun loadApplicationIcon(convertView: View?, appId: String?) {
        pictureLoaderService?.loadProcessAppIcon(convertView, appId)
    }

    private fun openWork(work : Work) {
        closeSelf()
        activity?.go<TaskWebViewActivity>(TaskWebViewActivity.start(work.id, "", work.title))
    }
    private fun closeSelf() {
        cpb_open_job_dialog_loading.gone()
        dismissAllowingStateLoss() // 关闭
    }
}