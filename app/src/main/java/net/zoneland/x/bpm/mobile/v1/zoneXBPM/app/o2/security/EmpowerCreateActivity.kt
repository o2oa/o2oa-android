package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import com.wugang.activityresult.library.ActivityResult
import kotlinx.android.synthetic.main.activity_empower_create.ll_empower_create_application
import kotlinx.android.synthetic.main.activity_empower_create.ll_empower_create_completeTime
import kotlinx.android.synthetic.main.activity_empower_create.ll_empower_create_process
import kotlinx.android.synthetic.main.activity_empower_create.ll_empower_create_startTime
import kotlinx.android.synthetic.main.activity_empower_create.ll_empower_create_toPerson
import kotlinx.android.synthetic.main.activity_empower_create.radio_empower_create_type
import kotlinx.android.synthetic.main.activity_empower_create.radio_group_process_step_two_department
import kotlinx.android.synthetic.main.activity_empower_create.tv_empower_create_application
import kotlinx.android.synthetic.main.activity_empower_create.tv_empower_create_completeTime
import kotlinx.android.synthetic.main.activity_empower_create.tv_empower_create_process
import kotlinx.android.synthetic.main.activity_empower_create.tv_empower_create_startTime
import kotlinx.android.synthetic.main.activity_empower_create.tv_empower_create_toPerson
import kotlinx.android.synthetic.main.activity_empower_create.tv_start_process_step_two_identity
import kotlinx.android.synthetic.main.activity_empower_create.view_empower_create_application
import kotlinx.android.synthetic.main.activity_empower_create.view_empower_create_process
import kotlinx.android.synthetic.main.picker_activity_map_picker.list
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process.StartProcessActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.EmpowerData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.WoIdentityListItem
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ApplicationWithProcessData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessInfoData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.ContactPickerResult
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialogfragment.DateTimePickerFragment
import org.jetbrains.anko.dip

class EmpowerCreateActivity : BaseMVPActivity<EmpowerCreateContract.View, EmpowerCreateContract.Presenter>(), EmpowerCreateContract.View {


    private val typeList:ArrayList<String> = arrayListOf("all", "application", "process")
    private val identityList = ArrayList<WoIdentityListItem>()
    private var fromPerson: String = ""
    private var toPerson: String = ""
    private var type: String  = ""
    private var startTime:String = ""
    private var completeTime:String = ""
    private var application: ApplicationWithProcessData? = null
    private var process: ProcessInfoData? = null


    override fun layoutResId(): Int = R.layout.activity_empower_create

