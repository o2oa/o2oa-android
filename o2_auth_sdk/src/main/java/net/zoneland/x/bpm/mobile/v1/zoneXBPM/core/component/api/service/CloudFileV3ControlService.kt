package net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.EchoData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.IdData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ValueData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.yunpan.*
import okhttp3.MultipartBody
import retrofit2.http.*
import rx.Observable


/**
 * Created by fancy on 2017/6/6.
 */

interface CloudFileV3ControlService {


    /**
     * 服务器应答
     */
    @GET("jaxrs/echo")
    fun echo(): Observable<ApiResponse<EchoData>>

    /**
     * 企业网盘 我的收藏
     */
    @GET("jaxrs/favorite/list")
    fun listMyFavorite(): Observable<ApiResponse<List<CloudFileZoneData.MyFavorite>>>

    /**
     * 企业网盘 我的共享区
     */
    @GET("jaxrs/zone/list")
    fun listMyZone(): Observable<ApiResponse<List<CloudFileZoneData.MyZone>>>


    /**
     * 文件夹下的文件列表
     */
    @GET("jaxrs/attachment3/list/folder/{folderId}/order/by/{orderBy}/desc/true")
    fun listFileByFolderIdV3(@Path("folderId") folderId: String, @Path("orderBy") orderBy: String): Observable<ApiResponse<List<FileV3Json>>>

    /**
     * 文件夹下的文件夹列表
     */
    @GET("jaxrs/folder3/list/{folderId}/order/by/{orderBy}/desc/true")
    fun listFolderByFolderIdV3(@Path("folderId") folderId: String,  @Path("orderBy") orderBy: String): Observable<ApiResponse<List<FolderV3Json>>>

    /**
     * 文件夹重命名
     */
    @POST("jaxrs/folder3/{id}/update/name")
    fun updateFolderNameV3(@Path("id") id: String, @Body body: RenamePost): Observable<ApiResponse<ValueData>>

    /**
     * 文件重命名
     */
    @POST("jaxrs/attachment3/{id}/update/name")
    fun updateFileNameV3(@Path("id") id: String, @Body body: RenamePost): Observable<ApiResponse<IdData>>

    @DELETE("jaxrs/attachment3/{id}")
    fun deleteFileV3(@Path("id") id: String): Observable<ApiResponse<ValueData>>

    @DELETE("jaxrs/folder3/{id}")
    fun deleteFolderV3(@Path("id") id: String): Observable<ApiResponse<ValueData>>

    /**
     * 保存到个人网盘
     */
    @POST("jaxrs/folder3/save/to/person/{personFolder}")
    fun moveToMyPan(@Path("personFolder") personFolder: String, @Body body: MoveToMyPanPost): Observable<ApiResponse<ValueData>>

    /**
     * 移动文件夹
     */
    @POST("jaxrs/folder3/{id}/move")
    fun moveFolderV3(@Path("id") id: String, @Body body: MoveV3Post): Observable<ApiResponse<ValueData>>

    /**
     * 移动文件
     */
    @POST("jaxrs/attachment3/{id}/move")
    fun moveFileV3(@Path("id") id: String, @Body body: MoveV3Post): Observable<ApiResponse<IdData>>


    /**
     * 创建文件夹
     *
     * @param json name ，superior上级id (为空就是顶级)
     * *
     * @return
     */
    @POST("jaxrs/folder3")
    fun createFolderV3(@Body json: Map<String, String>): Observable<ApiResponse<IdData>>



    /**
     * 上传文件
     * @param body
     *
     * @param folderId 共享区或者目录ID
     */
    @Multipart
    @POST("jaxrs/attachment3/upload/folder/{folderId}")
    fun uploadFile2FolderV3(@Part body: MultipartBody.Part, @Path("folderId") folderId: String): Observable<ApiResponse<IdData>>




    /**
     * 顶层文件列表
     */
    @GET("jaxrs/attachment2/list/top/order/by/updateTime/desc/true")
    fun listFileTop() : Observable<ApiResponse<List<FileJson>>>

    /**
     * 文件夹下的文件列表
     */
    @GET("jaxrs/attachment2/list/folder/{folderId}/order/by/updateTime/desc/true")
    fun listFileByFolderId(@Path("folderId") folderId: String): Observable<ApiResponse<List<FileJson>>>


