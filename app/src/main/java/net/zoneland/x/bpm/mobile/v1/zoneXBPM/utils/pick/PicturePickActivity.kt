package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Created by fancyLou on 2022-03-24.
 * Copyright © 2022 o2android. All rights reserved.
 */
class PicturePickActivity : AppCompatActivity() {

    private var multiple: Boolean = false
    private var mode: PickTypeMode = PickTypeMode.Picture


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val modeInt = intent.extras?.getInt(PicturePickUtil.MODE_INTENT_KEY) ?: 0
        mode = when(modeInt) {
            0 -> PickTypeMode.Picture
            1 -> PickTypeMode.File
            else -> PickTypeMode.FileWithMedia
        }
        multiple = intent.extras?.getBoolean(PicturePickUtil.MULTIPLE_INTENT_KEY) ?: false
        if (mode == PickTypeMode.Picture) {
            openSystemAlbum()
        } else { // PickTypeMode.File PickTypeMode.FileWithMedia
            openFileChoose()
        }
    }

    // 打开相册 选择图片
    private fun openSystemAlbum() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        if (multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(intent, PicturePickUtil.PICK_PHOTO)
    }

    // 文档类型的文件
   private val file_mimetypes = arrayOf(
       //"audio/*",
       //"image/*",
       //"video/*",
//       "font/*",
//       "message/*",
//       "model/*",
//       "multipart/*",
       "application/*",
       "text/*"
    )
    private val file_media_mimetypes = arrayOf(
        "audio/*",
        "image/*",
        "video/*",
        "application/*",
        "text/*"
    )
    // 选择文件
    private fun openFileChoose() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        val mimetypes = if (mode == PickTypeMode.FileWithMedia) {
            file_media_mimetypes
        } else {
            file_mimetypes
        }
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        if (multiple) {
            // 支持多选（长按多选）
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        // 用于表示 Intent 仅希望查询能使用 ContentResolver.openFileDescriptor(Uri, String) 打开的 Uri
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, PicturePickUtil.PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PicturePickUtil.PICK_PHOTO) {
            val results = ArrayList<String>()
            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    XLog.debug("选取的文件 $i : $uri")
                    val tempFilePath = if (uri != null) {
                        getFilePath(uri)
                    } else {
                        ""
                    }
                    if (!TextUtils.isEmpty(tempFilePath)) {
                        results.add(tempFilePath!!)
                    }
                }
            } else {
                // 获取选取返回的图片资源, Uri 格式
                val uri = data?.data
                // URI 格式参考: content://media/external/images/media/123
                XLog.debug("选取的文件: $uri")
                val tempFilePath = if (uri != null) {
                    getFilePath(uri)
                } else {
                    ""
                }
                if (!TextUtils.isEmpty(tempFilePath)) {
                    results.add(tempFilePath!!)
                }
            }
            intent.putExtra(PicturePickUtil.PICK_PHOTO_CALLBACK_KEY, results)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }


    /**
     * 图片转存到临时目录
     */
    private fun getFilePath(uri: Uri): String? {
        var imageInputStream: InputStream? = null
        try {
            val fileName = getFileName(uri) ?: "unknown.png"
            val path =  FileExtensionHelper.getXBPMTempFolder(this) + File.separator + fileName
            // 打开 Uri 的输入流
            imageInputStream = contentResolver.openInputStream(uri)
            val file = File(path)
            val fos = FileOutputStream(file, true)
            val buf = ByteArray(1024 * 8)
            var currentLength = 0
            while (true) {
                val num = imageInputStream?.read(buf) ?: 0
                // 计算进度条位置
                if (num <= 0) {
                    break
                }
                currentLength += num
                fos.write(buf, 0, num)
                fos.flush()
            }
            fos.flush()
            fos.close()
            imageInputStream?.close()
            return path
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 查找图片名称
     */
    private fun getFileName(uri: Uri): String? {
        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null)

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {

                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))

//                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
//                val size: String = if (!it.isNull(sizeIndex)) {
//                    // Technically the column stores an int, but cursor.getString()
//                    // will do the conversion automatically.
//                    it.getString(sizeIndex)
//                } else {
//                    "Unknown"
//                }
//                Log.i(TAG, "Size: $size")
            }
        }
        return null
    }


}