    override var mPresenter: EmpowerCreateContract.Presenter = EmpowerCreatePresenter()

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar(getString(R.string.title_activity_empower_create), setupBackButton = true)
        mPresenter.loadMyIdentity()
        loadUI()
        listenEvent()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_save -> {
                save()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun listenEvent() {
        ll_empower_create_toPerson.setOnClickListener {
            chooseToPerson()
        }
        ll_empower_create_startTime.setOnClickListener {
            chooseStartTime()
        }
        ll_empower_create_completeTime.setOnClickListener {
            chooseCompleteTime()
        }
        ll_empower_create_application.setOnClickListener {
            chooseApplication()
        }
        ll_empower_create_process.setOnClickListener {
            chooseProcess()
        }
    }

    private fun save() {
        val data = EmpowerData()
        if (TextUtils.isEmpty(toPerson)) {
            XToast.toastShort("请选择委托人！")
            return
        }
        if (TextUtils.isEmpty(startTime)) {
            XToast.toastShort("请选择开始时间！")
            return
        }
        if (TextUtils.isEmpty(completeTime)) {
            XToast.toastShort("请选择结束时间！")
            return
        }
        if (completeTime <= startTime) {
            XToast.toastShort("结束时间不能小于开始时间！")
            return
        }
        if (type == "application") {
            if (application == null) {
                XToast.toastShort("请选择应用！")
                return
            }
        }
        if (type == "process") {
            if (process == null) {
                XToast.toastShort("请选择流程！")
                return
            }
        }
        data.fromIdentity = fromPerson
        data.toIdentity = toPerson
        data.startTime = "$startTime:00"
        data.completedTime = "$completeTime:00"
        data.type = type
        if (type == "application") {
            data.application = application?.id ?: ""
            data.applicationName = application?.name ?: ""
            data.applicationAlias = application?.alias ?: ""
        }
        if (type == "process") {
            data.process = process?.id ?: ""
            data.processName = process?.name ?: ""
            data.processAlias = process?.alias ?: ""
            data.edition = process?.edition ?: ""
        }
        data.enable = true
        showLoadingDialog()
        mPresenter.createEmpower(data)
    }

    // 选择应用
    private fun chooseApplication() {
        val bundle = StartProcessActivity.startChooseApplication()
        ActivityResult.of(this)
            .className(StartProcessActivity::class.java)
            .params(bundle)
            .greenChannel().forResult { _, data ->
                val result = data?.getParcelableExtra<ApplicationWithProcessData>(StartProcessActivity.chooseApplicationResultKey)
                if (result != null) {
                    application = result
                    tv_empower_create_application.text = result.name
                }
            }
    }
    // 选择流程
    private fun chooseProcess() {
        val bundle = StartProcessActivity.startChooseProcess()
        ActivityResult.of(this)
            .className(StartProcessActivity::class.java)
            .params(bundle)
            .greenChannel().forResult { _, data ->
                val result = data?.getParcelableExtra<ProcessInfoData>(StartProcessActivity.chooseProcessResultKey)
                if (result != null) {
                    process = result
                    tv_empower_create_process.text = result.name
                }
            }
    }

    // 人员选择
    private fun chooseToPerson() {
        val bundle = ContactPickerActivity.startPickerBundle(arrayListOf(ContactPickerActivity.identityPicker), multiple = false)
        ActivityResult.of(this)
            .className(ContactPickerActivity::class.java)
            .params(bundle)
            .greenChannel().forResult { _, data ->
                val result = data?.getParcelableExtra<ContactPickerResult>(ContactPickerActivity.CONTACT_PICKED_RESULT)
                val list = result?.identities
                if (!list.isNullOrEmpty()) {
                    toPerson = list[0].distinguishedName
                    tv_empower_create_toPerson.text = list[0].name
                }
            }
    }

    // 开始时间选择
    private fun chooseStartTime() {
        val dialog = DateTimePickerFragment()
        val arg = Bundle()
        arg.putString(DateTimePickerFragment.PICKER_TYPE, DateTimePickerFragment.DATETIMEPICKER_TYPE)
        arg.putString(DateTimePickerFragment.DEFAULT_TIME,  DateHelper.nowByFormate("yyyy-MM-dd HH:ss"))
        dialog.arguments = arg
        dialog.setListener(object : DateTimePickerFragment.OnDateTimeSetListener {
            override fun onSet(time: String, pickerType: String) {
                startTime = time
                tv_empower_create_startTime.text = time
            }
        })
        dialog.show(supportFragmentManager, DateTimePickerFragment.DATETIMEPICKER_TYPE)
    }
    // 结束时间选择
    private fun chooseCompleteTime() {
        val dialog = DateTimePickerFragment()
        val arg = Bundle()
        arg.putString(DateTimePickerFragment.PICKER_TYPE, DateTimePickerFragment.DATETIMEPICKER_TYPE)
        arg.putString(DateTimePickerFragment.DEFAULT_TIME,  DateHelper.nowByFormate("yyyy-MM-dd HH:ss"))
        dialog.arguments = arg
        dialog.setListener(object : DateTimePickerFragment.OnDateTimeSetListener {
            override fun onSet(time: String, pickerType: String) {
                completeTime = time
                tv_empower_create_completeTime.text = time
            }
        })
        dialog.show(supportFragmentManager, DateTimePickerFragment.DATETIMEPICKER_TYPE)
    }
    private fun loadUI() {
        typeList.mapIndexed { index, it ->
            val radio = layoutInflater.inflate(R.layout.snippet_radio_button, null) as RadioButton
            radio.text = when(it) {
                "application"-> "应用"
                "process"-> "流程"
                else-> "全部"
            }
            if (index==0) {
                radio.isChecked = true
                type = it
            }
            radio.id = 100 + index//这里必须添加id 否则后面获取选中Radio的时候 group.getCheckedRadioButtonId() 拿不到id 会有空指针异常
            val layoutParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0,  dip(10f) ?: 10, 0, 0)
            radio_empower_create_type.addView(radio, layoutParams)
        }
        radio_empower_create_type.setOnCheckedChangeListener { _, checkedId ->
            val index = checkedId - 100
            type = typeList[index]
            changeType()
        }
    }

    private fun changeType() {
        when(type) {
            "application"-> {
                view_empower_create_application.visible()
                ll_empower_create_application.visible()
                view_empower_create_process.gone()
                ll_empower_create_process.gone()
            }
            "process"-> {
                view_empower_create_application.gone()
                ll_empower_create_application.gone()
                view_empower_create_process.visible()
                ll_empower_create_process.visible()
            }
            else-> {
                view_empower_create_application.gone()
                ll_empower_create_application.gone()
                view_empower_create_process.gone()
                ll_empower_create_process.gone()
            }
        }
    }

    override fun loadMyIdentity(identityList: List<WoIdentityListItem>) {
        if (identityList.isEmpty()) {
            XToast.toastShort("没有身份，无法创建授权！")
            finish()
            return
        }
        radio_group_process_step_two_department.removeAllViews()
        this.identityList.clear()
        //根据主身份排序
        val newList = identityList.sortedByDescending { id-> id.major }.toList()
        this.identityList.addAll(newList)
        if (this.identityList.size>0) {
            this.identityList.mapIndexed { index, it ->
                val radio = layoutInflater.inflate(R.layout.snippet_radio_button, null) as RadioButton
                radio.text = if (TextUtils.isEmpty(it.unitLevelName)) it.unitName else it.unitLevelName
                if (index==0) {
                    radio.isChecked = true
                    tv_start_process_step_two_identity.text = it.name + "("+it.unitName+")"
                    fromPerson = it.distinguishedName
                }
                radio.id = 100 + index//这里必须添加id 否则后面获取选中Radio的时候 group.getCheckedRadioButtonId() 拿不到id 会有空指针异常
                val layoutParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0,  dip(10f) ?: 10, 0, 0)
                radio_group_process_step_two_department.addView(radio, layoutParams)
            }
        }
        radio_group_process_step_two_department.setOnCheckedChangeListener { _, checkedId ->
            val index = checkedId - 100
            tv_start_process_step_two_identity.text = this.identityList[index].name + "("+ this.identityList[index].unitName+")"
            fromPerson = this.identityList[index].distinguishedName
        }
    }

    override fun createBack(result: Boolean, msg: String?) {
        hideLoadingDialog()
        if (result) {
            finish()
        } else {
            if (!TextUtils.isEmpty(msg)) {
                XToast.toastShort(msg!!)
            }
        }
    }
}