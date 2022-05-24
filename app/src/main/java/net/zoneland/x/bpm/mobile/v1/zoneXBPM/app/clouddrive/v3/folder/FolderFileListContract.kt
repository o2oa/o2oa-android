package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive.v3.folder

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BasePresenter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.CloudFileV3Data

/**
 * Created by fancyLou on 2022-05-20.
 * Copyright © 2022 o2android. All rights reserved.
 */
object FolderFileListContract {

    interface View: BaseView {
        fun folderFileItemList(list: List<CloudFileV3Data>)
        fun updateName(isSuccess: Boolean, error: String?)
        fun delete(isSuccess: Boolean, error: String?)
        fun moveToMyPan(isSuccess: Boolean, error: String?)
        fun move(isSuccess: Boolean, error: String?)
        fun uploadFile(isSuccess: Boolean, error: String?)
        fun createFolder(isSuccess: Boolean, error: String?)
    }

    interface Presenter: BasePresenter<View>{
        fun getFolderFileItemList(parentId: String)
        fun updateFolderName(folderId: String, newName: String)
        fun updateFileName(fileId: String, newName: String)
        fun deleteFolderOrFile(item: CloudFileV3Data)
        fun moveToMyPan(parentId: String, files: List<CloudFileV3Data.FileItem>, folders: List<CloudFileV3Data.FolderItem>)
        fun move(parentId: String, files: List<CloudFileV3Data.FileItem>, folders: List<CloudFileV3Data.FolderItem>)
        fun uploadFileList(parentId: String, files: List<String>)
        fun createFolder(parentId: String, name: String)
    }

}