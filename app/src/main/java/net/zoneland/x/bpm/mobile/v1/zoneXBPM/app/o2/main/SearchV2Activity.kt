package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_search.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.cms.view.CMSWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.WorkOrWorkcompletedList
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.ZoneUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import org.jetbrains.anko.dip

class SearchV2Activity : BaseMVPActivity<SearchV2Contract.View, SearchV2Contract.Presenter>(),
    SearchV2Contract.View, View.OnClickListener {


    private val historyList: ArrayList<String> = arrayListOf()
    private val views: ArrayList<TextView> = arrayListOf()
    private var searchKey = ""
    private val resultList: ArrayList<O2SearchV2Entry> = arrayListOf()
    private var page = 1 // 当前页面
    private var totalPage = 0 // 结果总页数
    private var isLoading = false

    private val resultTypeCMS = "cms"


    private val adapter: CommonRecycleViewAdapter<O2SearchV2Entry> by lazy {
        object : CommonRecycleViewAdapter<O2SearchV2Entry>(
            this,
            resultList,
            R.layout.item_search_v2_result_list
        ) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: O2SearchV2Entry?) {
                if (t != null && holder != null) {

                    holder.setText(R.id.tv_search_result_title, t.title)
                        .setText(
                            R.id.tv_search_result_time, if (t.updateTime.length > 10) {
                                t.updateTime.substring(0, 10)
                            } else {
                                t.updateTime
                            }
                        )
                        .setText(R.id.tv_search_result_summary, t.summary)
                        .setText(
                            R.id.tv_search_result_unit_name, if (t.creatorUnit.contains("@")) {
                                t.creatorUnit.split("@")[0]
                            } else {
                                t.creatorUnit
                            }
                        )
                        .setText(
                            R.id.tv_search_result_person, if (t.creatorPerson.contains("@")) {
                                t.creatorPerson.split("@")[0]
                            } else {
                                t.creatorPerson
                            }
                        )
                    if (t.title.isNotEmpty()) {
                        val titleSp = SpannableStringBuilder(t.title)
                        val index = t.title.indexOf(searchKey)
                        if (index >= 0) {
                            titleSp.setSpan(
                                ForegroundColorSpan(Color.RED),
                                index,
                                (index + searchKey.length),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            )
                        }
                        val titleTV = holder.getView<TextView>(R.id.tv_search_result_title)
                        titleTV.text = titleSp
                    }
                    if (t.summary.isNotEmpty()) {
                        val summarySp = SpannableStringBuilder(t.summary)
                        val index = t.summary.indexOf(searchKey)
                        if (index >= 0) {
                            summarySp.setSpan(
                                ForegroundColorSpan(Color.RED),
                                index,
                                (index + searchKey.length),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                            )
                        }
                        val summaryTV = holder.getView<TextView>(R.id.tv_search_result_summary)
                        summaryTV.text = summarySp
                    }
                }
            }
        }
    }


    override var mPresenter: SearchV2Contract.Presenter = SearchV2Presenter()

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        toolbar = findViewById(R.id.toolbar_snippet_top_bar)
        toolbar?.title = ""
        setSupportActionBar(toolbar)
        toolbar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
        toolbar?.setNavigationOnClickListener { finish() }
        //获取焦点
        et_search_input.isFocusable = true
        et_search_input.requestFocus()
        // 事件
        loadListener()
        // 加载搜索历史
        loadHistory()
        // 结果list
        initRecycler()
    }

    override fun layoutResId(): Int = R.layout.activity_search


    private fun initRecycler() {
        swipe_refresh_search_result.setColorSchemeResources(
            R.color.z_color_refresh_scuba_blue,
            R.color.z_color_refresh_red,
            R.color.z_color_refresh_purple,
            R.color.z_color_refresh_orange
        )
        swipe_refresh_search_result.setOnRefreshListener {
            XLog.debug("下拉刷新")
            if (!isLoading) {
                page = 1
                search()
            }
        }
        recycler_search_result_list.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_search_result_list.isNestedScrollingEnabled = false
        recycler_search_result_list.adapter = adapter
        recycler_search_result_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as? LinearLayoutManager
                val lastP = lm?.findLastVisibleItemPosition()
                if (lastP != null && lastP == resultList.size - 1 && page < totalPage && !isLoading) {
                    XLog.debug("加载更多。。。。")
                    page++
                    search()
                }
            }
        })

        adapter.setOnItemClickListener { _, position ->
            XLog.debug("点击了 position $position")
            val item = resultList[position]
            if (item.category == resultTypeCMS) {
                gotoCMSWebView(item.id, item.title)
            } else {
                getWorkByJobId(item.id, item.title)
            }
        }

    }


    private fun loadListener() {
        ll_search_delete_all_history_btn.setOnClickListener(this)
        // 输入框监听点击键盘搜索键的情况
        et_search_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                XLog.debug("开始搜索")
                val key = et_search_input.text.toString()
                if (key.isNotBlank()) {
                    searchKey = key
                    searchKey = searchKey.trim()
                    addhistory(searchKey)
                    page = 1 //重置
                    search()
                } else {
                    cleanInput()
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        // 输入框 监听清除输入内容的情况
        et_search_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty()) {

                } else {
                    cleanInput()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

    }

    private fun search() {
        isLoading = true
        et_search_input.clearFocus()
        ZoneUtil.toggleSoftInput(et_search_input, false)
        showLoadingDialog()
        val post = O2SearchV2Form()
        post.page = page
        post.query = searchKey
        mPresenter.search(post)
    }

    private fun cleanInput() {
        searchKey = ""
        loadHistory()
        ll_search_history.visible()
        swipe_refresh_search_result.gone()
        ll_search_no_results.gone()
        et_search_input.clearFocus()
        ZoneUtil.toggleSoftInput(et_search_input, false)
    }

    private fun loadHistory() {
        loadHistoryList()
        frame_search_history_list.removeAllViews()
        //获取当前屏幕实际宽度（px）
        val w = resources.displayMetrics.widthPixels
        var xDistance = -1
        var yDistance = 0
        //标签间隔16dp
        val distance = dip(16f)
        for ((i, s) in historyList.withIndex()) {
            val view = LayoutInflater.from(this).inflate(
                R.layout.fragment_search_history_tag,
                frame_search_history_list,
                false
            ) as TextView
            view.text = s
            frame_search_history_list.addView(view)
            view.setOnClickListener {
                XLog.debug("点击了 $s")
                searchKey = s
                et_search_input.setText(s)
                page = 1 //重置
                search()
            }
            if (xDistance == -1) {
                xDistance = 0
            } else {
                //获取前一个标签宽度+16dp作为下一个标签横坐标
                xDistance += views[i - 1].getSelfWidth() + distance
                if (xDistance + view.getSelfWidth() + distance > w) {
                    //加上新标签的宽度大于屏幕宽度时换行
                    xDistance = 0
                    //换行时y坐标向下一行
                    yDistance += 120
                }
            }
            view.layoutSelf(xDistance, yDistance)
            views.add(view)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_search_delete_all_history_btn -> {
                XLog.debug("删除所有的历史搜索")
                O2SDKManager.instance().prefs().edit {
                    putStringSet(O2.PRE_SEARCH_HISTORY_KEY, setOf())
                }
                loadHistory()
            }
        }
    }

    private fun addhistory(key: String) {
        var historys = O2SDKManager.instance().prefs()
            .getStringSet(O2.PRE_SEARCH_HISTORY_KEY, HashSet<String>())
        if (historys == null) {
            historys = HashSet<String>()
        }
        if (!historys.contains(key)) {
            historys.add(key)
        }
        O2SDKManager.instance().prefs().edit {
            putStringSet(O2.PRE_SEARCH_HISTORY_KEY, historys)
        }
    }

    private fun loadHistoryList() {
        historyList.clear()
        val historys =
            O2SDKManager.instance().prefs().getStringSet(O2.PRE_SEARCH_HISTORY_KEY, setOf())
        if (historys != null) {
            historyList.addAll(historys.map { it })
        }
    }


    private fun getWorkByJobId(jobId: String, title: String) {
        showLoadingDialog()
        mPresenter.getWorkByJobId(jobId, title)
    }

    private fun gotoWorkActivity(workId: String, title: String) {
        XLog.debug("goto task work web view page id:$workId , title: $title")
        val bundle = Bundle()
        bundle.putString(TaskWebViewActivity.WORK_WEB_VIEW_WORK, workId)
        bundle.putString(TaskWebViewActivity.WORK_WEB_VIEW_TITLE, title)
        go<TaskWebViewActivity>(bundle)

    }

    private fun gotoCMSWebView(docId: String, title: String) {
        XLog.debug("goto cms web view page id:$docId , title: $title")
        go<CMSWebViewActivity>(CMSWebViewActivity.startBundleData(docId, title))
    }

    override fun searchResult(result: O2SearchV2PageModel) {
        isLoading = false
        swipe_refresh_search_result.isRefreshing = false
        hideLoadingDialog()
        val count = result.count
        if (count > 0) {
            val m = count % O2.DEFAULT_PAGE_NUMBER
            totalPage = count / O2.DEFAULT_PAGE_NUMBER
            if (m > 0) {
                totalPage += 1 // 加一页
            }
        }
        if (page == 1 && result.documentList.isEmpty()) { // 没有结果
            ll_search_no_results.visible()
            ll_search_history.gone()
            swipe_refresh_search_result.gone()
        } else {
            ll_search_no_results.gone()
            ll_search_history.gone()
            swipe_refresh_search_result.visible()
            if (page == 1) {
                resultList.clear()
            }
            resultList.addAll(result.documentList)
            adapter.notifyDataSetChanged()
        }
    }

    override fun workOrWorkcompletedResult(list: WorkOrWorkcompletedList?, title: String) {
        hideLoadingDialog()
        if (list != null && (list.workList.isNotEmpty() || list.workCompletedList.isNotEmpty())) {
            val itemCount = list.workList.size + list.workCompletedList.size
            if (itemCount == 1) {
                if (list.workList.isNotEmpty()) {
                    gotoWorkActivity(list.workList[0].id!!, title)
                } else if (list.workCompletedList.isNotEmpty()) {
                    gotoWorkActivity(list.workCompletedList[0].id!!, title)
                }
            } else {
                val items: ArrayList<WorkOrWorkcompletedItem> = arrayListOf()
                list.workList.forEach {
                    val itemTitle = if (TextUtils.isEmpty(it.title)) { getString(R.string.no_title)+"[${it.activityName}]" } else { it.title!!+"[${it.activityName}]" }
                    val item = WorkOrWorkcompletedItem(it.id!!, itemTitle)
                    items.add(item)
                }
                list.workCompletedList.forEach {
                    val itemTitle = if (TextUtils.isEmpty(it.title)) { getString(R.string.no_title)+"[${it.activityName}]" } else { it.title!!+"[${it.activityName}]" }
                    val item = WorkOrWorkcompletedItem(it.id!!, itemTitle)
                    items.add(item)
                }
                val itemTitles = items.map { it.title }
                BottomSheetMenu(this)
                    .setItems(itemTitles, ContextCompat.getColor(this, R.color.z_color_text_primary)) { i ->
                        val item = items[i]
                        gotoWorkActivity(item.id, title)
                    }
                    .show()
            }
        } else {
            XToast.toastShort(this, getString(R.string.search_process_job_error))
        }
    }

    data class WorkOrWorkcompletedItem(
        var id: String, // work or workcompleted id
        var title: String
    )
}