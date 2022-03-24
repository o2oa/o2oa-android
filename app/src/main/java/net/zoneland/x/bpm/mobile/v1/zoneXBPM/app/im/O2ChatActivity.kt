package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.wugang.activityresult.library.ActivityResult
import com.zlw.main.recorderlib.RecordManager
import com.zlw.main.recorderlib.recorder.RecordConfig
import com.zlw.main.recorderlib.recorder.RecordHelper
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener
import kotlinx.android.synthetic.main.activity_o2_chat.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.LocalImageViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.tbs.FileReaderActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PickTypeMode
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class O2ChatActivity : BaseMVPActivity<O2ChatContract.View, O2ChatContract.Presenter>(), O2ChatContract.View, View.OnTouchListener, SensorEventListener {

    companion object {
        const val con_id_key = "con_id_key"
        fun startChat(activity: Activity, conversationId: String) {
            val bundle = Bundle()
            bundle.putString(con_id_key, conversationId)
            activity.go<O2ChatActivity>(bundle)
        }
    }


    override var mPresenter: O2ChatContract.Presenter = O2ChatPresenter()

    override fun layoutResId(): Int = R.layout.activity_o2_chat

    private var imConfig: IMConfig = IMConfig()
    private val adapter: O2ChatMessageAdapter by lazy { O2ChatMessageAdapter() }
    private val emojiList = O2IM.im_emoji_hashMap.keys.toList().sortedBy { it }
    private val emojiAdapter: CommonRecycleViewAdapter<String> by lazy {
        object : CommonRecycleViewAdapter<String>(this, emojiList, R.layout.item_o2_im_chat_emoji) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: String?) {
                if (t != null) {
                    holder?.setImageViewResource(R.id.image_item_o2_im_chat_emoji, O2IM.emojiResId(t))
                }
            }
        }
    }

    //
    private val defaultTitle = "聊天界面"
    private var page = 0

    private var conversationId = ""

    private var conversationInfo: IMConversationInfo? = null
    //录音服务
    private var isAudioRecordCancel = false
    private var audioRecordTime = 0L
    //录音计时器
    private val audioCountDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                XLog.debug("倒计时结束！")
                endRecordAudio()
            }

            override fun onTick(millisUntilFinished: Long) {
                val sec = ((millisUntilFinished + 15) / 1000)
                audioRecordTime = 60 - sec
                runOnUiThread {
                    val times = if (audioRecordTime > 9) {
                        "00:$audioRecordTime"
                    } else {
                        "00:0$audioRecordTime"
                    }
                    tv_o2_chat_audio_speak_duration.text = times
                }
                XLog.debug("倒计时还剩余：$sec 秒")
            }

        }
    }

    //media play
//    private var mPlayer: MediaPlayer? = null

    //拍照
//    private val cameraImageUri: Uri by lazy { FileUtil.getUriFromFile(this, File(FileExtensionHelper.getCameraCacheFilePath(this))) }
    private  val TAKE_FROM_CAMERA_CODE = 1004
    private var cameraImagePath: String? = null

    //是否能修改群名 群成员
    private var canUpdate = false




