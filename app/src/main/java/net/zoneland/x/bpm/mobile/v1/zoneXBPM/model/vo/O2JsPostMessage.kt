package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.vo

/**
 * Created by fancyLou on 2019-05-06.
 * Copyright © 2019 O2. All rights reserved.
 */



class O2JsPostMessage<T> (
        var callback: String?,
        var type: String? ,
        var data:T?

)

class O2UtilDatePickerMessage(var value: String?,
                       var startDate: String?,
                       var endDate: String?)

class O2UtilNavigationMessage(var title: String?)

/**
 * 第三方 app 打开的的schema
 */
class O2UtilNavigationOpenOtherAppMessage(var schema: String?)


class O2BizComplexPickerMessage(
        var pickMode: ArrayList<String>?,
        var topList: ArrayList<String>?,
        var multiple: Boolean?,
        var maxNumber: Int?,
        var pickedIdentities: ArrayList<String>?,
        var pickedDepartments: ArrayList<String>?,
        var pickedGroups: ArrayList<String>?,
        var pickedUsers: ArrayList<String>?,
        var duty: ArrayList<String>?,
        var orgType: String?
)

class O2BizIdentityPickerMessage(
        var topList: ArrayList<String>?,
        var multiple: Boolean?,
        var maxNumber: Int?,
        var pickedIdentities: ArrayList<String>?,
        var duty: ArrayList<String>?
)

class O2BizUnitPickerMessage(
        var topList: ArrayList<String>?,
        var multiple: Boolean?,
        var maxNumber: Int?,
        var pickedDepartments: ArrayList<String>?,
        var orgType: String?
)

class O2BizGroupPickerMessage(
        var multiple: Boolean?,
        var maxNumber: Int?,
        var pickedGroups: ArrayList<String>?
)


class O2BizPersonPickerMessage(
        var multiple: Boolean?,
        var maxNumber: Int?,
        var pickedUsers: ArrayList<String>?
)
// 预览文件
class O2BizPreviewDocMessage(
        var url: String?,
        var fileName: String?
)

// 流程上传附件
class O2TaskUploadAttachmentMessage(
        var site: String?,
        var param: String?
)

// 流程附件替换
class O2TaskReplaceAttachmentMessage(
        var attachmentId: String?,
        var site: String?,
        var param: String?
)

// 流程附件下载
class O2TaskDownloadAttachmentMessage(
        var attachmentId: String?,
)
// 打开正文的url
class O2TaskOpenDocumentMessage(
        var url: String?,
)
// 打开cms文档
class O2OpenCmsDocMessage(
        var docId: String?,
        var title: String?,
        var options: Map<String, Any>?
)

class O2OpenTaskCenterMessage(
        var type: String
)