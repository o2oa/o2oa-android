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
import java.io.File

class FileReaderActivity : BaseO2BindActivity() {


    private val viewModel: FileReaderViewModel by lazy { ViewModelProviders.of(this).get(FileReaderViewModel::class.java) }
    private var mTbsReaderView: TbsReaderView?=null

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
        mTbsReaderView = TbsReaderView(this) { arg, arg1, arg2 ->
            XLog.info("arg:$arg, 1:$arg1, 2:$arg2")
        }
        fl_file_reader_container.addView(mTbsReaderView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        filePath = intent.extras?.getString(file_reader_file_path_key) ?: ""
        XLog.info("打开文件 ：$filePath")
        if (!TextUtils.isEmpty(filePath)) {
            openFileWithTBS(filePath)
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
        mTbsReaderView?.onStop()
        super.onDestroy()
    }

    private fun share() {
        val file = File(filePath)
        AndroidUtils.shareFile(this, file)

    }

    private fun openFileWithTBS(file: String) {
        XLog.info("打开文件：$file")
        val type = getFileType(file)
        val b = mTbsReaderView?.preOpen(type, false)
        if (b == true) {
            val bund = Bundle()
            bund.putString("filePath", file)
            bund.putString("tempPath", FileExtensionHelper.getXBPMTempFolder(this))
            mTbsReaderView?.openFile(bund)
        }else {
            XLog.error("type is error , $type")
            XToast.toastShort(this, getString(R.string.message_file_type_cannot_be_previewed))
            fl_file_reader_container.removeAllViews()
            val btn = Button(this)
            btn.text = getString(R.string.message_use_other_application_open_file)
            val param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            param.gravity = Gravity.CENTER
            fl_file_reader_container.addView(btn, param)
            btn.setOnClickListener {
                val f = File(file)
                AndroidUtils.openFileWithDefaultApp(this, f)
                finish()
            }
        }

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
