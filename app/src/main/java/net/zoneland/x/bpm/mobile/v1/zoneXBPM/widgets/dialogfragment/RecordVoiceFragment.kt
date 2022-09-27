package net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialogfragment


import android.app.AlertDialog
import android.graphics.drawable.BitmapDrawable
import android.media.AudioFormat
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.zlw.main.recorderlib.RecordManager
import com.zlw.main.recorderlib.recorder.RecordConfig
import com.zlw.main.recorderlib.recorder.RecordHelper
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener
import kotlinx.android.synthetic.main.dialog_fragment_record_voice.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.inVisible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.util.*

/**
 * Created by fancyLou on 2022-09-27.
 * Copyright © 2022 o2android. All rights reserved.
 */
class RecordVoiceFragment: DialogFragment(), View.OnTouchListener {


    interface OnBackRecordResultListener {
        fun onBack(voiceFilePath: String, voiceDuringTime: Long)
    }

    fun setResultListener(listener: OnBackRecordResultListener) {
        this.resultListener = listener
    }

    private var resultListener: OnBackRecordResultListener? = null

    //录音服务
    private var isAudioRecordCancel = false
    private var startY: Float = 0f
    private var isCancelRecord = false
    // 录音时间
    private var audioRecordTime = 0L
    // 录音文件路径
    private var voiceResultPath = ""
    //录音计时器
//    private val audioCountDownTimer: CountDownTimer by lazy {
//        object : CountDownTimer(60 * 1000, 1000) {
//            override fun onFinish() {
//                XLog.debug("倒计时结束！")
//                endRecordAudio()
//            }
//
//            override fun onTick(millisUntilFinished: Long) {
//                val sec = ((millisUntilFinished + 15) / 1000)
//                audioRecordTime = 60 - sec
//                activity?.runOnUiThread {
//                    val times = if (audioRecordTime > 9) {
//                        "00:$audioRecordTime"
//                    } else {
//                        "00:0$audioRecordTime"
//                    }
//                    tv_record_voice_speak_duration.text = times
//                }
//                XLog.debug("倒计时还剩余：$sec 秒")
//            }
//
//        }
//    }

    private val timer: Timer by lazy {
        Timer()
    }
    private val timerTask: TimerTask by lazy {
        object : TimerTask() {
            override fun run() {
                audioRecordTime += 1
                activity?.runOnUiThread {
                    val minu = audioRecordTime / 60
                    val seconds = audioRecordTime % 60
                    val minuStr = if (minu > 9) {
                        "$minu"
                    } else {
                        "0$minu"
                    }
                    val secondStr = if (seconds > 9) {
                        "$seconds"
                    } else {
                        "0$seconds"
                    }
                    tv_record_voice_speak_duration.text = "$minuStr:$secondStr"
                }
                XLog.debug("录音时间：$audioRecordTime 秒")
            }

        }
    }

    private fun startCountTimer() {
        timer.schedule(timerTask, 1000, 1000)
    }
    private fun endCountTimer() {
        timer.cancel()
    }

