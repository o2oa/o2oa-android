package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.wugang.activityresult.library.ActivityResult
import kotlinx.android.synthetic.main.activity_o2_chat_group_member.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.ContactPickerResult
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleImageView

/**
 * Created by fancyLou on 2021-01-13.
 * Copyright © 2021 O2. All rights reserved.
 */
class O2ChatGroupMemberActivity : BaseMVPActivity<O2ChatGroupMemberContract.View, O2ChatGroupMemberContract.Presenter>(), O2ChatGroupMemberContract.View {

    override var mPresenter: O2ChatGroupMemberContract.Presenter = O2ChatGroupMemberPresenter()


    private val invitePersonAdd = "添加"
    private val groupMembers = ArrayList<String>()
    private var conversationId: String = ""

    override fun layoutResId(): Int = R.layout.activity_o2_chat_group_member


    companion object {
        const val conversationIdKey = "conversationIdKey"
        const val groupMembersKey = "groupMembersKey"

        fun openEditGroupMembers(conversationId: String, groupMembers: ArrayList<String>) : Bundle {
            val bundle = Bundle()
            bundle.putString(conversationIdKey, conversationId)
            bundle.putStringArrayList(groupMembersKey, groupMembers)
            return bundle
        }
    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        val cId = intent?.extras?.getString(conversationIdKey)
        if (TextUtils.isEmpty(cId)) {
            XToast.toastShort(this, "错误参数")
            finish()
            return
        }
        conversationId = cId!!
        val members = intent?.extras?.getStringArrayList(groupMembersKey)
        if (members == null || members.isEmpty()) {
            XToast.toastShort(this, "错误参数")
            finish()
            return
        }

        setupToolBar("管理成员", true)

        groupMembers.clear()
        groupMembers.add(invitePersonAdd)
        groupMembers.addAll(members)

        rv_o2_chat_members.layoutManager = GridLayoutManager(this, 5)
        rv_o2_chat_members.adapter = groupMembersAdapter
        groupMembersAdapter.notifyDataSetChanged()
        groupMembersAdapter.setOnItemClickListener { view, position ->
            if (position == 0) { //新增
                addMembers()
            }else {
                deleteMember(position)
            }
        }
    }

    private fun addMembers() {
        val users = groupMembers
        users.removeAt(0)
        ActivityResult.of(this)
        .className(ContactPickerActivity::class.java)
        .params(ContactPickerActivity.startPickerBundle(pickerModes = arrayListOf(ContactPickerActivity.personPicker), multiple = true, initUserList = users))
        .greenChannel().forResult { _, data ->
            val result = data?.getParcelableExtra<ContactPickerResult>(ContactPickerActivity.CONTACT_PICKED_RESULT)
            if (result != null && result.users.isNotEmpty()) {
                val a = arrayListOf<String>()
                a.addAll(result.users.map { it.distinguishedName })
                if (!a.any { it == O2SDKManager.instance().distinguishedName }) {
                    a.add(O2SDKManager.instance().distinguishedName)
                }
                showLoadingDialog()
                XLog.debug("选择人员 ${users.joinToString()}")
                mPresenter.updateConversationPeople(conversationId, a)
            }else {
                XLog.debug("没有选择人员！！！！")
            }
        }
    }

    private fun deleteMember(position: Int) {
        val users = groupMembers
        users.removeAt(position)
        users.removeAt(0)
        showLoadingDialog()
        XLog.debug("群成员：${users.joinToString()}")
        mPresenter.updateConversationPeople(conversationId, users)
    }

    override fun updateSuccess(info: IMConversationInfo) {
        hideLoadingDialog()
        groupMembers.clear()
        groupMembers.add(invitePersonAdd)
        groupMembers.addAll(info.personList)
        groupMembersAdapter.notifyDataSetChanged()
    }

    override fun updateFail(msg: String) {
        hideLoadingDialog()
        XToast.toastShort(this, msg)
    }

    private val groupMembersAdapter: CommonRecycleViewAdapter<String> by lazy {
        object : CommonRecycleViewAdapter<String>(this, groupMembers, R.layout.item_person_avatar_name) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: String?) {
                if (TextUtils.isEmpty(t)){
                    XLog.error("person id is null!!!!!!")
                    return
                }
                val avatar = holder?.getView<CircleImageView>(R.id.circle_image_avatar)
                avatar?.setImageResource(R.mipmap.contact_icon_avatar)
                val delete = holder?.getView<ImageView>(R.id.delete_people_iv)
                delete?.visibility = View.VISIBLE
                if (avatar!=null) {
                    if (invitePersonAdd==t){
                        avatar.setImageResource(R.mipmap.icon_add_people)
                        delete?.visibility = View.GONE
                    }else {
                        val url = APIAddressHelper.instance().getPersonAvatarUrlWithId(t!!)
                        O2ImageLoaderManager.instance().showImage(avatar, url)
                    }
                }
                val nameTv = holder?.getView<TextView>(R.id.tv_name)
                if (nameTv!=null) {
                    if(invitePersonAdd==t){
                        nameTv.text = t
                    }else{
                        if (t != null && t.contains("@")) {
                            nameTv.text = t.split("@").first()
                        }else {
                            nameTv.text = t
                        }
                    }
                }
            }
        }
    }
}