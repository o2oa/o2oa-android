package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.tbs

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.tencent.smtt.sdk.TbsReaderView
import kotlinx.android.synthetic.main.activity_file_reader.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseO2BindActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.databinding.ActivityFileReaderBinding
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.AndroidUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.tbs.WordReadHelper
import java.io.File
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.tbs.WordReadView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport


class FileReaderActivity : BaseO2BindActivity() {


    private val viewModel: FileReaderViewModel by lazy { ViewModelProviders.of(this).get(FileReaderViewModel::class.java) }

    private var wordReadView: WordReadView? = null

    companion object {
        const val file_reader_file_path_key = "file_reader_file_path_key"
        fun startBundle(filePath: String): Bundle {
            val bundle = Bundle()
            bundle.putString(file_reader_file_path_key, filePath)
            return bundle
        }
    }

    var filePath = ""

    override fun bindView(savedInstanceState: Bundle?) {
        val bind = DataBindingUtil.setContentView<ActivityFileReaderBinding>(this, R.layout.activity_file_reader)
        bind.viewmodel = viewModel
        bind.lifecycleOwner = this
    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar(getString(R.string.file_preview), true)
        filePath = intent.extras?.getString(file_reader_file_path_key) ?: ""
        XLog.info("打开文件 ：$filePath")
        if (TextUtils.isEmpty(filePath)) {
            XToast.toastShort(this, "文件路径为空！")
            finish()
            return
        }
        if(WordReadHelper.getInstance().initFinish()){
            wordReadView = WordReadView(this)
            wordReadView?.setFileListener { filePath ->
                cannotOpenFile(filePath)
            }
            fl_file_reader_container.addView(wordReadView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            openFileWithTBS(filePath)
        } else {
            O2DialogSupport.openConfirmDialog(this, "文件预览器内核未下载完成，使用其它应用打开文件？",  { _ ->
                val f = File(filePath)
                AndroidUtils.openFileWithDefaultApp(this@FileReaderActivity, f)
                finish()
            })
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // todo 是否展现 后台需要一个配置
        menuInflater.inflate(R.menu.menu_share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_share) {
            share()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        wordReadView?.destroy()
        super.onDestroy()
    }

    private fun share() {
        val file = File(filePath)
        AndroidUtils.shareFile(this, file)
    }

    private fun openFileWithTBS(file: String) {
        XLog.info("打开文件：$file")
        wordReadView?.loadFile(file)
    }

    private fun cannotOpenFile(filePath: String) {
        O2DialogSupport.openAlertDialog(this, getString(R.string.message_use_other_application_open_file), {
            val f = File(filePath)
            AndroidUtils.openFileWithDefaultApp(this@FileReaderActivity, f)
            finish()
        })
    }


    private fun getFileType(path: String): String {
        var str = ""

        if (TextUtils.isEmpty(path)) {
            return str
        }
        val i = path.lastIndexOf('.')
        if (i <= -1) {
            return str
        }
        str = path.substring(i + 1)
        return str
    }

}
