package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.start

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_process_start_dialog.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessDraftWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessInfoData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import org.jetbrains.anko.dip

/**
 * Created by fancyLou on 2023-04-12.
 * Copyright © 2023 o2android. All rights reserved.
 */
class StartProcessDialogFragment: DialogFragment(), StartProcessDialogContract.View {

    companion object {
        const val TAG = "StartProcessDialog"
        private const val processIdKey = "processIdKey"
        private const val processDataKey = "processDataKey"

        fun createStartProcessDialog(processId: String, jsonData: String?, dismiss: (result: Boolean, jobId: String?)->Unit ): StartProcessDialogFragment {
            val dialog = StartProcessDialogFragment()
            val bundle = Bundle()
            bundle.putString(processIdKey, processId)
            if (!TextUtils.isEmpty(jsonData)) {
                bundle.putString(processDataKey, jsonData)
            }
            dialog.arguments = bundle
            dialog.dismissListener = object : StartProcessDialogDismissListener {
                override fun onDismiss(result: Boolean, jobId: String?) {
                    dismiss(result, jobId)
                }
            }
            return dialog
        }
    }

    interface StartProcessDialogDismissListener {
        fun onDismiss(result: Boolean, jobId: String?)
    }

    var dismissListener: StartProcessDialogDismissListener? = null

    private val  mPresenter = StartProcessDialogPresenter()
    private var process: ProcessInfoData? = null
    private var processData: JsonElement? = null
    private val identityList = ArrayList<ProcessWOIdentityJson>()
    private var identity = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.customStyleDialogStyle) //NO_FRAME就是dialog无边框，0指的是默认系统Theme
        mPresenter.attachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window?.setGravity(Gravity.TOP)
        window?.setWindowAnimations(R.style.DialogEmptyAnimation)//取消过渡动画 , 使DialogSearch的出现更加平滑
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_process_start_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_start_process_dialog_close.setOnClickListener {
            closeSelf(false)
        }
        val processId = arguments?.getString(processIdKey)
        if (arguments == null || TextUtils.isEmpty(processId)) {
            XToast.toastShort(activity, getString(R.string.start_process_id_empty))
            closeSelf(false)
        } else {
            XLog.info("开始启动流程：$processId")
            // 开始查询流程
            val data = arguments?.getString(processDataKey)
            if (!TextUtils.isEmpty(data)) {
                XLog.info("有传入data：$data")
                processData = O2SDKManager.instance().gson.fromJson(data, JsonObject::class.java)
            }
            mPresenter.getProcess(processId!!)
        }
    }


    override fun getProcess(p: ProcessInfoData?) {
        if (p != null) {
            process = p
            tv_start_process_dialog_title.text =  "${getString(R.string.start_process_dialog_title)} - ${p.name}"
            mPresenter.loadCurrentPersonIdentityWithProcess(p.id)
        } else {
            XToast.toastShort(activity, getString(R.string.start_process_get_error))
            closeSelf(false)
        }
    }

    override fun loadCurrentPersonIdentity(list: List<ProcessWOIdentityJson>) {
        if (list.isEmpty()) { // 没有身份 无法启动流程
            XToast.toastShort(activity, getString(R.string.message_get_current_identity_fail))
            closeSelf(false)
        } else {

            if (list.size == 1) { // 一个身份直接下去
                identity = (list[0].distinguishedName)
                start()
            } else { // 多身份需要选择
                refreshForm(list)
            }
        }
    }

    override fun startProcessSuccess(task: TaskData) {
        closeSelf(true, task.job)
        val name = if (!TextUtils.isEmpty(process?.name)){ process?.name }else{ getString(R.string.create_manuscript) }
        activity?.go<TaskWebViewActivity>(TaskWebViewActivity.start(task.work, "", name))
    }

    override fun startProcessSuccessNoWork() {
        XToast.toastShort(activity, getString(R.string.message_start_process_success))
        closeSelf(true)
    }

    override fun startProcessFail(message: String) {
        if (!TextUtils.isEmpty(message)) {
            XToast.toastShort(activity, getString(R.string.message_start_process_fail_with_error, message))
        }
        closeSelf(false)
    }

    override fun startDraftSuccess(work: ProcessDraftWorkData) {
        closeSelf(true)
        activity?.go<TaskWebViewActivity>(TaskWebViewActivity.startDraft(work))
    }

    override fun startDraftFail(message: String) {
        XToast.toastShort(activity, message)
        closeSelf(false)
    }

    /**
     * 显示表单 选择身份
     */
    private fun refreshForm(list: List<ProcessWOIdentityJson>) {
        cpb_start_process_dialog_loading.gone()
        radio_group_process_step_two_department.removeAllViews()
        identityList.clear()
        //根据主身份排序
        val newList = list.sortedByDescending { id-> id.major }.toList()
        identityList.addAll(newList)
        if (identityList.size>0) {
            identityList.mapIndexed { index, it ->
                val radio = layoutInflater.inflate(R.layout.snippet_radio_button, null) as RadioButton
                radio.text = if (TextUtils.isEmpty(it.unitLevelName)) it.unitName else it.unitLevelName
                if (index==0) {
                    radio.isChecked = true
                    tv_start_process_step_two_identity.text = it.name + "("+it.unitName+")"
                    identity = it.distinguishedName
                }
                radio.id = 100 + index//这里必须添加id 否则后面获取选中Radio的时候 group.getCheckedRadioButtonId() 拿不到id 会有空指针异常
                val layoutParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, activity?.dip(10f) ?: 10, 0, 0)
                radio_group_process_step_two_department.addView(radio, layoutParams)
            }
        }
        radio_group_process_step_two_department.setOnCheckedChangeListener { _, checkedId ->
            val index = checkedId - 100
            tv_start_process_step_two_identity.text = identityList[index].name + "("+identityList[index].unitName+")"
            identity = identityList[index].distinguishedName

        }
        btn_start_process_step_two_positive.setOnClickListener {
            start()
        }
        ll_start_process_dialog_form.visible()
    }

    /**
     * 启动流程
     */
    private fun start() {
        if (process != null && !TextUtils.isEmpty(identity)) {
            if (process!!.defaultStartMode == O2.O2_Process_start_mode_draft) {
                startDraft(identity)
            }else {
                startProcess(identity)
            }
        } else {
            XLog.error("流程数据为空？ $identity")
            XToast.toastShort(activity, getString(R.string.message_get_current_identity_fail))
        }
    }


    private fun startProcess(identity: String) {
        //启动流程
        mPresenter.startProcess(identity, process!!.id, processData)
    }

    private fun startDraft(identity: String) {
        //启动草稿
        mPresenter.startDraft(identity, process!!.id, processData)
    }

    /**
     * @param result 是否启动成功
     * @param jobId 如果有当前用户的待办工作生成 就返回jobId
     */
    private fun closeSelf(result: Boolean, jobId: String? = null) {
        cpb_start_process_dialog_loading.gone()
        dismissListener?.onDismiss(result, jobId)
        dismissAllowingStateLoss() // 关闭
    }
}