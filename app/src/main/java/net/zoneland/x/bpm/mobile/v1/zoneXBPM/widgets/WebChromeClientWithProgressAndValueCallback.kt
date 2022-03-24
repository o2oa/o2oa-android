package net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import org.jetbrains.anko.dip
import java.io.File
import java.io.IOException

/**
 * Created by fancyLou on 2019-04-29.
 * Copyright © 2019 O2. All rights reserved.
 */


class WebChromeClientWithProgressAndValueCallback private constructor (val activity: Activity?) : WebChromeClient() {

    companion object {
        const val TAKE_FROM_CAMERA_KEY = 1099
        const val TAKE_FROM_PICTURES_KEY = 1098
        fun with(activity: Activity): WebChromeClientWithProgressAndValueCallback =
                WebChromeClientWithProgressAndValueCallback(activity)
        fun with(fragment: Fragment): WebChromeClientWithProgressAndValueCallback =
                WebChromeClientWithProgressAndValueCallback(fragment.activity)
    }

    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null

    var progressBar: ProgressBar? = null

    var onO2ReceivedTitle: ((String) -> Unit)? = null


    init {
        progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal)
        progressBar?.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, activity?.dip(2)?:10, Gravity.TOP)
        val drawable = ContextCompat.getDrawable(activity!!, R.drawable.web_view_progress_bar)
        if (drawable != null) {
            progressBar?.progressDrawable = drawable
        }
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        getWebviewTitle(view)
    }

    private fun getWebviewTitle(view: WebView?) {
        val list = view?.copyBackForwardList()
        val current = list?.currentItem
        if (current?.title != null && onO2ReceivedTitle != null) {
            onO2ReceivedTitle?.invoke(current.title)
        }
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        if (newProgress == 100) {
            progressBar?.visibility = View.GONE
        } else {
            if (progressBar?.visibility == View.GONE)
                progressBar?.visibility = View.VISIBLE
            progressBar?.progress = newProgress
        }
        super.onProgressChanged(view, newProgress)
    }


    // For Android >= 5.0
    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
        XLog.debug("选择文件 5。0。。。。。。。。。。。。。。。。。")
        uploadMessageAboveL = filePathCallback
        showPictureChooseMenu()
        return true
    }

    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)
        activity?.finish()
    }

    /**
     * 接收activity返回的数据
     * @return true已经处理 false没有处理
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                 TAKE_FROM_CAMERA_KEY -> {
                    //拍照
                    XLog.debug("拍照//// ")
                    if (uploadMessageAboveL != null && cameraImageUri!=null)   {
                        val list = ArrayList<Uri>()
                        list.add(cameraImageUri!!)
                        uploadMessageAboveL?.onReceiveValue(list.toTypedArray())
                    }
                    return true
                }
            }
        }
        return false
    }


    private fun showPictureChooseMenu() {
        if (activity != null) {
            BottomSheetMenu(activity)
                    .setTitle("上传照片")
                    .setItem("从相册选择", ContextCompat.getColor(activity, R.color.z_color_text_primary)) {
                        takeFromPictures()
                    }
                    .setItem("拍照", ContextCompat.getColor(activity, R.color.z_color_text_primary)) {
                        takeFromCamera()
                    }
                    .setCancelButton("取消", ContextCompat.getColor(activity, R.color.z_color_text_hint)) {
                        XLog.debug("取消。。。。。")
                        if (uploadMessageAboveL!=null) {
                            uploadMessageAboveL?.onReceiveValue(null)
                        }
                    }
                    .show()
        }else {
            XLog.error("activity 不存在， 无法打开dialog菜单!")
        }

    }


    private fun takeFromPictures() {
        if (activity != null) {
            PicturePickUtil().withAction(activity)
                .forResult { files ->
                    if (files!=null && files.isNotEmpty()) {
                        XLog.debug("照片 path:${files[0]}")
                        if (uploadMessageAboveL != null)   {
                            val uri = FileUtil.getUriFromFile(activity, File(files[0]))
                            val uriList = ArrayList<Uri>()
                            uriList.add(uri)
                            uploadMessageAboveL?.onReceiveValue(uriList.toTypedArray())
                        }
                    }
                }

        }else {
            XLog.error("activity 不存在， 无法打开图片选择器!")
        }
    }

    private fun takeFromCamera() {
        if (activity != null) {
            PermissionRequester(activity).request(Manifest.permission.CAMERA)
                    .o2Subscribe {
                        onNext { (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
                            XLog.info("granted:$granted , shouldShowRequest:$shouldShowRequestPermissionRationale, denied:$deniedPermissions")
                            if (!granted) {
                                O2DialogSupport.openAlertDialog(activity, "非常抱歉，相机权限没有开启，无法使用相机！")
                            } else {
                                openCamera()
                            }
                        }
                    }
        }else {
            XLog.error("activity 不存在， 无法打开拍照功能!")
        }
    }
    private fun openCamera() {
        if (activity != null) {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            //return-data false 不是直接返回拍照后的照片Bitmap 因为照片太大会传输失败
//            intent.putExtra("return-data", false)
//            //改用Uri 传递
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
//            intent.putExtra("noFaceDetection", true)
//            activity.startActivityForResult(intent, TAKE_FROM_CAMERA_KEY)

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(activity.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        FileExtensionHelper.createImageFile(activity)
                    } catch (ex: IOException) {
                        XToast.toastShort(activity, activity.getString(R.string.message_camera_file_create_error))
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        cameraImageUri = FileUtil.getUriFromFile(activity, it)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                        activity.startActivityForResult(takePictureIntent, TAKE_FROM_CAMERA_KEY)
                    }
                }
            }
        }else {
            XLog.error("activity 不存在， 无法打开拍照功能!")
        }
    }


}