package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.logs

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_logs.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.tbs.FileReaderActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.DividerItemDecoration
import java.io.File

class LogsActivity : BaseMVPActivity<LogsContract.View, LogsContract.Presenter>(), LogsContract.View {

    override var mPresenter: LogsContract.Presenter =  LogsPresenter()

    private var list: ArrayList<File> = ArrayList()

    private val adapter by lazy {
        object : CommonRecycleViewAdapter<File>(this, list, R.layout.item_text1) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: File?) {
                holder?.setText(android.R.id.text1, t?.name)
            }
        }
    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        setupToolBar("日志", true)
        rv_logs_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_logs_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
        rv_logs_list.adapter = adapter
        adapter.setOnItemClickListener { _, position ->
            go<FileReaderActivity>(FileReaderActivity.startBundle(this.list[position].absolutePath))
        }
        mPresenter.loadLogFileList()
    }

    override fun layoutResId(): Int = R.layout.activity_logs

    override fun showLogList(list: List<File>) {
        this.list.clear()
        this.list.addAll(list)
        adapter.notifyDataSetChanged()
    }
}