//    private var mKeyboardHeight = 150 // 输入法默认高度为400


    override fun afterSetContentView(savedInstanceState: Bundle?) {
        // 起初的布局可自动调整大小 WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
        window.setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        setupToolBar(defaultTitle, setupBackButton = true)
        val json = O2SDKManager.instance().prefs().getString(O2.PRE_IM_CONFIG_KEY, "") ?: ""
        if (!TextUtils.isEmpty(json)) {
            val c = O2SDKManager.instance().gson.fromJson(json, IMConfig::class.java)
            if (c != null) {
                imConfig = c
            }
        }

        conversationId = intent.getStringExtra(con_id_key) ?: ""
        if (TextUtils.isEmpty(conversationId)) {
            XToast.toastShort(this, "缺少参数！")
            finish()
        }
        //消息列表初始化
        sr_o2_chat_message_layout.setOnRefreshListener {
            XLog.debug("下啦零零落落零零落落来了")
            getPageData()
        }
        rv_o2_chat_messages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_o2_chat_messages.adapter = adapter
        adapter.eventListener = object : O2ChatMessageAdapter.MessageEventListener {
            override fun resendClick(message: IMMessage) {
                mPresenter.sendIMMessage(message)//重新发送
            }

            override fun playAudio(position: Int, msgBody: IMMessageBody) {
                XLog.debug("audio play position: $position")
                mPresenter.getFileFromNetOrLocal(position, msgBody)
            }

            override fun openOriginImage(position: Int, msgBody: IMMessageBody) {
                 mPresenter.getFileFromNetOrLocal(position, msgBody)
            }

            override fun openLocation(msgBody: IMMessageBody) {
                val location = O2LocationActivity.LocationData(msgBody.address, msgBody.addressDetail, msgBody.latitude, msgBody.longitude)
                val bundle = O2LocationActivity.showLocation(location)
                go<O2LocationActivity>(bundle)
            }

            override fun openFile(position: Int, msgBody: IMMessageBody) {
                mPresenter.getFileFromNetOrLocal(position, msgBody)
            }

            override fun onCreateContextMenu(menu: ContextMenu?, message: IMMessage) {
                createContextMenu(menu, message)
            }

            override fun openProcessWork(position: Int, msgBody: IMMessageBody) {
                if (!TextUtils.isEmpty(msgBody.work)) {
                    go<TaskWebViewActivity>(TaskWebViewActivity.start(msgBody.work, "", ""))
                }
            }
        }
        //输入法切换的时候滚动到底部
        cl_o2_chat_outside.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                scroll2Bottom()
            }
        }

        //表情初始化
        rv_o2_chat_emoji_box.layoutManager = GridLayoutManager(this, 10)
        rv_o2_chat_emoji_box.adapter = emojiAdapter
        emojiAdapter.setOnItemClickListener { _, position ->
            val key = emojiList[position]
            XLog.debug(key)
            newEmojiMessage(key)
            //更新阅读时间
            mPresenter.readConversation(conversationId)
        }

        initListener()

        sr_o2_chat_message_layout.isRefreshing = true
        getPageData()

        //录音格式
        initAudioRecord()

        registerBroadcast()

        // 距离监听 手机是否靠近耳朵
        sensorListen()
    }

    /**
     * 聊天消息
     * 长按菜单
     */
    private fun createContextMenu(menu: ContextMenu?, message: IMMessage) {
        val menuList = ArrayList<String>()
        // 撤回菜单
        if (imConfig.enableRevokeMsg) {
            if (message.createPerson == O2SDKManager.instance().distinguishedName) {
                menuList.add(O2IM.IM_Message_Menu_name_Revoke)
            } else if (conversationInfo?.adminPerson == O2SDKManager.instance().distinguishedName) {
                menuList.add(O2IM.IM_Message_Menu_name_Revoke_group)
            }
        }
        if (menuList.isNotEmpty()) {
            val groupId = 0
            menuList.forEachIndexed { index, s ->
                menu?.add(groupId, index, index, s)
                menu?.getItem(index)?.setOnMenuItemClickListener { item ->
                    if (item.title == O2IM.IM_Message_Menu_name_Revoke || item.title == O2IM.IM_Message_Menu_name_Revoke_group) {
                        revokeMsg(message)
                    }
                    true
                }
            }
        }
    }

    /**
     * 撤回
     */
    private fun revokeMsg(message: IMMessage) {
        XLog.debug("撤回消息，${message.createPerson}" )
        adapter.removeMessage(message)
        mPresenter.revokeMsg(message.id)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        if (canUpdate) {
            if (imConfig.enableClearMsg) {
                menuInflater.inflate(R.menu.menu_chat_with_clear, menu)
            } else {
                menuInflater.inflate(R.menu.menu_chat, menu)
            }
        } else {
            menuInflater.inflate(R.menu.menu_chat_no_update, menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_chat_report -> {
                openReportDialog()
                return true
            }
            R.id.menu_chat_update_title -> {
                updateTitle()
                return true
            }
            R.id.menu_chat_update_member -> {
                updateMembers()
                return true
            }
            R.id.menu_chat_clear_msg -> {
                clearAllMsg()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openReportDialog() {
        O2DialogSupport.openCustomViewDialog(this, getString(R.string.menu_im_report), R.layout.dialog_im_report) { dialog ->
            val reason = dialog.findViewById<AppCompatEditText>(R.id.et_im_report_dialog_reason)
            val r = reason.text.toString()
            XLog.info("举报 理由 ：$r")
            val desc = dialog.findViewById<AppCompatEditText>(R.id.et_im_report_dialog_desc)
            val d = desc.text.toString()
            XLog.info("举报 详情 ：$d")
            if (TextUtils.isEmpty(r)) {
                XToast.toastShort(this, "举报理由不能为空！")
                return@openCustomViewDialog
            }
            if (TextUtils.isEmpty(d)) {
                XToast.toastShort(this, "详情描述不能为空！")
                return@openCustomViewDialog
            }

            XToast.toastShort(this, "感谢您的提交，我们会尽快核实并处理！")
        }
    }

    /**
     * 清空聊天记录
     */
    private fun clearAllMsg() {
        O2DialogSupport.openConfirmDialog(this, getString(R.string.im_message_confirm_delete_msgs), {
            _ ->
            mPresenter.deleteAllChatMsg(conversationId)
        })
    }

    private fun updateTitle() {
        val dialog = O2DialogSupport.openCustomViewDialog(this, getString(R.string.menu_im_tribe_name_update), R.layout.dialog_name_modify) { dialog ->
            val text = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
            val content = text.text.toString()
            dialog.dismiss()
            if (TextUtils.isEmpty(content)) {
                XToast.toastShort(this@O2ChatActivity, getString(R.string.im_tribe_name_cannot_empty))
            } else {
                showLoadingDialog()
                mPresenter.updateConversationTitle(conversationId, content)
            }
        }
        val edit = dialog.findViewById<EditText>(R.id.dialog_name_editText_id)
        edit.hint = getString(R.string.im_hint_input_tribe_name)
    }

    private fun updateMembers() {
        val users = conversationInfo?.personList ?: ArrayList<String>()

        go<O2ChatGroupMemberActivity>(O2ChatGroupMemberActivity.openEditGroupMembers(conversationId, users))



//        ActivityResult.of(this)
//                .className(ContactPickerActivity::class.java)
//                .params(ContactPickerActivity.startPickerBundle(pickerModes = arrayListOf(ContactPickerActivity.personPicker), multiple = true, initUserList = users))
//                .greenChannel().forResult { _, data ->
//                    val result = data?.getParcelableExtra<ContactPickerResult>(ContactPickerActivity.CONTACT_PICKED_RESULT)
//                    if (result != null && result.users.isNotEmpty()) {
//                        val a = arrayListOf<String>()
//                        a.addAll(result.users.map { it.distinguishedName })
//                        if (!a.any { it == O2SDKManager.instance().distinguishedName }) {
//                            a.add(O2SDKManager.instance().distinguishedName)
//                        }
//                        showLoadingDialog()
//                        mPresenter.updateConversationPeople(conversationId, a)
//                    }else {
//                        XLog.debug("没有选择人员！！！！")
//                    }
//                }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
        }
//        if (mPlayer != null) {
//            mPlayer?.release()//释放资源
//            mPlayer = null
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_FROM_CAMERA_CODE) {
            //拍照
            XLog.debug("拍照//// ")
//            newImageMessage(FileExtensionHelper.getCameraCacheFilePath(this))
            if (!TextUtils.isEmpty(cameraImagePath)) {
                newImageMessage(cameraImagePath!!)
            }
        }
    }

    private var startY: Float = 0f
    private var isCancelRecord = false


    /**
     * 录音按钮的touch事件
     * 按住录音
     * 释放发送语音消息
     * 上滑取消发送
     */
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.y
                XLog.debug("点击开始............录音")
                updateRecordingDialogUI(0)
                recordingDialog?.show()
                startRecordAudio()
            }
            MotionEvent.ACTION_UP -> {
                XLog.debug("结束了................录音")
                if (isCancelRecord) {
                    XLog.debug("取消了录音.....")
                    cancelRecordAudio()
                }else {
                    XLog.debug("完成了录音.....")
                    endRecordAudio()
                }
                recordingDialog?.dismiss()
            }
            MotionEvent.ACTION_MOVE -> {
                val moveY = event.y
                if (startY - moveY > 100) {
                    isCancelRecord = true
                    updateRecordingDialogUI(1)
                }
//                if (startY - moveY < 20) {
//                    isCancelRecord = false
//                    updateRecordingDialogUI(0)
//                }
            }
            MotionEvent.ACTION_CANCEL -> {
                XLog.debug("取消了................录音")
                cancelRecordAudio()
                recordingDialog?.dismiss()
            }
        }
        return true
    }

    private var tvPrompt: TextView? = null
    private var ivLoadGif: GifImageView? = null
    private var recordingDialog: AlertDialog? = null
    private fun recordingDialog() : AlertDialog {
        val dialogBuilder = AlertDialog.Builder(this, R.style.DialogManage)
        dialogBuilder.setCancelable(false)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_voice_speak, null)
        tvPrompt = view.findViewById<TextView>(R.id.tv_prompt)
        ivLoadGif = view.findViewById(R.id.iv_load_gif)
        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(BitmapDrawable())
        return dialog
    }

    /**
     * @param status 0 录制中 1 取消发送
     */
    private fun updateRecordingDialogUI(status: Int) {
        when(status) {
            0 -> {
                tvPrompt?.text = "松开发送，上滑取消"
                ivLoadGif?.setImageResource(R.mipmap.listener08_anim)
            }
            1 -> {
                tvPrompt?.text = "松开手指，取消发送"
                ivLoadGif?.setImageResource(R.mipmap.chat_audio_record_cancel)
            }
        }
    }


    override fun updateSuccess(info: IMConversationInfo) {
        hideLoadingDialog()
        this.conversationInfo?.title = info.title
        updateToolbarTitle(info.title)
        this.conversationInfo?.personList = info.personList
    }

    override fun updateFail(msg: String) {
        hideLoadingDialog()
        XToast.toastShort(this, msg)
    }

    override fun conversationInfo(info: IMConversationInfo) {
        conversationInfo = info
        if (conversationInfo?.adminPerson == O2SDKManager.instance().distinguishedName) {
            canUpdate = true
            invalidateOptionsMenu()
        }
        //
        var title = defaultTitle
        if (O2IM.conversation_type_single == conversationInfo?.type) {
            val persons = conversationInfo?.personList
            if (persons != null && persons.isNotEmpty()) {
                val person = persons.firstOrNull { it != O2SDKManager.instance().distinguishedName }
                if (person != null) {
                    title = if (person.indexOf("@") > 0) {
                        person.substring(0, person.indexOf("@"))
                    }else {
                        person
                    }
                }
            }
        } else if (O2IM.conversation_type_group == conversationInfo?.type) {
            title = conversationInfo?.title ?: defaultTitle
        }
        updateToolbarTitle(title)
    }

    override fun conversationGetFail() {
        XToast.toastShort(this, "获取会话信息异常！")
        finish()
    }

    override fun backPageMessages(list: List<IMMessage>) {
        sr_o2_chat_message_layout.isRefreshing = false
        if (list.isNotEmpty()) {
            page++
            adapter.addPageMessage(list)
        }
        //第一次 滚动到底部
        if (page == 1) {
            scroll2Bottom()
        }
    }

    override fun sendMessageSuccess(id: String) {
        //消息前面的loading消失
        adapter.sendMessageSuccess(id)
    }

    override fun sendFail(id: String) {
        //消息前面的loading消失 变成重发按钮
        adapter.sendMessageFail(id)
    }

    override fun localFile(filePath: String, msgType: String, position: Int) {
        XLog.debug("local file :$filePath type:$msgType")
        when (msgType) {
            MessageType.audio.key -> {
                playAudio2(filePath, position)
            }
            MessageType.image.key -> {
                //打开大图
                go<LocalImageViewActivity>(LocalImageViewActivity.startBundle(filePath))
            }
            else -> go<FileReaderActivity>(FileReaderActivity.startBundle(filePath))
        }

    }

    override fun downloadFileFail(msg: String) {
        XToast.toastShort(this, msg)
    }

    override fun deleteAllChatMsgSuccess() {
        XToast.toastShort(this, getString(R.string.im_message_clear_msg_success))
        page = 0
        adapter.clearAllMessage()
        getPageData()
    }

    override fun deleteAllChatMsgFail(msg: String) {
        XToast.toastShort(this, msg)
    }

    override fun revokeMsgSuccess() {
        //XToast.toastShort(this, getString(R.string.im_message_clear_msg_success))
        XLog.info("撤回消息成功！")
    }

    override fun revokeMsgFail(msg: String) {
        XToast.toastShort(this, getString(R.string.im_message_revoke_fail) + msg)
    }

    /**
     * 监听
     */
    private fun initListener() {
        et_o2_chat_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && !TextUtils.isEmpty(s)) {
                    btn_o2_chat_send.visible()
                    btn_o2_chat_emotion.gone()
                    btn_o2_chat_plus.gone()
                } else {
                    btn_o2_chat_emotion.visible()
                    btn_o2_chat_plus.visible()
                    btn_o2_chat_send.gone()
                }
            }
        })
        et_o2_chat_input.setOnClickListener {
            rv_o2_chat_emoji_box_out.postDelayed({
                rv_o2_chat_emoji_box.gone()
                tv_o2_chat_audio_send_box.gone()
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }, 250)
        }
        rv_o2_chat_emoji_box_out.setKeyboardListener { isActive, keyboardHeight ->
            if (isActive) { // 输入法打开
//                if (mKeyboardHeight != keyboardHeight) { // 键盘发生改变时才设置emojiView的高度，因为会触发onGlobalLayoutChanged，导致onKeyboardStateChanged再次被调用
//                    mKeyboardHeight = keyboardHeight
//                    initEmojiView() // 每次输入法弹起时，设置emojiView的高度为键盘的高度，以便下次emojiView弹出时刚好等于键盘高度
//                }
                if (rv_o2_chat_emoji_box.visibility == View.VISIBLE) { // 表情打开状态下
                    rv_o2_chat_emoji_box.gone()
                }
                if (ll_o2_chat_tool_bar.visibility == View.VISIBLE) { // 按钮区域
                    ll_o2_chat_tool_bar.gone()
                }
                if (tv_o2_chat_audio_send_box.visibility == View.VISIBLE) { // 语音按钮
                    tv_o2_chat_audio_send_box.gone()
                }
            }
        }
        btn_o2_chat_emotion.setOnClickListener {
            //关闭语音框
            tv_o2_chat_audio_send_box.gone()
            //关闭操作按钮区域
            ll_o2_chat_tool_bar.gone()
            //
            if (rv_o2_chat_emoji_box_out.isKeyboardActive) { //输入法激活时
                if (rv_o2_chat_emoji_box.visibility == View.GONE) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) //  不改变布局，隐藏键盘，emojiView弹出
                    val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et_o2_chat_input.applicationWindowToken, 0)
                    rv_o2_chat_emoji_box.visibility = View.VISIBLE
                } else {
                    rv_o2_chat_emoji_box.visibility = View.GONE
                    val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et_o2_chat_input.applicationWindowToken, 0)
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
            } else {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                if (rv_o2_chat_emoji_box.visibility == View.GONE) {
                    rv_o2_chat_emoji_box.visibility = View.VISIBLE
                } else {
                    rv_o2_chat_emoji_box.visibility = View.GONE
                }
            }
        }
        // 按钮区域按钮显示隐藏
        btn_o2_chat_plus.setOnClickListener {
            //关闭语音框
            tv_o2_chat_audio_send_box.gone()
            //关闭表情框
            rv_o2_chat_emoji_box.gone()
            if (rv_o2_chat_emoji_box_out.isKeyboardActive) { //输入法激活时
                if (ll_o2_chat_tool_bar.visibility == View.GONE) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) //  不改变布局，隐藏键盘，emojiView弹出
                    val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et_o2_chat_input.applicationWindowToken, 0)
                    ll_o2_chat_tool_bar.visibility = View.VISIBLE
                } else {
                    ll_o2_chat_tool_bar.visibility = View.GONE
                    val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et_o2_chat_input.applicationWindowToken, 0)
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
            }else {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                if (ll_o2_chat_tool_bar.visibility == View.GONE) {
                    ll_o2_chat_tool_bar.visibility = View.VISIBLE
                } else {
                    ll_o2_chat_tool_bar.visibility = View.GONE
                }
            }

        }
        btn_o2_chat_send.setOnClickListener {
            sendTextMessage()
        }

        //bottom toolbar
        image_o2_chat_audio_speak_btn.setOnTouchListener(this)
        img_o2_chat_mic.setOnClickListener {
            //先检查录音权限
            PermissionRequester(this@O2ChatActivity)
                    .request(Manifest.permission.RECORD_AUDIO)
                    .o2Subscribe {
                        onNext { (granted, _, _) ->
                            if (!granted){
                                O2DialogSupport.openAlertDialog(this@O2ChatActivity, "语音消息需要权限, 去设置", { permissionSetting() })
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                        }
                    }
            //关闭表情框
            rv_o2_chat_emoji_box.gone()
            //关闭按钮区域
            ll_o2_chat_tool_bar.gone()
            if (rv_o2_chat_emoji_box_out.isKeyboardActive) { //输入法激活时
                if (tv_o2_chat_audio_send_box.visibility == View.GONE) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) //  不改变布局，隐藏键盘，emojiView弹出
                    val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et_o2_chat_input.applicationWindowToken, 0)
                    tv_o2_chat_audio_send_box.visibility = View.VISIBLE
                } else {
                    tv_o2_chat_audio_send_box.visibility = View.GONE
                    val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et_o2_chat_input.applicationWindowToken, 0)
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
            } else {
                if (tv_o2_chat_audio_send_box.visibility == View.GONE) {
                    tv_o2_chat_audio_send_box.visibility = View.VISIBLE
                } else {
                    tv_o2_chat_audio_send_box.visibility = View.GONE
                }
            }
        }
        ll_o2_chat_album_btn.setOnClickListener {
            PicturePickUtil().withAction(this)
                .forResult { files ->
                    if (files!=null && files.isNotEmpty()) {
                        newImageMessage(files[0])
                    }
                }
        }
        ll_o2_chat_camera_btn.setOnClickListener {
            PermissionRequester(this@O2ChatActivity).request(Manifest.permission.CAMERA)
                    .o2Subscribe {
                        onNext {  (granted, _, _) ->
                            if (!granted){
                                O2DialogSupport.openAlertDialog(this@O2ChatActivity, "拍照需要权限, 去设置", { permissionSetting() })
                            } else {
                                openCamera()
                            }
                        }
                        onError { e, _ ->
                            XLog.error("", e)
                        }
                    }
        }
        ll_o2_chat_location_btn.setOnClickListener {
            ActivityResult.of(this)
                    .className(O2LocationActivity::class.java)
                    .params(O2LocationActivity.startChooseLocation())
                    .greenChannel()
                    .forResult { resultCode, data ->
                        if (resultCode == Activity.RESULT_OK) {
                            val location = data.extras?.getParcelable<O2LocationActivity.LocationData>(O2LocationActivity.RESULT_LOCATION_KEY)
                            if (location != null) {
                                newLocationMessage(location)
                            }
                        }
                    }
        }
        ll_o2_chat_file_btn.setOnClickListener {
            //文件选择器
            PicturePickUtil().withAction(this)
                .setMode(PickTypeMode.File)
                .forResult { files ->
                    if (files !=null && files.isNotEmpty()) {
                        newFileMessage(files[0])
                    }
                }
        }
    }


    private fun openCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        //return-data false 不是直接返回拍照后的照片Bitmap 因为照片太大会传输失败
