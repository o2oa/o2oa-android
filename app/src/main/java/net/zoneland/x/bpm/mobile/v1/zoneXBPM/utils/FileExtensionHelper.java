package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils;

import android.content.Context;
import android.text.TextUtils;

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2;
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by FancyLou on 2015/11/25.
 */
public class FileExtensionHelper {



    /**
     * 临时目录
     * @return
     */
    public static String getXBPMTempFolder(Context context) {
        File file = FileUtil.INSTANCE.o2AppExternalBaseDir(context);
        if (file != null) {
            return file.getAbsolutePath() + File.separator + O2.INSTANCE.getBASE_TMP_FOLDER();
        }else {
            return null;
        }
    }

    /**
     * 日志目录
     * @return
     */
    public static String getXBPMLogFolder(Context context) {
        File file = FileUtil.INSTANCE.o2AppExternalBaseDir(context);
        if (file != null) {
            return file.getAbsolutePath() + File.separator + O2.INSTANCE.getBASE_LOG_FOLDER();
        }else {
            return null;
        }
    }

    /**
     * 流程平台附件临时目录
     * @return
     */
    public static String getXBPMWORKAttachmentFolder(Context context) {
        File file = FileUtil.INSTANCE.o2AppExternalBaseDir(context);
        if (file != null) {
            return file.getAbsolutePath() + File.separator + O2.INSTANCE.getBASE_WORK_ATTACH_FOLDER();
        }else {
            return null;
        }
    }

    /**
     * 论坛附件下载目录
     * @return
     */
    public static String getXBPMBBSAttachFolder(Context context) {
        File file = FileUtil.INSTANCE.o2AppExternalBaseDir(context);
        if (file != null) {
            return file.getAbsolutePath() + File.separator + O2.INSTANCE.getBASE_BBS_ATTACH_FOLDER();
        }else {
            return null;
        }
    }

    /**
     * 会议附件下载目录
     * @return
     */
    public static String getXBPMMEETINGAttachFolder(Context context) {
        File file = FileUtil.INSTANCE.o2AppExternalBaseDir(context);
        if (file != null) {
            return file.getAbsolutePath() + File.separator + O2.INSTANCE.getBASE_MEETING_ATTACH_FOLDER();
        }else {
            return null;
        }
    }

    /**
     * 内容附件下载目录
     * @return
     */
    public static String getXBPMCMSAttachFolder(Context context) {
        File file = FileUtil.INSTANCE.o2AppExternalBaseDir(context);
        if (file != null) {
            return file.getAbsolutePath() + File.separator + O2.INSTANCE.getBASE_CMS_ATTACH_FOLDER();
        }else {
            return null;
        }
    }

    /**
     * 头像缓存目录
     * @return
     */
    public static String getXBPMAvatarTempFolder(Context context) {
        File file = FileUtil.INSTANCE.o2AppExternalBaseDir(context);
        if (file != null) {
            return file.getAbsolutePath() + File.separator + O2.INSTANCE.getAVATAR_TMP_FOLDER();
        }else {
            return null;
        }
    }

    /**
     * 签名图片临时地址
     * @return
     */
    public static String generateSignTempFilePath(Context context) {
        return getXBPMTempFolder(context) + File.separator + UUID.randomUUID().toString() + O2.INSTANCE.getIMAGE_SUFFIX_PNG();
    }

    /**
     * 根据姓名生成图片存储路径
     *
     * @return
     */
    public static String generateAvatarFilePath(Context context) {
        String avatar_icon_file_name = UUID.randomUUID().toString();
        String imageFilePath = getXBPMAvatarTempFolder(context) + File.separator + avatar_icon_file_name + O2.INSTANCE.getIMAGE_SUFFIX_PNG();
        return imageFilePath;
    }

    /**
     * BBS上传文件压缩后使用的临时文件路径
     * @return
     */
    public static String generateBBSTempFilePath(Context context) {
        String randomId = UUID.randomUUID().toString();
        return getXBPMTempFolder(context) + File.separator + randomId + O2.INSTANCE.getIMAGE_SUFFIX_PNG();
    }

    /**
     * 获取流程平台附件路径
     * @param fileName
     * @return
     */
    public static String getXBPMWORKAttachmentFileByName(String fileName, Context context) {
        return getXBPMWORKAttachmentFolder(context) + File.separator + fileName;
    }