    private var tvPrompt: TextView? = null
    private var ivLoadGif: GifImageView? = null
    private var recordingDialog: AlertDialog? = null
    private fun recordingDialog() : AlertDialog {
        val dialogBuilder = AlertDialog.Builder(activity, R.style.DialogManage)
        dialogBuilder.setCancelable(false)
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_voice_speak, null)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dWin = dialog?.window
        dWin?.setDimAmount(0.8f)
        val lp = dWin?.attributes
        lp?.gravity = Gravity.BOTTOM
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        dWin?.attributes = lp
        return inflater.inflate(R.layout.dialog_fragment_record_voice, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(null)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAudioRecord()
        recordingDialog = recordingDialog()
        image_record_voice_speak_btn.setOnTouchListener(this)
        image_record_close.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

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
            }
            MotionEvent.ACTION_CANCEL -> {
                XLog.debug("取消了................录音")
                cancelRecordAudio()
                recordingDialog?.dismiss()
            }
        }
        return true
    }


    /**
     * 初始化录音相关对象
     */
    private fun initAudioRecord() {
        RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.MP3)
        RecordManager.getInstance().changeRecordConfig(RecordManager.getInstance().recordConfig.setSampleRate(16000))
        RecordManager.getInstance().changeRecordConfig(
            RecordManager.getInstance().recordConfig.setEncodingConfig(
                AudioFormat.ENCODING_PCM_8BIT))
        RecordManager.getInstance().changeRecordDir(FileExtensionHelper.getXBPMTempFolder(activity) + File.separator)
        RecordManager.getInstance().setRecordStateListener(object : RecordStateListener {
            override fun onError(error: String?) {
                XLog.error("录音错误, $error")
            }

            override fun onStateChange(state: RecordHelper.RecordState?) {
                when (state) {
                    RecordHelper.RecordState.IDLE -> XLog.debug("录音状态， 空闲状态")
                    RecordHelper.RecordState.RECORDING -> {
                        XLog.debug("录音状态， 录音中")
//                        audioCountDownTimer.start()
                        startCountTimer()
                    }
                    RecordHelper.RecordState.PAUSE -> XLog.debug("录音状态， 暂停中")
                    RecordHelper.RecordState.STOP -> XLog.debug("录音状态， 正在停止")
                    RecordHelper.RecordState.FINISH -> XLog.debug("录音状态， 录音流程结束（转换结束）")
                }

            }
        })
        RecordManager.getInstance().setRecordResultListener { result ->
            if (result == null) {
                activity?.runOnUiThread { XToast.toastShort(activity, "录音失败！") }
            } else {
                XLog.debug("录音结束 返回结果 ${result.path} ， 是否取消：$isAudioRecordCancel, 录音时间：$audioRecordTime")
                if (audioRecordTime < 1) {
                    activity?.runOnUiThread {
                        XToast.toastShort(activity, getString(R.string.message_im_audio_too_short))
                    }
                } else {
                    if (!isAudioRecordCancel) {
                        XLog.info("录音完成 ${result.path} $audioRecordTime")
                        voiceResultPath = result.path
                        showReloadAndCompletedButton()
                    }
                }
            }
        }

    }

    /**
     * 开始录音
     */
    private fun startRecordAudio() {
        XLog.debug("开始录音。。。。")
        audioRecordTime = 0L
        voiceResultPath = "" // 清除路径地址
        RecordManager.getInstance().start()
        tv_record_voice_speak_title.text = resources.getText(R.string.activity_im_audio_speak_cancel)
    }

    /**
     * 结束录音
     */
    private fun endRecordAudio() {
        XLog.debug("结束录音。。。。")
//        audioCountDownTimer.cancel()
        endCountTimer()
        RecordManager.getInstance().stop()
        tv_record_voice_speak_title.text = resources.getText(R.string.activity_im_audio_speak)
        tv_record_voice_speak_duration.text = ""
    }

    /**
     * 取消录音
     */
    private fun cancelRecordAudio() {
        XLog.debug("取消录音。。。。。")
        isAudioRecordCancel = true
//        audioCountDownTimer.cancel()
        endCountTimer()
        RecordManager.getInstance().stop()
        tv_record_voice_speak_title.text = resources.getText(R.string.activity_im_audio_speak)
        tv_record_voice_speak_duration.text = ""
    }

    /**
     * 录音结束显示完成按钮
     */
    private fun showReloadAndCompletedButton() {
        image_record_voice_speak_btn.inVisible()
        btn_record_voice_reload.visible()
        btn_record_voice_reload.setOnClickListener {
            hideReloadAndCompletedButton()
            voiceResultPath = ""
            audioRecordTime = 0L
        }
        btn_record_voice_completed.visible()
        btn_record_voice_completed.setOnClickListener {
            resultListener?.onBack(voiceResultPath, audioRecordTime)
            dismissAllowingStateLoss()
        }
    }

    /**
     *
     */
    private fun hideReloadAndCompletedButton() {
        btn_record_voice_reload.inVisible()
        btn_record_voice_completed.inVisible()
        image_record_voice_speak_btn.visible()
    }
}