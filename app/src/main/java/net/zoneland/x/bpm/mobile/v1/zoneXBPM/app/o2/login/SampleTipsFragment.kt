package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.login

import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_sample_tips.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.saveToAlbum


/**
 * A simple [Fragment] subclass.
 * Use the [SampleTipsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SampleTipsFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.customStyleDialogStyle) //NO_FRAME就是dialog无边框，0指的是默认系统Theme
        XLog.debug("onCreate ..........1111")
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setGravity(Gravity.CENTER)
        window?.setWindowAnimations(R.style.DialogEmptyAnimation)//取消过渡动画 , 使DialogSearch的出现更加平滑
        XLog.debug("onStart ..........")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        XLog.debug("onCreateView ..........")
        return inflater.inflate(R.layout.fragment_sample_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        XLog.debug("onViewCreated .........1.")
//        dialog?.setCancelable(false)
//        dialog?.setCanceledOnTouchOutside(false)
//        dialog?.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                return@OnKeyListener true
//            }
//            false
//        })

        // 关闭
        ib_sample_tips_fragment_close.setOnClickListener {
            dismissAllowingStateLoss()
        }
        image_sample_tips_fragment_wx_code.setOnLongClickListener {
            saveMPWeixinImageToAlbum()
            true
        }

        val style = SpannableStringBuilder("关注“浙江兰德网络”微信公众号，点击最右侧“体验环境”再点击“体验账号”获取。")
        style.setSpan(ForegroundColorSpan(Color.RED), 3, 9, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        style.setSpan(ForegroundColorSpan(Color.RED), 22, 26, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        style.setSpan(ForegroundColorSpan(Color.RED), 31, 35, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tv_sample_tips_fragment_wx_name.text = style
        val moreStyle = SpannableStringBuilder("更多案例体验，请访问O2OA官网www.o2oa.net，点击菜单“立即体验”。")
        moreStyle.setSpan(ForegroundColorSpan(Color.RED), 16, 28, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        moreStyle.setSpan(ForegroundColorSpan(Color.RED), 34, 38, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tv_sample_tips_fragment_more.text = moreStyle
        XLog.debug("onViewCreated .........2.")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SampleTipsFragment.
         */
        @JvmStatic
        fun newInstance() =
            SampleTipsFragment()
    }



    // 保存微信公众号的二维码到相册
    private fun saveMPWeixinImageToAlbum() {
        if (activity != null) {
            val bitmap = BitmapFactory.decodeResource(activity!!.resources, R.mipmap.pic_code_weixin)
            bitmap.saveToAlbum(activity!!, "o2oa_mp_weixin.png", null)
            XToast.toastShort(activity, "成功保存到相册")
        }
    }

}