    /**
     * 获取流程平台附件路径
     * @param fileName
     * @return
     */
    public static String getXBPMMEETINGAttachmentFileByName(String fileName, Context context) {
        return getXBPMMEETINGAttachFolder(context) + File.separator + fileName;
    }


    /**
     * 拍照后的暂存地址
     * png
     * @Deprecated 现在用createImageFile 方法生成一张图片 再进行拍摄
     * @return
     */
    @Deprecated
    public static String getCameraCacheFilePath(Context context) {
        return getXBPMTempFolder(context) + File.separator + "camera_cache.png";
    }


    /**
     * 拍照用的地址 自动生成
     * @return
     * @throws IOException
     */
    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = FileUtil.INSTANCE.appExternalImageDir(context);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }


    /**
     * 是否图片
     * @param extension
     * @return
     */
    public static boolean isImageFromFileExtension(String extension) {
        if(TextUtils.isEmpty(extension)){
            return false;
        }
        extension = extension.toLowerCase();
        switch (extension){
            case "jpg":
            case "jpeg":
            case "gif":
            case "png":
            case "bmp":
                return true;
        }
        return false;
    }

    /**
     *
     * @param extension
     * @return
     */
    public static boolean isFileTBSCanOpen(String extension) {
        if(TextUtils.isEmpty(extension)){
            return false;
        }
        extension = extension.toLowerCase();
        switch (extension){
            case "doc":
            case "docx":
            case "ppt":
            case "pptx":
            case "xlsx":
            case "xls":
            case "pdf":
            case "txt":
            case "json":
                return true;
        }
        return false;
    }

    /**
     * 是否视频文件
     * @param extension 扩展名 mp4等
     * @return
     */
    public static boolean isVideoFromExtension(String extension) {
        if(TextUtils.isEmpty(extension)){
            return false;
        }
        extension = extension.toLowerCase();
        switch (extension){
            case "mp4":
            case "avi":
            case "mov":
            case "rm":
            case "mkv":
                return true;
        }
        return false;
    }

    /**
     * 文件扩展 返回图标
     * @param extension
     * @return
     */
    public static int getImageResourceByFileExtension(String extension) {
        if(TextUtils.isEmpty(extension)){
            return R.mipmap.icon_file_unkown;
        }
        extension = extension.toLowerCase();
        switch (extension){
            case "jpg":
            case "jpeg":
                return R.mipmap.icon_file_jpeg;
            case "gif":
                return R.mipmap.icon_file_gif;
            case "png":
                return R.mipmap.icon_file_png;
            case "tiff":
                return R.mipmap.icon_file_tiff;
            case "bmp":
            case "webp":
                return R.mipmap.icon_file_img;
            case "ogg":
            case "mp3":
            case "wav":
            case "wma":
                return R.mipmap.icon_file_mp3;
            case "mp4":
                return R.mipmap.icon_file_mp4;
            case "avi":
                return R.mipmap.icon_file_avi;
            case "mov":
            case "rm":
            case "mkv":
                return R.mipmap.icon_file_rm;
            case "doc":
            case "docx":
                return R.mipmap.icon_file_word;
            case "xls":
            case "xlsx":
                return R.mipmap.icon_file_excel;
            case "ppt":
            case "pptx":
                return R.mipmap.icon_file_ppt;
            case "html":
                return R.mipmap.icon_file_html;
            case "pdf":
                return R.mipmap.icon_file_pdf;
            case "txt":
            case "json":
                return R.mipmap.icon_file_txt;
            case "zip":
                return R.mipmap.icon_file_zip;
            case "rar":
                return R.mipmap.icon_file_rar;
            case "7z":
                return R.mipmap.icon_file_arch;
            case "ai":
                return R.mipmap.icon_file_ai;
            case "att":
                return R.mipmap.icon_file_att;
            case "au":
                return R.mipmap.icon_file_au;
            case "cad":
                return R.mipmap.icon_file_cad;
            case "cdr":
                return R.mipmap.icon_file_cdr;
            case "eps":
                return R.mipmap.icon_file_eps;
            case "exe":
                return R.mipmap.icon_file_exe;
            case "iso":
                return R.mipmap.icon_file_iso;
            case "link":
                return R.mipmap.icon_file_link;
            case "swf":
                return R.mipmap.icon_file_flash;
            case "psd":
                return R.mipmap.icon_file_psd;
            case "tmp":
                return R.mipmap.icon_file_tmp;
            default:
                return R.mipmap.icon_file_unkown;
        }

    }




}