    /**
     * 分页查询文件列表
     * @param typeBody 分类
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @POST("jaxrs/attachment2/list/type/{page}/size/{count}")
    fun listFileByPage(@Path("page") page: Int, @Path("count") count: Int,
                       @Body typeBody: CloudDiskPageForm): Observable<ApiResponse<List<FileJson>>>

    /**
     * 顶层文件夹列表
     */
    @GET("jaxrs/folder2/list/top/order/by/updateTime/desc/true")
    fun listFolderTop(): Observable<ApiResponse<List<FolderJson>>>

    /**
     * 文件夹下的文件夹列表
     */
    @GET("jaxrs/folder2/list/{folderId}/order/by/updateTime/desc/true")
    fun listFolderByFolderId(@Path("folderId") folderId: String): Observable<ApiResponse<List<FolderJson>>>


    /**
     * 创建文件夹
     *
     * @param json name ，superior上级id (为空就是顶级)
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @POST("jaxrs/folder2")
    fun createFolder(@Body json: Map<String, String>): Observable<ApiResponse<IdData>>


    /**
     * 上传文件
     * @param body
     *
     * @param folderId 顶级目录用：O2.FIRST_PAGE_TAG
     */
    @Multipart
    @POST("jaxrs/attachment2/upload/folder/{folderId}")
    fun uploadFile2Folder(@Part body: MultipartBody.Part, @Path("folderId") folderId: String): Observable<ApiResponse<IdData>>


    /**
     * 更新文件信息
     * @param item
     * *
     * @param id
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/attachment2/{id}")
    fun updateFile(@Body item: FileJson, @Path("id") id: String): Observable<ApiResponse<IdData>>

    /**
     * 删除文件
     * @param id
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @DELETE("jaxrs/attachment2/{id}")
    fun deleteFile(@Path("id") id: String): Observable<ApiResponse<IdData>>

    /**
     * 获取文件
     */
    @GET("jaxrs/attachment2/{id}")
    fun getFile(@Path("id") id: String): Observable<ApiResponse<FileJson>>


    /**
     * 重命名文件夹
     * @param folderId
     * *
     * @param folder
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @PUT("jaxrs/folder2/{folderId}")
    fun updateFolder(@Path("folderId") folderId: String, @Body folder: FolderJson): Observable<ApiResponse<IdData>>

    /**
     * 删除文件夹
     * @param folderId
     * *
     * @return
     */
    @Headers("Content-Type:application/json;charset=UTF-8")
    @DELETE("jaxrs/folder2/{folderId}")
    fun deleteFolder(@Path("folderId") folderId: String): Observable<ApiResponse<IdData>>


    /**
     * 分享
     */
    @POST("jaxrs/share")
    fun share(@Body form: CloudDiskShareForm): Observable<ApiResponse<IdData>>

    /**
     * 分享给我的
     * @param fileType 分享的文件类型:文件(attachment)|目录(folder)|全部({0})
     */
    @GET("jaxrs/share/list/to/me2/{fileType}")
    fun listShareToMe(@Path("fileType") fileType: String): Observable<ApiResponse<List<ShareJson>>>


    /**
     * 我分享的
     * @param shareType 分享类型:密码分享(password)|指定分享(member)|全部({0})
     * @param fileType 分享的文件类型:文件(attachment)|目录(folder)|全部({0})
     */
    @GET("jaxrs/share/list/my2/{shareType}/{fileType}")
    fun listMyShare(@Path("shareType") shareType: String, @Path("fileType") fileType: String) : Observable<ApiResponse<List<ShareJson>>>


    /**
     * 获取共享文件指定文件夹下的附件.
     * @param shareId:共享文件ID
     * @param folderId:目录ID(共享文件对象ShareJson中的fileId)
     */
    @GET("jaxrs/share/list/att/share/{shareId}/folder/{folderId}/")
    fun listShareAttachmentWithFolder(@Path("shareId") shareId: String, @Path("folderId") folderId: String): Observable<ApiResponse<List<FileJson>>>

    /**
     * 获取共享文件指定文件夹下的直属文件夹.
     * @param shareId:共享文件ID
     * @param folderId:目录ID(共享文件对象ShareJson中的fileId)
     */
    @GET("jaxrs/share/list/folder/share/{shareId}/folder/{folderId}/")
    fun listShareFolderWithFolder(@Path("shareId") shareId: String, @Path("folderId") folderId: String): Observable<ApiResponse<List<FolderJson>>>

    /**
     * 屏蔽给我的分享
     */
    @GET("jaxrs/share/shield/{id}")
    fun shieldShare(@Path("id")id: String): Observable<ApiResponse<IdData>>

    /**
     * 删除分享
     */
    @DELETE("jaxrs/share/{id}")
    fun deleteMyShare(@Path("id")id: String): Observable<ApiResponse<ValueData>>
}