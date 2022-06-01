package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan


/**
 * Created by fancyLou on 2022-05-20.
 * Copyright © 2022 o2android. All rights reserved.
 */

/**
 * v3 版本 文件对象
 */
sealed class CloudFileV3Data(open var id: String, open var name: String) {

    // 文件对象
    class FileItem(override var id: String, override var name: String,
                   var createTime: String,
                   var updateTime: String,
                   var isAdmin: Boolean = false, // 是否管理员
                   var isEditor: Boolean = false, // 是否编辑者
                   var isCreator: Boolean = false, // 是否创建着
                   var person: String,
                   var extension: String,
                   var contentType: String,
                   var type: String, // 文件所属分类.
                   var length: Long,
                   var folder: String, // 文件所属目录。
                   var zoneId: String, // 共享区ID。
                   var originFile: String, // 真实文件id.
                   var lastUpdateTime: String,
                   var lastUpdatePerson: String,
                   var status: String // 正常|已删除
                   ): CloudFileV3Data(id, name)

    // 文件夹对象
    class FolderItem(override var id: String, override var name: String,
                     var createTime: String,
                     var updateTime: String,
                     var lastUpdatePerson: String,
                     var lastUpdateTime: String,
                     var person: String,
                     var superior: String, // 上级目录ID
                     var zoneId: String, // 共享区ID
                     var attachmentCount: Int, // 附件数量
                     var folderCount: Int, // 目录数量
                     var isAdmin: Boolean = false, // 是否管理员
                     var isEditor: Boolean = false, // 是否编辑者
                     var isCreator: Boolean = false, // 是否创建着
                     var status: String // 正常|已删除
                     ): CloudFileV3Data(id, name)


}

// 文件夹对象 V3版本
data class FolderV3Json(var id: String,
                        var name: String,
                            var createTime: String,
                            var updateTime: String,
                            var lastUpdatePerson: String,
                            var lastUpdateTime: String,
                            var person: String,
                            var superior: String, // 上级目录ID
                            var zoneId: String, // 共享区ID
                            var attachmentCount: Int, // 附件数量
                            var folderCount: Int, // 目录数量
                            var isAdmin: Boolean = false, // 是否管理员
                            var isEditor: Boolean = false, // 是否编辑者
                            var isCreator: Boolean = false, // 是否创建着
                            var status: String // 正常|已删除
) {
    //v3版本
    fun copyToVO3(): CloudFileV3Data.FolderItem {
        return CloudFileV3Data.FolderItem(id, name, createTime, updateTime, lastUpdatePerson, lastUpdateTime, person, superior, zoneId, attachmentCount, folderCount, isAdmin, isEditor, isCreator, status)
    }
}

// 文件对象 V3版本
data class FileV3Json(var id: String,
                      var name: String,
                    var createTime: String,
                    var updateTime: String,
                    var isAdmin: Boolean = false, // 是否管理员
                    var isEditor: Boolean = false, // 是否编辑者
                    var isCreator: Boolean = false, // 是否创建着
                    var person: String,
                    var extension: String,
                    var contentType: String,
                    var type: String, // 文件所属分类.
                    var length: Long,
                    var folder: String, // 文件所属目录。
                    var zoneId: String, // 共享区ID。
                    var originFile: String, // 真实文件id.
                    var lastUpdateTime: String,
                    var lastUpdatePerson: String,
                    var status: String // 正常|已删除
) {
    //v3版本
    fun copyToVO3(): CloudFileV3Data.FileItem {
        return CloudFileV3Data.FileItem(id, name, createTime, updateTime, isAdmin, isEditor, isCreator, person, extension, contentType, type, length, folder, zoneId, originFile, lastUpdateTime, lastUpdatePerson, status)
    }
}

// 重命名提交对象
data class RenamePost(var name: String)
// 保存到个人网盘 提交对象
data class MoveToMyPanPost(
    var attIdList: List<String> = ArrayList(),
    var folderIdList: List<String> = ArrayList()
)

// 选择器使用的
data class MoveV3Post(
    var name: String = "", // 文件或文件夹名称
    var folder: String = "", // 上级目录
    var superior: String = "" // 上级目录
)

// 选择器使用的
data class FolderItemForPicker(
    var id: String = "",
    var name: String = "",
    var updateTime: String = ""
)

// 共享区提交对象
data class ZonePost(
    var name: String = "",
    var description: String = ""
)


// 收藏提交对象
data class FavoritePost(
    var name: String = "",
    var folder: String = "",
    var orderNumber: String = "" //orderNumber:排序号,升序排列,为空在最后
)