//        intent.putExtra("return-data", false)
//        //改用Uri 传递
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
//        intent.putExtra("noFaceDetection", true)
//        startActivityForResult(intent, camera_result_code)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    FileExtensionHelper.createImageFile(this)
                } catch (ex: IOException) {
                    XToast.toastShort(this, getString(R.string.message_camera_file_create_error))
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    cameraImagePath = it.absolutePath
                    val photoURI = FileUtil.getUriFromFile(this, it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_FROM_CAMERA_CODE)
                }
            }
        }
    }


    private fun permissionSetting() {
        val packageUri = Uri.parse("package:$packageName")
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
    }

    // 设置表情栏的高度
//    private fun initEmojiView() {
//        val layoutParams = rv_o2_chat_emoji_box.layoutParams
//        layoutParams.height = mKeyboardHeight
//        rv_o2_chat_emoji_box.layoutParams = layoutParams
//    }

    /**
     * 初始化录音相关对象
     */
    private fun initAudioRecord() {
        RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.MP3)
        RecordManager.getInstance().changeRecordConfig(RecordManager.getInstance().recordConfig.setSampleRate(16000))
        RecordManager.getInstance().changeRecordConfig(RecordManager.getInstance().recordConfig.setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT))
        RecordManager.getInstance().changeRecordDir(FileExtensionHelper.getXBPMTempFolder(this) + File.separator)
        RecordManager.getInstance().setRecordStateListener(object : RecordStateListener {
            override fun onError(error: String?) {
                XLog.error("录音错误, $error")
            }

            override fun onStateChange(state: RecordHelper.RecordState?) {
                when (state) {
                    RecordHelper.RecordState.IDLE -> XLog.debug("录音状态， 空闲状态")
                    RecordHelper.RecordState.RECORDING -> {
                        XLog.debug("录音状态， 录音中")
                        audioCountDownTimer.start()
                    }
                    RecordHelper.RecordState.PAUSE -> XLog.debug("录音状态， 暂停中")
                    RecordHelper.RecordState.STOP -> XLog.debug("录音状态， 正在停止")
                    RecordHelper.RecordState.FINISH -> XLog.debug("录音状态， 录音流程结束（转换结束）")
                }

            }
        })
        RecordManager.getInstance().setRecordResultListener { result ->
            if (result == null) {
                runOnUiThread { XToast.toastShort(this@O2ChatActivity, "录音失败！") }
            } else {
                XLog.debug("录音结束 返回结果 ${result.path} ， 是否取消：$isAudioRecordCancel, 录音时间：$audioRecordTime")
                if (audioRecordTime < 1) {
                    runOnUiThread {
                        XToast.toastShort(this@O2ChatActivity, "录音时间太短！")
                    }
                } else {
                    if (!isAudioRecordCancel) {
                        newAudioMessage(result.path, "$audioRecordTime")
                    }
                }
            }
        }
        recordingDialog = recordingDialog()
    }

    /**
     * 开始录音
     */
    private fun startRecordAudio() {
        XLog.debug("开始录音。。。。")
        audioRecordTime = 0L
        RecordManager.getInstance().start()
        tv_o2_chat_audio_speak_title.text = resources.getText(R.string.activity_im_audio_speak_cancel)
    }

    /**
     * 结束录音
     */
    private fun endRecordAudio() {
        XLog.debug("结束录音。。。。")
        audioCountDownTimer.cancel()
        RecordManager.getInstance().stop()
        tv_o2_chat_audio_speak_title.text = resources.getText(R.string.activity_im_audio_speak)
        tv_o2_chat_audio_speak_duration.text = ""
    }

    /**
     * 取消录音
     */
    private fun cancelRecordAudio() {
        XLog.debug("取消录音。。。。。")
        isAudioRecordCancel = true
        audioCountDownTimer.cancel()
        RecordManager.getInstance().stop()
        tv_o2_chat_audio_speak_title.text = resources.getText(R.string.activity_im_audio_speak)
        tv_o2_chat_audio_speak_duration.text = ""
    }

    private fun playAudio2(filePath: String, position: Int) {

        O2MediaPlayerManager.instance().startPlay(filePath) {
            adapter.stopAudioAnimation()
        }
//
//        if (mPlayer != null) {
//            mPlayer?.release()
//            mPlayer = null
//        }
//        XLog.debug("uri : $filePath")
//        val uri = Uri.fromFile(File(filePath))
//        mPlayer = MediaPlayer.create(this@O2ChatActivity, uri)
//        mPlayer?.setVolume(1.0f, 1.0f)
//        mPlayer?.setOnCompletionListener {
//            XLog.debug("播音结束！")
//            adapter.stopAudioAnimation()
//        }
//        mPlayer?.start()
    }


    /**
     * 获取消息数据
     */
    private fun getPageData() {
        mPresenter.getConversation(conversationId)
        mPresenter.getMessage(page + 1, conversationId)
        //更新阅读时间
        mPresenter.readConversation(conversationId)
    }

    /**
     * 滚动消息到底部
     */
    private fun scroll2Bottom() {
        rv_o2_chat_messages.scrollToPosition(adapter.lastPosition())
    }

    /**
     * 发送消息
     */
    private fun sendTextMessage() {
        val text = et_o2_chat_input.text.toString()
        if (!TextUtils.isEmpty(text)) {
            et_o2_chat_input.setText("")
            newTextMessage(text)
        }
        //更新阅读时间
        mPresenter.readConversation(conversationId)
    }

    /**
     * 创建文本消息 并发送
     */
    private fun newTextMessage(text: String) {
        val time = DateHelper.now()
        val body = IMMessageBody(type = MessageType.text.key, body = text)
        val bodyJson = O2SDKManager.instance().gson.toJson(body)
        XLog.debug("body: $bodyJson")
        val uuid = UUID.randomUUID().toString()
        val message = IMMessage(uuid, conversationId, bodyJson,
                O2SDKManager.instance().distinguishedName, time, 1)
        adapter.addMessage(message)
        mPresenter.sendIMMessage(message)//发送到服务器
        scroll2Bottom()
    }

    /**
     * 创建表情消息
     */
    private fun newEmojiMessage(emoji: String) {
        val time = DateHelper.now()
        val body = IMMessageBody(type = MessageType.emoji.key, body = emoji)
        val bodyJson = O2SDKManager.instance().gson.toJson(body)
        XLog.debug("body: $bodyJson")
        val uuid = UUID.randomUUID().toString()
        val message = IMMessage(uuid, conversationId, bodyJson,
                O2SDKManager.instance().distinguishedName, time, 1)
        adapter.addMessage(message)
        mPresenter.sendIMMessage(message)//发送到服务器
        scroll2Bottom()
    }

    /**
     * 文件消息创建 并发送
     */
    private fun newAudioMessage(filePath: String, duration: String) {
        val time = DateHelper.now()
        val body = IMMessageBody(type = MessageType.audio.key, body = MessageBody.audio.body,
                fileTempPath = filePath, audioDuration = duration)
        val bodyJson = O2SDKManager.instance().gson.toJson(body)
        XLog.debug("body: $bodyJson")
        val uuid = UUID.randomUUID().toString()
        val message = IMMessage(uuid, conversationId, bodyJson,
                O2SDKManager.instance().distinguishedName, time, 1)
        adapter.addMessage(message)
        mPresenter.sendIMMessage(message)//发送到服务器
        scroll2Bottom()
    }

    /**
     * 图片消息 创建 并发送
     */
    private fun newImageMessage(filePath: String) {
        val time = DateHelper.now()
        val body = IMMessageBody(type = MessageType.image.key, body = MessageBody.image.body, fileTempPath = filePath)
        val bodyJson = O2SDKManager.instance().gson.toJson(body)
        XLog.debug("body: $bodyJson")
        val uuid = UUID.randomUUID().toString()
        val message = IMMessage(uuid, conversationId, bodyJson,
                O2SDKManager.instance().distinguishedName, time, 1)
        adapter.addMessage(message)
        mPresenter.sendIMMessage(message)//发送到服务器
        scroll2Bottom()
    }

    /**
     * 位置消息 创建并发送
     */
    private fun newLocationMessage(location: O2LocationActivity.LocationData) {
        val time = DateHelper.now()
        val body = IMMessageBody(type = MessageType.location.key, body = MessageBody.location.body,
                address = location.address, addressDetail = location.addressDetail,
                latitude = location.latitude, longitude = location.longitude)
        val bodyJson = O2SDKManager.instance().gson.toJson(body)
        XLog.debug("body: $bodyJson")
        val uuid = UUID.randomUUID().toString()
        val message = IMMessage(uuid, conversationId, bodyJson,
                O2SDKManager.instance().distinguishedName, time, 1)
        adapter.addMessage(message)
        mPresenter.sendIMMessage(message)//发送到服务器
        scroll2Bottom()
    }

    /**
     * 文件消息 创建并发送
     */
    private fun newFileMessage(filePath: String) {
        // 如果是图片 用图片消息发送
        val index = filePath.lastIndexOf(".")
        val extension = filePath.substring(index+1)
        if (FileExtensionHelper.isImageFromFileExtension(extension)) {
            newImageMessage(filePath)
        }else {
            val time = DateHelper.now()
            val body = IMMessageBody(type = MessageType.file.key, body = MessageBody.file.body, fileTempPath = filePath)
            val bodyJson = O2SDKManager.instance().gson.toJson(body)
            XLog.debug("body: $bodyJson")
            val uuid = UUID.randomUUID().toString()
            val message = IMMessage(uuid, conversationId, bodyJson,
                    O2SDKManager.instance().distinguishedName, time, 1)
            adapter.addMessage(message)
            mPresenter.sendIMMessage(message)//发送到服务器
            scroll2Bottom()
        }
    }


    ///////////////////// 距离监听 手机是否靠近耳朵
    private  var sensorManager: SensorManager? = null
    private  var sensor: Sensor? = null
    private fun sensorListen() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            if (sensor != null) {
                sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (O2MediaPlayerManager.instance().isHeadsetOn()) { //如果连接了耳机就不切换
            return
        }
        val value = event?.values?.get(0)
        if (value != null) {
            if (O2MediaPlayerManager.instance().isPlaying()) {
                if (value == sensor?.maximumRange) {
                    O2MediaPlayerManager.instance().changeToSpeaker()
                } else {
                    O2MediaPlayerManager.instance().changeToReceiver()
                }
            }else {
                if(value == sensor?.maximumRange){
                    O2MediaPlayerManager.instance().changeToSpeaker()
                }
            }
        }

    }

    /**
     * 接收到消息
     */
    private fun receiveMessage(message: IMMessage) {
        adapter.addMessage(message)
        scroll2Bottom()
        //更新阅读时间
        mPresenter.readConversation(conversationId)
    }


    var mReceiver: IMMessageReceiver? = null
    private fun registerBroadcast() {
        mReceiver = IMMessageReceiver()
        val filter = IntentFilter(O2IM.IM_Message_Receiver_Action)
        registerReceiver(mReceiver, filter)
    }


    inner class IMMessageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val body = intent?.getStringExtra(O2IM.IM_Message_Receiver_name)
            if (body != null && body.isNotEmpty()) {
                XLog.debug("接收到im消息, $body")
                try {
                    val message = O2SDKManager.instance().gson.fromJson<IMMessage>(body, IMMessage::class.java)
                    if (message.conversationId == conversationId) {
                        receiveMessage(message)
                    }
                } catch (e: Exception) {
                    XLog.error("", e)
                }

            }
        }

    }
}
