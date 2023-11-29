package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.launch

import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenterImpl
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.realm.RealmDataService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.CustomStyleData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.portal.PortalData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo.AppItemOnlineVo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.Base64ImageUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.O2FileDownloadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancy on 2017/6/6.
 */


class LaunchPresenter : BasePresenterImpl<LaunchContract.View>(), LaunchContract.Presenter {


    val service: RealmDataService by lazy { RealmDataService() }

    override fun downloadConfig() {
        Observable.create<Boolean> { sub->
            try {
                val style = O2SDKManager.instance().prefs().getString(O2CustomStyle.CUSTOM_STYLE_JSON_KEY, "")
                if (TextUtils.isEmpty(style)) {
                    XLog.error("配置内容为空！")
                }else {
                    val data = O2SDKManager.instance().gson.fromJson<CustomStyleData>(style, CustomStyleData::class.java)
                    if (data != null) {
                        val images = data.images
                        val portalList = data.portalList
                        val nativeAppList = data.nativeAppList
                        storageIndexPageInfo(data)
                        storageImages(images)
                        storagePortalList(portalList)
                        storageNativeList(nativeAppList)
                    }
                }
                sub.onNext(true)
            } catch (e: Exception) {
                sub.onError(e)
            }

            sub.onCompleted()
        }.subscribeOn(Schedulers.io())
                .o2Subscribe {
                    onNext {
                        XLog.info("配置文件设置成功！")
                    }
                    onError { e, _ ->
                        XLog.error("配置文件设置失败", e)
                    }
                }


    }
    private fun storageNativeList(nativeAppList: List<AppItemOnlineVo>) {
        service.deleteALlNativeApp().subscribeOn(Schedulers.immediate()).subscribe {
            service.saveNativeList(nativeAppList).subscribe()
        }
    }

    private fun storagePortalList(portalList: List<PortalData>) {
        service.deleteAllPortal().subscribeOn(Schedulers.immediate()).subscribe {
            service.savePortalList(portalList).subscribe()
        }
    }

