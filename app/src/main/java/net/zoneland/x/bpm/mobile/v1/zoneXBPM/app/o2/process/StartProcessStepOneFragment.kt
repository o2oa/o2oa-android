package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_start_process_step_one.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity.Companion.startDraft
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.*
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
    val appList = ArrayList<ApplicationOrCategory>()
    val processList = ArrayList<ProcessInfoData>()
    var clickProcess : ProcessInfoData? = null
    val appAdapter: CommonRecycleViewAdapter<ApplicationOrCategory> by lazy {
        object : CommonRecycleViewAdapter<ApplicationOrCategory>(activity, appList, R.layout.item_start_process_application) {
            override fun convert(holder: CommonRecyclerViewHolder, t: ApplicationOrCategory) {
                val categoryNameTv = holder.getView<TextView>(R.id.tv_item_start_app_category_name)
                val appNameTv = holder.getView<TextView>(R.id.tv_item_start_process_application_name)
                val icon = holder.getView<CircleImageView>(R.id.image_item_start_process_application_icon)
                val back = holder.getView<RelativeLayout>(R.id.linear_item_start_process_application_content)
                back.setBackgroundResource(R.color.z_color_background_normal)
                if (!TextUtils.isEmpty(t.categoryName)) { // 分类名称
                    categoryNameTv.visible()
                    categoryNameTv.text = t.categoryName!!
                    appNameTv.gone()
                    icon.gone()
                } else {
                    categoryNameTv.gone()
                    appNameTv.visible()
                    icon.visible()
                    appNameTv.text = t.app?.name ?: ""
                    val bitmap = BitmapFactory.decodeFile(O2CustomStyle.processDefaultImagePath(activity))
                    icon?.setImageBitmap(bitmap)
                    icon.tag = t.app?.id
                    (activity as StartProcessActivity).loadProcessApplicationIcon(icon, t.app?.id ?: "1" )
                    if (t.app?.id == currentChooseAppId) {
                        back.setBackgroundColor(Color.WHITE)
                    }
                }
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
        when((activity as StartProcessActivity).chooseMode) {
            "1" -> (activity as StartProcessActivity).setToolBarTitle(getString(R.string.title_activity_start_process))
            "2" -> (activity as StartProcessActivity).setToolBarTitle(getString(R.string.title_activity_choose_application))
            "3" -> (activity as StartProcessActivity).setToolBarTitle(getString(R.string.title_activity_start_process))
        }
        //(activity as StartProcessActivity).setToolBarTitle(getString(R.string.title_activity_start_process))

        // 应用
        recycler_start_process_application_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recycler_start_process_application_list.addItemDecoration(itemDecoration)
        appAdapter.setOnItemClickListener { _, position ->
            if (appList[position].app != null) {
                if ((activity as StartProcessActivity).chooseMode == "2") {
                    (activity as StartProcessActivity).chooseModeResult(appList[position].app, null)
                } else {
                    currentChooseAppId = appList[position].app!!.id
                    setProcessList(appList[position].app!!.processList)
//                    mPresenter.loadProcessListByAppId(currentChooseAppId)
                    appAdapter.notifyDataSetChanged()
//                    appAdapter.notifyItemChanged(position)
                }
            }
        }
        recycler_start_process_application_list.adapter = appAdapter

        // 流程
        recycler_start_process_application_process_list.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recycler_start_process_application_process_list.adapter = processAdapter
        processAdapter.setOnItemClickListener { _, position ->
            if ((activity as StartProcessActivity).chooseMode == "3") {
                (activity as StartProcessActivity).chooseModeResult(null, processList[position])
            } else {
                onProcessItemClick(processList[position])
            }
        }
        if ((activity as StartProcessActivity).chooseMode == "2") { // 应用选择 隐藏流程列表
            recycler_start_process_application_process_list.gone()
        }

        showLoadingDialog()
        mPresenter.loadApplicationListWithProcess()
    }


    override fun loadApplicationListWithProcess(list: List<ApplicationWithProcessData>) {
        hideLoadingDialog()
        //  进行分类处理 根据分类展现应用列表
        val appOrCategoryList = ArrayList<ApplicationOrCategory>()
        val map = HashMap<String, ArrayList<ApplicationWithProcessData>>()
        for (app in list) {
            var categoryName = app.applicationCategory ?: ""
            if (TextUtils.isEmpty(categoryName)) {
               categoryName = "未分类"
            }
            if (map[categoryName] == null) {
                map[categoryName] = ArrayList()
            }
            map[categoryName]!!.add(app)
        }
        for ((key, value) in map.entries) {
            val aoc = ApplicationOrCategory(key, null)
            appOrCategoryList.add(aoc)
            for (a in value) {
                val aoc1 = ApplicationOrCategory(null, a)
                appOrCategoryList.add(aoc1)
            }
        }
        appList.clear()
        appList.addAll(appOrCategoryList)
        if (appList.size > 1) {
            currentChooseAppId = appList[1].app?.id ?: ""
            setProcessList(appList[1].app?.processList ?: ArrayList())
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
