package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import com.wugang.activityresult.library.ActivityResult

/**
 * Created by fancyLou on 2022-03-24.
 * Copyright © 2022 o2android. All rights reserved.
 */
class PicturePickUtil {

    companion object {
        const val PICK_PHOTO = 1024
        const val PICK_PHOTO_CALLBACK_KEY = "1024"
        const val MULTIPLE_INTENT_KEY = "MultipleIntentKey"
        const val MODE_INTENT_KEY = "modeintentkey"
    }

    private var activity: Activity? = null
    private var multiple: Boolean = false
    private var mode: PickTypeMode = PickTypeMode.Picture

    /**
     * 导入activity
     */
    fun withAction(activity: Activity) : PicturePickUtil {
        this.activity = activity
        return this
    }

    /**
     * 是否允许多选
     */
    fun allowMultiple(multiple: Boolean): PicturePickUtil  {
        this.multiple = multiple
        return this
    }

    /**
     * 选择类型
     */
    fun setMode(mode: PickTypeMode): PicturePickUtil {
        this.mode = mode
        return this
    }

    /**
     * 返回选择的结果
     */
    fun forResult(callback: (ArrayList<String>?)->Unit) {
        if (activity != null) {
            val bundle = Bundle()
            bundle.putInt(MODE_INTENT_KEY, mode.v)
            bundle.putBoolean(MULTIPLE_INTENT_KEY, multiple)
            ActivityResult.of(activity!!)
                .className(PicturePickActivity::class.java)
                .params(bundle)
                .greenChannel()
                .forResult { resultCode, intent ->
                    if (resultCode == Activity.RESULT_OK && intent != null) {
                        val imagePaths = intent.getStringArrayListExtra(PICK_PHOTO_CALLBACK_KEY)
                        if (imagePaths != null && imagePaths.isNotEmpty()) {
                            callback(imagePaths)
                            return@forResult
                        }
                    }
                }
        }
        callback(null)
    }


}