package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils

import android.content.Context
import android.widget.Toast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2App
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R

/**
 * 自定义toast 工具类
 *
 * Created by FancyLou on 2016/8/18.
 */
object XToast {

    private var toast: Toast? = null
    private var toastLong: Toast? = null

    fun toastShort(message: String) {
        toastShort(null, message)
    }
    fun toastShort(context: Context?, message: String) {
        if (toast == null) {
            toast =  Toast.makeText(O2App.instance, null, Toast.LENGTH_SHORT)
        }
        toast?.setText(message)
        toast?.show()
    }
    fun toastShort(messageRes: Int) {
        toastShort(null, messageRes)
    }
    fun toastShort(context: Context?, messageRes: Int) {
        if (toast == null) {
            toast =  Toast.makeText(O2App.instance, null, Toast.LENGTH_SHORT)
        }
        toast?.setText(messageRes)
        toast?.show()
    }

    fun toastLong(message: String) {
        toastLong(null, message)
    }
    fun toastLong(context: Context?, message: String) {
        if (toastLong == null) {
            toastLong =  Toast.makeText(O2App.instance, null, Toast.LENGTH_LONG)
        }
        toastLong?.setText(message)
        toastLong?.show()
//        context?.let {
//            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
//            ToastUtil(it).Long(message).setToastBackgroundDrawable(Color.WHITE,
//                    FancySkinManager.instance().getDrawable(it, R.drawable.toast_background)!!).show()
//            }
    }
    fun toastLong(messageRes: Int) {
        toastLong(null, messageRes)
    }
    fun toastLong(context: Context?, messageRes: Int) {
        if (toastLong == null) {
            toastLong =  Toast.makeText(O2App.instance, null, Toast.LENGTH_LONG)
        }
        toastLong?.setText(messageRes)
        toastLong?.show()
    }
//        ToastUtil(context).Long(messageRes).setToastBackgroundDrawable(Color.WHITE,
//                FancySkinManager.instance().getDrawable(context, R.drawable.toast_background)!!).show()
//    }

    fun getXToast(context: Context): Toast {
        val toast = Toast.makeText(context, null, Toast.LENGTH_SHORT)
        toast.setText(R.string.exit_click_double_back_btn)
        return toast
//        return ToastUtil(context).Short(R.string.exit_click_double_back_btn).setToastBackgroundDrawable(Color.WHITE,
//                FancySkinManager.instance().getDrawable(context, R.drawable.toast_background)!!).toast
    }
}
