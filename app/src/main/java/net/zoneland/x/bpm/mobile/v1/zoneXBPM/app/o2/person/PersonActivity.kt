package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.person


import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_person_info.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.O2ChatActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.OrganizationPermissionManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.GenderTypeEnums
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.person.PersonAttributeJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.person.PersonJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.AndroidUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.makeCallDial
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderOptions
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import org.jetbrains.anko.dip
import org.jetbrains.anko.email
import org.jetbrains.anko.sendSMS
import org.jetbrains.anko.sp


class PersonActivity : BaseMVPActivity<PersonContract.View, PersonContract.Presenter>(), PersonContract.View, View.OnClickListener {
    override var mPresenter: PersonContract.Presenter = PersonPresenter()
    override fun layoutResId(): Int = R.layout.activity_person_info

    companion object {
        val PERSON_NAME_KEY = "name"

        fun startBundleData(person: String): Bundle {
            val bundle = Bundle()
            bundle.putString(PERSON_NAME_KEY, person)
            return bundle
        }
    }

    var loadedPersonId = ""//用户的id字段
    var loadedPersonDN = ""//用户的id字段
    var personId = ""
    var genderName = ""
    var hasCollection = false
//    val mobileMenuItemList: ArrayList<String> by lazy {  arrayListOf(getString(R.string.call), getString(R.string.sms), getString(R.string.copy)) }
//    val mobileClickMenu: CommonMenuPopupWindow by lazy { CommonMenuPopupWindow(mobileMenuItemList, this) }
//    val emailMenuItemList: ArrayList<String> by lazy {  arrayListOf(getString(R.string.send_email), getString(R.string.copy)) }
//    val emailClickMenu: CommonMenuPopupWindow by lazy { CommonMenuPopupWindow(emailMenuItemList, this) }
    private val backgroundList = arrayListOf(R.mipmap.pic_person_bg_1, R.mipmap.pic_person_bg_2, R.mipmap.pic_person_bg_3, R.mipmap.pic_person_bg_4, R.mipmap.pic_person_bg_5, R.mipmap.pic_person_bg_6)
//    private var canTalkTo = false
    private var attributeList: ArrayList<PersonAttributeJson> = ArrayList()

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        //透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0 全透明实现
            //getWindow.setStatusBarColor(Color.TRANSPARENT)
            val window: Window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4 全透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        personId = intent.extras?.getString(PERSON_NAME_KEY, "")?:""
        if (TextUtils.isEmpty(personId)) {
            XToast.toastShort(this, getString(R.string.message_no_person_id_error))
            finish()
            return
        }
        randomBg()
        linear_person_mobile_button.setOnClickListener(this)
        linear_person_email_button.setOnClickListener(this)
        linear_person_collection_button.setOnClickListener(this)
        image_person_back.setOnClickListener(this)
        btn_begin_talk.setOnClickListener(this)

