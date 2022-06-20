package net.zoneland.x.bpm.mobile.v1.zoneXBPM.flutter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIAssemblesData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIDistributeData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIWebServerData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.AuthenticationInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.CollectUnitData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import java.io.File
import java.io.IOException

/**
 * flutter 程序的容器
 * Created by fancyLou on 2022-06-16.
 * Copyright © 2022 o2android. All rights reserved.
 */
class FlutterConnectFragment: FlutterFragment(), MethodChannel.MethodCallHandler  {


    private var channel: MethodChannel? = null
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        initChannel(flutterEngine.dartExecutor.binaryMessenger)
    }

    // 初始化 消息通道
    private fun initChannel(messenger: BinaryMessenger) {
        channel = MethodChannel(messenger, FlutterO2Utils.nativeChannelName)
        channel?.setMethodCallHandler(this)
    }

    // 和 flutter 通信
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        XLog.debug("执行flutter通信")
        if (call.method == FlutterO2Utils.MethodNameO2Config) {
            val themeSuffix = FancySkinManager.instance().currentSkinSuffix()
            XLog.debug("theme:$themeSuffix")
            val map = HashMap<String, String>()
            if (themeSuffix != "blue") {
                map[FlutterO2Utils.parameterNameTheme] = "red"
            } else {
                map[FlutterO2Utils.parameterNameTheme] = "blue"
            }
            //user
            try {
                val user = AuthenticationInfoJson()
                user.id = O2SDKManager.instance().cId
                user.distinguishedName = O2SDKManager.instance().distinguishedName
                user.token = O2SDKManager.instance().zToken
                user.name = O2SDKManager.instance().cName
                val jsonUser = O2SDKManager.instance().gson.toJson(user)
                map[FlutterO2Utils.parameterNameUser] = jsonUser
            } catch (e: Exception) {
                XLog.error("$e")
            }
            //unit
            try {
                val unit = CollectUnitData()
                unit.name = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_UNIT_KEY, "")
                unit.centerContext =
                    O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_CONTEXT_KEY, "")
                unit.centerHost =
                    O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HOST_KEY, "")
                unit.centerPort = O2SDKManager.instance().prefs().getInt(O2.PRE_CENTER_PORT_KEY, 80)
                unit.httpProtocol = O2SDKManager.instance().prefs()
                    .getString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, "http")
                map[FlutterO2Utils.parameterNameUnit] = O2SDKManager.instance().gson.toJson(unit)
            } catch (e: Exception) {
                XLog.error("$e")
            }
            //centerServer
            try {
                val oldDataJson =
                    O2SDKManager.instance().prefs().getString(O2.PRE_ASSEMBLESJSON_KEY, "")
                val oldWebDataJson =
                    O2SDKManager.instance().prefs().getString(O2.PRE_WEBSERVERJSON_KEY, "")
                val data = O2SDKManager.instance().gson.fromJson<APIAssemblesData>(
                    oldDataJson,
                    APIAssemblesData::class.java
                )
                val webData = O2SDKManager.instance().gson.fromJson<APIWebServerData>(
                    oldWebDataJson,
                    APIWebServerData::class.java
                )
                val dis = APIDistributeData()
                dis.webServer = webData
                dis.assembles = data
                dis.tokenName = O2SDKManager.instance().tokenName() // 添加tokenName支持
                map[FlutterO2Utils.parameterNameCenterServer] =
                    O2SDKManager.instance().gson.toJson(dis)
            } catch (e: Exception) {
                XLog.error("$e")
            }
            result.success(map)
        } else if (call.method == FlutterO2Utils.MethodNameO2PickImage) {
            imagePicker(call, result)
        } else {
            XLog.error("没有实现当前方法, method:${call.method}")
            result.error("没有实现当前方法", "没有实现当前方法", "")
        }
    }

    private fun imagePicker(call: MethodCall, result: MethodChannel.Result) {
        var source = "gallery" // gallery,camera
        if (call.hasArgument("source")) {
            source = call.argument<String>("source") ?: "gallery"
            XLog.debug("有source： $source")
        }
        XLog.debug("打开图片选择器： $source")
        when (source) {
            "gallery" -> {
                openAlbum { file ->
                    backImagePicker(file, result)
                }
            }
            "camera" -> {
                checkCameraPermission { file ->
                    backImagePicker(file, result)
                }
            }
            else -> {
                result.error("参数错误", "参数错误", "")
            }
        }
    }

    private fun backImagePicker(file: String?, result: MethodChannel.Result) {
        val map = HashMap<String, String>()
        map[FlutterO2Utils.parameterNamePickerImageFile] = file ?: ""
        result.success(map)
    }

    // 选择图片
    private fun openAlbum(callback: (String?)->Unit) {
            PicturePickUtil().withAction(requireActivity())
                .forResult { files ->
                    if (files != null && files.isNotEmpty()) {
                        callback(files[0])
                    } else {
                        callback(null)
                    }
                }
    }

    private fun checkCameraPermission(callback: (String?)->Unit) {
        PermissionRequester(requireActivity()).request(Manifest.permission.CAMERA)
            .o2Subscribe {
                onNext {  (granted, _, _) ->
                    if (!granted){
                        O2DialogSupport.openAlertDialog(requireActivity(), getString(R.string.dialog_msg_camera_need_permission), { permissionSetting() })
                        callback(null)
                    } else {
                        openCamera(callback)
                    }
                }
                onError { e, _ ->
                    XLog.error("", e)
                    callback(null)
                }
            }

    }
    private fun permissionSetting() {
        val packageUri = Uri.parse("package:${requireActivity().packageName}")
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
    }
    private var cameraImagePath: String? = null
    private  val TAKE_FROM_CAMERA_CODE = 1004
    private var pickCameraBack: ((String?)->Unit)? = null

    // 拍照
    private fun openCamera(callback: (String?)->Unit) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    FileExtensionHelper.createImageFile(requireActivity())
                } catch (ex: IOException) {
                    XToast.toastShort(requireActivity(), getString(R.string.message_camera_file_create_error))
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    pickCameraBack = callback
                    cameraImagePath = it.absolutePath
                    val photoURI = FileUtil.getUriFromFile(requireActivity(), it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, TAKE_FROM_CAMERA_CODE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_FROM_CAMERA_CODE) {
            //拍照
            XLog.debug("拍照////返回 ")
            if (pickCameraBack != null) {
                pickCameraBack?.invoke(cameraImagePath)
                pickCameraBack = null
            }
        }
    }

}