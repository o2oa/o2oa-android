package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils

import android.content.Context
import android.graphics.Color
import android.widget.Toast
import com.xiaomi.push.it

import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R

/**
 * 自定义toast 工具类
 *
 * Created by FancyLou on 2016/8/18.
 */
object XToast {

    fun toastShort(context: Context?, message: String) {

        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
//            ToastUtil(it).Short(message).setToastBackgroundDrawable(Color.WHITE,
//                    FancySkinManager.instance().getDrawable(it, R.drawable.toast_background)!!).show()
        }

    }

    fun toastShort(context: Context?, messageRes: Int) {
        context?.let {
            Toast.makeText(it, messageRes, Toast.LENGTH_SHORT).show()
//            ToastUtil(it).Short(messageRes).setToastBackgroundDrawable(Color.WHITE,
//                    FancySkinManager.instance().getDrawable(it, R.drawable.toast_background)!!).show()
        }
    }

    fun toastLong(context: Context?, message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
//            ToastUtil(it).Long(message).setToastBackgroundDrawable(Color.WHITE,
//                    FancySkinManager.instance().getDrawable(it, R.drawable.toast_background)!!).show()
//            }
    }

    fun toastLong(context: Context, messageRes: Int) {
        Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show()
    }
//        ToastUtil(context).Long(messageRes).setToastBackgroundDrawable(Color.WHITE,
//                FancySkinManager.instance().getDrawable(context, R.drawable.toast_background)!!).show()
    }

    fun getXToast(context: Context): Toast {
        return Toast.makeText(context, R.string.exit_click_double_back_btn, Toast.LENGTH_SHORT)
//        return ToastUtil(context).Short(R.string.exit_click_double_back_btn).setToastBackgroundDrawable(Color.WHITE,
//                FancySkinManager.instance().getDrawable(context, R.drawable.toast_background)!!).toast
    }
}