        showLoadingDialog()
        mPresenter.loadPersonInfo(personId)
        mPresenter.isUsuallyPerson(O2SDKManager.instance().distinguishedName, personId)
    }
    // 随机背景图
    private fun randomBg() {
        val index = (0..5).random()
        XLog.debug("背景图片 $index")
        main_content.setBackgroundResource(backgroundList[index])
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.linear_person_mobile_button -> {
                if (!OrganizationPermissionManager.instance().isHiddenMobile(loadedPersonDN)) {
                    callPhone()
                }
            }
            R.id.linear_person_email_button -> sendEmail()
            R.id.linear_person_collection_button -> usuallyBtnClick()
            R.id.image_person_back -> finish()
            R.id.btn_begin_talk -> {
                // 开始聊天
                if (!TextUtils.isEmpty(loadedPersonDN) && O2SDKManager.instance().distinguishedName != loadedPersonDN) {
                    startTalk()
                }else {

                }
            }
        }
    }

    private fun startTalk() {
        mPresenter.startSingleTalk(loadedPersonDN)
    }

    private fun usuallyBtnClick() {
        if (O2SDKManager.instance().distinguishedName == loadedPersonDN) {
            XLog.debug("自己收藏自己。。。。$personId")
            return
        }
        if (hasCollection) {
            mPresenter.deleteUsuallyPerson(O2SDKManager.instance().distinguishedName, personId)
            image_person_collection.setImageResource(R.mipmap.icon_collection_disable_50dp)
            tv_person_collection_text.text = getString(R.string.person_collect)
            hasCollection = false
        } else {
            val mobile = tv_person_mobile.text.toString()
            val name = tv_person_name.text.toString()
            mPresenter.collectionUsuallyPerson(O2SDKManager.instance().distinguishedName, personId, O2SDKManager.instance().cName, name, genderName, mobile)
            image_person_collection.setImageResource(R.mipmap.icon_collection_enable_50dp)
            tv_person_collection_text.text = getString(R.string.person_collect_cancel)
            hasCollection = true
        }
    }

    private fun sendEmail() {
        val emailAddress = tv_person_email.text.toString()
        XLog.debug("发送邮件：$emailAddress")
        if (TextUtils.isEmpty(emailAddress)) {
            return
        }
        BottomSheetMenu(this).setItems(arrayListOf(getString(R.string.send_email), getString(R.string.copy)), ContextCompat.getColor(this, R.color.text_primary)) { i->
            when(i) {
                0 -> email(emailAddress)
                1 ->  {
                    AndroidUtils.copyTextToClipboard(emailAddress, this@PersonActivity)
                    XToast.toastShort(this@PersonActivity, getString(R.string.message_copy_email_success))
                }
            }
        }.show()

//        emailClickMenu.setOnDismissListener { ZoneUtil.lightOn(this@PersonActivity) }
//        emailClickMenu.onMenuItemClickListener = object : CommonMenuPopupWindow.OnMenuItemClickListener {
//            override fun itemClick(position: Int) {
//                when(position){
//                    0 -> email(emailAddress)
//                    1 ->  {
//                        AndroidUtils.copyTextToClipboard(emailAddress, this@PersonActivity)
//                        XToast.toastShort(this@PersonActivity, getString(R.string.message_copy_email_success))
//                    }
//                }
//                emailClickMenu.dismiss()
//            }
//        }
//        emailClickMenu.showAtLocation(main_content, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
//        ZoneUtil.lightOff(this)
    }

    private fun callPhone() {
        val phone = tv_person_mobile.text.toString()
        XLog.debug("拨打电话：$phone")
        if (TextUtils.isEmpty(phone)) {
            return
        }

        BottomSheetMenu(this).setItems(arrayListOf(getString(R.string.call), getString(R.string.sms), getString(R.string.copy)), ContextCompat.getColor(this, R.color.text_primary)) { i ->
            when(i) {
                0 -> makeCallDial(phone)
                1 -> sendSMS(phone)
                2 -> {
                    AndroidUtils.copyTextToClipboard(phone, this@PersonActivity)
                    XToast.toastShort(this@PersonActivity, getString(R.string.message_copy_phone_number_success))
                }
            }
        }.show()

//        mobileClickMenu.setOnDismissListener { ZoneUtil.lightOn(this@PersonActivity) }
//        mobileClickMenu.onMenuItemClickListener = object : CommonMenuPopupWindow.OnMenuItemClickListener {
//            override fun itemClick(position: Int) {
//                when (position) {
//                    0 -> makeCallDial(phone)
//                    1 -> sendSMS(phone)
//                    2 -> {
//                        AndroidUtils.copyTextToClipboard(phone, this@PersonActivity)
//                        XToast.toastShort(this@PersonActivity, getString(R.string.message_copy_phone_number_success))
//                    }
//                }
//                mobileClickMenu.dismiss()
//            }
//        }
//        mobileClickMenu.showAtLocation(main_content, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
//        ZoneUtil.lightOff(this)
    }

    override fun isUsuallyPerson(flag: Boolean) {
        if (flag) {
            image_person_collection.setImageResource(R.mipmap.icon_collection_enable_50dp)
            tv_person_collection_text.text = getString(R.string.person_collect_cancel)
            hasCollection = true
        } else {
            image_person_collection.setImageResource(R.mipmap.icon_collection_disable_50dp)
            tv_person_collection_text.text = getString(R.string.person_collect)
            hasCollection = false
        }
    }

    override fun loadPersonInfo(personInfo: PersonJson) {
        hideLoadingDialog()
        loadedPersonId = personInfo.id
        loadedPersonDN = personInfo.distinguishedName
        attributeList.clear()
        attributeList.addAll( personInfo.woPersonAttributeList )
        if (OrganizationPermissionManager.instance().isHiddenMobile(loadedPersonDN)) {
            tv_person_mobile.text = "***********"
        } else {
            tv_person_mobile.text = personInfo.mobile
        }

        tv_person_email.text = personInfo.mail
        if (GenderTypeEnums.FEMALE.key == personInfo.genderType) {
            linear_person_gender_women_button.visible()
            linear_person_gender_men_button.gone()
        } else {
            linear_person_gender_women_button.gone()
            linear_person_gender_men_button.visible()
        }
        genderName = GenderTypeEnums.getNameByKey(personInfo.genderType)
        if (!TextUtils.isEmpty(personInfo.signature)) {
            tv_person_sign.text = getString(R.string.person_sign).plus(personInfo.signature)
        }
        tv_person_name.text = personInfo.name
        tv_person_name_2.text = personInfo.name
        if (personInfo.woIdentityList.isNotEmpty()) {
            var department = ""
            personInfo.woIdentityList.mapIndexed { index, woIdentityListItem ->
                if (index != personInfo.woIdentityList.size - 1) {
                    department += woIdentityListItem.unitName + ","
                } else {
                    department += woIdentityListItem.unitName
                }
            }
            tv_person_department.text = department
        }
        tv_person_employee.text = personInfo.employee
        tv_person_distinguishedName.text = personInfo.distinguishedName
        tv_person_superior.text = personInfo.superior
        tv_person_phone.text = personInfo.officePhone
        tv_person_board_date.text = personInfo.boardDate
        tv_person_describe.text = personInfo.description


        val url = APIAddressHelper.instance().getPersonAvatarUrlWithId(personInfo.id)
        O2ImageLoaderManager.instance().showImage(image_person_avatar, url, O2ImageLoaderOptions(placeHolder = R.mipmap.icon_avatar_men))

        // 属性列表 添加到个人信息中
        if (attributeList.isNotEmpty()) {
            attributeList.forEach { attr ->
                if (attr.name != "appBindDeviceList") {
                    val divider = View(this@PersonActivity)
                    divider.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        this@PersonActivity.dip(0.5f)
                    )
                    divider.setBackgroundColor(
                        ContextCompat.getColor(
                            this@PersonActivity,
                            R.color.z_color_split_line_ddd
                        )
                    )
                    ll_person_form_box.addView(divider)
                    val ll = LinearLayout(this@PersonActivity)
                    ll.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        this@PersonActivity.dip(44f)
                    )
                    ll.gravity = Gravity.CENTER_VERTICAL
                    ll.orientation = LinearLayout.HORIZONTAL
                    ll_person_form_box.addView(ll)
                    val label = TextView(this@PersonActivity)
                    val layout =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                    layout.weight = 1f
                    label.layoutParams = layout
                    label.setTextColor(
                        ContextCompat.getColor(
                            this@PersonActivity,
                            R.color.z_color_text_primary
                        )
                    )
                    label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    label.text = attr.name // 属性名称
                    ll.addView(label)
                    val tvValue = TextView(this@PersonActivity)
                    val layout2 =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                    layout2.weight = 2f
                    tvValue.layoutParams = layout2
                    tvValue.setTextColor(
                        ContextCompat.getColor(
                            this@PersonActivity,
                            R.color.z_color_text_primary_dark
                        )
                    )
                    tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    tvValue.text = attr.attributeList.joinToString()
                    ll.addView(tvValue)
                }
            }
        }

    }

    override fun loadPersonInfoFail() {
        hideLoadingDialog()
    }

    override fun createConvSuccess(conv: IMConversationInfo) {
        O2ChatActivity.startChat(this, conv.id!!)
    }

    override fun createConvFail(message: String) {
        XLog.error(message)
        XToast.toastShort(this, getString(R.string.message_start_im_fail))
    }
}