    private fun storageImages(images: List<CustomStyleData.ImageValue>) {
        images.map { image ->
            val imageUrlPath = image.path
            if (!TextUtils.isEmpty(imageUrlPath)) { // 新的  url 方式展现图片
                val downloadUrl = APIAddressHelper.instance().getO2WebUrl(imageUrlPath)
                webImageUrl(image.name, downloadUrl)
            } else {
                // 老的 base64
                val base64 = image.value
                val path = baseImageLocalPath(image.name)
                if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(base64)) {
                    val result = Base64ImageUtil.generateImage(path, base64)
                    XLog.info("generate image result: $result, path: $path")
                }
            }
        }
    }

    private fun webImageUrl(name: String, downloadUrl: String) {
        val prefs = when (name) {
            O2CustomStyle.IMAGE_KEY_LAUNCH_LOGO -> O2CustomStyle.IMAGE_KEY_LAUNCH_LOGO_URL
            O2CustomStyle.IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_FOCUS -> O2CustomStyle.IMAGE_KEY_LAUNCH_LOGO_URL
            O2CustomStyle.IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_BLUR -> O2CustomStyle.IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_BLUR_URL
            O2CustomStyle.IMAGE_KEY_LOGIN_AVATAR -> O2CustomStyle.IMAGE_KEY_LOGIN_AVATAR_URL
            O2CustomStyle.IMAGE_KEY_PEOPLE_AVATAR_DEFAULT -> O2CustomStyle.IMAGE_KEY_PEOPLE_AVATAR_DEFAULT_URL
            O2CustomStyle.IMAGE_KEY_PROCESS_DEFAULT -> O2CustomStyle.IMAGE_KEY_PROCESS_DEFAULT_URL
            O2CustomStyle.IMAGE_KEY_SETUP_ABOUT_LOGO -> O2CustomStyle.IMAGE_KEY_SETUP_ABOUT_LOGO_URL
            O2CustomStyle.IMAGE_KEY_APPLICATION_TOP -> O2CustomStyle.IMAGE_KEY_APPLICATION_TOP_URL
            else -> ""
        }
        if (prefs.isNotEmpty()) {
            O2SDKManager.instance().prefs().edit {
                putString(prefs, downloadUrl)
            }
        }
    }
    private fun baseImageLocalPath(name: String): String? {
        return when (name) {
            O2CustomStyle.IMAGE_KEY_LAUNCH_LOGO -> {
                O2CustomStyle.launchLogoImagePath(mView?.getContext())
            }
            O2CustomStyle.IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_FOCUS -> {
                O2CustomStyle.indexMenuLogoFocusImagePath(mView?.getContext())
            }
            O2CustomStyle.IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_BLUR -> {
                O2CustomStyle.indexMenuLogoBlurImagePath(mView?.getContext())
            }
            O2CustomStyle.IMAGE_KEY_LOGIN_AVATAR -> {
                O2CustomStyle.loginAvatarImagePath(mView?.getContext())
            }
//            O2CustomStyle.IMAGE_KEY_PEOPLE_AVATAR_DEFAULT -> {
//                O2CustomStyle.peopleAvatarImagePath(mView?.getContext())
//            }
//            O2CustomStyle.IMAGE_KEY_PROCESS_DEFAULT -> {
//                O2CustomStyle.processDefaultImagePath(mView?.getContext())
//            }
            O2CustomStyle.IMAGE_KEY_SETUP_ABOUT_LOGO -> {
                O2CustomStyle.setupAboutImagePath(mView?.getContext())
            }
            O2CustomStyle.IMAGE_KEY_APPLICATION_TOP -> {
                O2CustomStyle.applicationTopImagePath(mView?.getContext())
            }
            else -> ""
        }
    }

    private fun downloadImageToLocal(filePath: String, downloadUrl: String) {
        XLog.info("下载图片，$downloadUrl 到 $filePath")
        O2FileDownloadHelper.download(downloadUrl, filePath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .o2Subscribe {
                onNext {
                    XLog.info("下载完成！")
                }
                onError { e, _ ->
                    XLog.error("", e)
                }
            }
    }
    private fun storageIndexPageInfo(data: CustomStyleData?) {
        O2SDKManager.instance().prefs().edit {
            putString(O2CustomStyle.INDEX_TYPE_PREF_KEY, data?.indexType
                    ?: O2CustomStyle.INDEX_TYPE_DEFAULT)
            putString(O2CustomStyle.INDEX_ID_PREF_KEY, data?.indexPortal ?: "")
            putBoolean(O2CustomStyle.CUSTOM_STYLE_SIMPLE_MODE_PREF_KEY, data?.simpleMode ?: false)
            putStringSet(O2CustomStyle.CUSTOM_STYLE_INDEX_PAGES_KEY, data?.appIndexPages?.toSet())
            putStringSet(O2CustomStyle.CUSTOM_STYLE_INDEX_FILTER_PROCESS_KEY, data?.processFilterList?.toSet())
            putStringSet(O2CustomStyle.CUSTOM_STYLE_INDEX_FILTER_CATEGORY_KEY, data?.cmsCategoryFilterList?.toSet())
            putBoolean(O2CustomStyle.CUSTOM_STYLE_SILENCE_GRAY_PREF_KEY, data?.needGray ?: false)
            putBoolean(O2CustomStyle.CUSTOM_STYLE_SYSTEM_MESSAGE_CAN_CLICK_KEY, data?.systemMessageCanClick ?: true)
            putString(O2CustomStyle.CUSTOM_STYLE_APP_EXIT_ALERT_KEY, data?.appExitAlert ?: "")
            putString(O2CustomStyle.CUSTOM_STYLE_CONTACT_PERMISSION_PREF_KEY, data?.contactPermissionView ?: O2CustomStyle.CUSTOM_STYLE_CONTACT_PERMISSION_DEFAULT)
        }
    }


}