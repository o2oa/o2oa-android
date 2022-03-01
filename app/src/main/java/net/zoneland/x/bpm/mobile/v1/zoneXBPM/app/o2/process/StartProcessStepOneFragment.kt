package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_start_process_step_one.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ApplicationData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ApplicationWithProcessData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessDraftWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessInfoData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.goThenKill
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleImageView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.DividerItemDecoration


class StartProcessStepOneFragment : BaseMVPFragment<StartProcessStepOneContract.View, StartProcessStepOneContract.Presenter>(), StartProcessStepOneContract.View {

    override var mPresenter: StartProcessStepOneContract.Presenter = StartProcessStepOnePresenter()

    override fun layoutResId(): Int = R.layout.fragment_start_process_step_one

    var currentChooseAppId = ""
    val appList = ArrayList<ApplicationWithProcessData>()
    val processList = ArrayList<ProcessInfoData>()
    var clickProcess : ProcessInfoData? = null
    val appAdapter: CommonRecycleViewAdapter<ApplicationWithProcessData> by lazy {
        object : CommonRecycleViewAdapter<ApplicationWithProcessData>(activity, appList, R.layout.item_start_process_application) {
            override fun convert(holder: CommonRecyclerViewHolder, t: ApplicationWithProcessData) {
                holder.setText(R.id.tv_item_start_process_application_name, t.name)
                val back = holder.getView<LinearLayout>(R.id.linear_item_start_process_application_content)
                if (t.id == currentChooseAppId) {
                    back.setBackgroundColor(Color.WHITE)
                } else {
                    back.setBackgroundResource(R.color.z_color_background_normal)
                }
                val icon = holder.getView<CircleImageView>(R.id.image_item_start_process_application_icon)
                val bitmap = BitmapFactory.decodeFile(O2CustomStyle.processDefaultImagePath(activity))
                icon?.setImageBitmap(bitmap)
                icon.tag = t.id
                (activity as StartProcessActivity).loadProcessApplicationIcon(icon, t.id)
            }
        }
    }
    val processAdapter: CommonRecycleViewAdapter<ProcessInfoData> by lazy {
        object : CommonRecycleViewAdapter<ProcessInfoData>(activity, processList, R.layout.item_start_process_application_process) {
            override fun convert(holder: CommonRecyclerViewHolder, t: ProcessInfoData) {
                holder.setText(R.id.tv_item_start_process_application_process_name, t.name)
            }
        }
    }
    val itemDecoration: DividerItemDecoration by lazy { DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST) }

    override fun initUI() {

        (activity as StartProcessActivity).setToolBarTitle(getString(R.string.title_activity_start_process))
        recycler_start_process_application_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recycler_start_process_application_list.addItemDecoration(itemDecoration)
        appAdapter.setOnItemClickListener { _, position ->
            currentChooseAppId = appList[position].id
            mPresenter.loadProcessListByAppId(currentChooseAppId)
            appAdapter.notifyDataSetChanged()
        }
        recycler_start_process_application_list.adapter = appAdapter
        recycler_start_process_application_process_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        processAdapter.setOnItemClickListener { _, position ->
            onProcessItemClick(processList[position])
        }
        recycler_start_process_application_process_list.adapter = processAdapter

        showLoadingDialog()
        mPresenter.loadApplicationListWithProcess()
    }


    override fun loadApplicationListWithProcess(list: List<ApplicationWithProcessData>) {
        hideLoadingDialog()
        appList.clear()
        appList.addAll(list)
        if (appList.size>0) {
            currentChooseAppId = appList[0].id
            setProcessList(appList[0].processList)
            linear_start_process_content?.visible()
            tv_start_process_empty?.gone()
        }
        appAdapter.notifyDataSetChanged()
    }

    private fun setProcessList(list: List<ProcessInfoData>) {
        processList.clear()
        processList.addAll(list)
        processAdapter.notifyDataSetChanged()
    }

//    override fun loadApplicationList(list: List<ApplicationData>) {
//        appList.clear()
//        appList.addAll(list)
//        if (appList.size>0) {
//            currentChooseAppId = appList[0].id
//            mPresenter.loadProcessListByAppId(currentChooseAppId)
//            linear_start_process_content?.visible()
//            tv_start_process_empty?.gone()
//        }
//        appAdapter.notifyDataSetChanged()
//    }

    override fun loadApplicationListFail() {
        hideLoadingDialog()
        XToast.toastShort(activity, getString(R.string.message_get_application_fail))
        appList.clear()
        linear_start_process_content?.gone()
        tv_start_process_empty?.visible()
    }

    override fun loadProcessList(list: List<ProcessInfoData>) {
        processList.clear()
        processList.addAll(list)
        processAdapter.notifyDataSetChanged()
    }

    override fun loadProcessListFail() {
        XToast.toastShort(activity, getString(R.string.message_get_process_fail))
        processList.clear()
        processAdapter.notifyDataSetChanged()
    }

    override fun loadCurrentPersonIdentity(list: List<ProcessWOIdentityJson>) {
        if (list.isNotEmpty() ) {
            if (list.size == 1) {
                //是否走草稿
                if (clickProcess != null && clickProcess?.defaultStartMode == O2.O2_Process_start_mode_draft) {
                    startDraft(list[0].distinguishedName)
                }else {
                    startProcess(list[0].distinguishedName)
                }
            }else {
                goToStepTwo()
            }
        }else {
            hideLoadingDialog()
            XToast.toastShort(activity, getString(R.string.message_get_current_identity_fail))
        }
    }

    override fun loadCurrentPersonIdentityFail() {
        hideLoadingDialog()
        XToast.toastShort(activity, getString(R.string.message_get_current_identity_fail))
    }

    override fun startProcessSuccess(workId: String) {
        hideLoadingDialog()
        val name = if (clickProcess != null && !TextUtils.isEmpty(clickProcess?.name)){ clickProcess?.name?: getString(R.string.create_manuscript)}else{getString(R.string.create_manuscript)}
        (activity as StartProcessActivity).goThenKill<TaskWebViewActivity>(TaskWebViewActivity.start(workId, "", name))
    }

    override fun startProcessSuccessNoWork() {
        hideLoadingDialog()
        XToast.toastShort(activity, getString(R.string.message_start_process_success))
        (activity as StartProcessActivity).finish()
    }

    override fun startProcessFail(message: String) {
        hideLoadingDialog()
        XToast.toastShort(activity, message)
    }

    override fun startDraftSuccess(work: ProcessDraftWorkData) {
        hideLoadingDialog()
        (activity as StartProcessActivity).goThenKill<TaskWebViewActivity>(TaskWebViewActivity.startDraft(work))
    }

    override fun startDraftFail(message: String) {
        hideLoadingDialog()
        XToast.toastShort(activity, message)
    }

    private fun startProcess(identity: String) {
        //启动流程
        mPresenter.startProcess(identity, clickProcess!!.id)
    }

    private fun startDraft(identity: String) {
        //启动草稿
        mPresenter.startDraft(identity, clickProcess!!.id)
    }

    private fun onProcessItemClick(processInfoData: ProcessInfoData) {
        clickProcess = processInfoData
        showLoadingDialog()
        mPresenter.loadCurrentPersonIdentityWithProcess(processInfoData.id)
    }

    private fun goToStepTwo() {
        hideLoadingDialog()
        if (clickProcess != null) {
            val stepTwo = StartProcessStepTwoFragment.newInstance(clickProcess!!.id, clickProcess!!.name, clickProcess?.defaultStartMode ?: "")
            (activity as StartProcessActivity).addFragment(stepTwo)
        }
    }


}
