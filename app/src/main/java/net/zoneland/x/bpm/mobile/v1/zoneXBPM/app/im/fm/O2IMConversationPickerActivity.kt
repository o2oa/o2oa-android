package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.fm

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_o2_imconversation_picker.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.O2IM
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMConversationInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.ContactPickerResult
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleImageView

class O2IMConversationPickerActivity : BaseMVPActivity<O2IMConversationPickerContract.View, O2IMConversationPickerContract.Presenter>(), O2IMConversationPickerContract.View {

    companion object {
        const val IM_CONVERSATION_PICKED_RESULT = "IM_CONVERSATION_PICKED_RESULT"
    }

    override var mPresenter: O2IMConversationPickerContract.Presenter = O2IMConversationPickerPresenter()


    private val cList = ArrayList<IMConversationInfo>()
    private val adapter: CommonRecycleViewAdapter<IMConversationInfo> by lazy {
        object : CommonRecycleViewAdapter<IMConversationInfo>(this, cList,
            R.layout.item_o2_im_conversation_picker) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: IMConversationInfo?) {
                if (holder != null && t != null) {
                    if (t.type == O2IM.conversation_type_single) {
                        //头像
                        val person = t.personList.firstOrNull { it != O2SDKManager.instance().distinguishedName }
                        if (person != null) {
                            val url = APIAddressHelper.instance().getPersonAvatarUrlWithId(person)
                            val avatar = holder.getView<CircleImageView>(R.id.image_o2_im_con_avatar)
                            O2ImageLoaderManager.instance().showImage(avatar, url)
                            val name = if (person.indexOf("@") > 0) {
                                person.substring(0, person.indexOf("@"))
                            }else {
                                person
                            }
                            holder.setText(R.id.tv_o2_im_con_title, name)
                        }
                    }else if(O2IM.conversation_type_group == t.type) {
                        holder.setText(R.id.tv_o2_im_con_title, t.title )
                            .setImageViewResource(R.id.image_o2_im_con_avatar, R.mipmap.group_default)
                    }
                }
            }
        }
    }


    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar(getString(R.string.im_choose_conversation), true)
        rv_o2_im_conversation.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_o2_im_conversation.adapter = adapter
        adapter.setOnItemClickListener { _, position ->
            pickerConversation(position)
        }
        mPresenter.getMyConversationList()
    }

    override fun layoutResId(): Int = R.layout.activity_o2_imconversation_picker


    override fun myConversationList(list: List<IMConversationInfo>) {
        if (list.isEmpty()) {
            tv_null_conversation.visible()
            rv_o2_im_conversation.gone()
        } else {
            tv_null_conversation.gone()
            rv_o2_im_conversation.visible()
            cList.clear()
            cList.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }


    private fun pickerConversation(position: Int) {
        val con = cList[position]
        intent.putExtra(IM_CONVERSATION_PICKED_RESULT, con)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}