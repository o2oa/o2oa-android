package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process


import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_task_complete_search.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.WorkTypeEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.service.PictureLoaderService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.SearchWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.ZoneUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2oaColorScheme
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleImageView


/**
 * 搜索工作
 * 支持多个类型，待办、已办、待阅、已阅
 *
 */
class TaskCompletedSearchActivity : BaseMVPActivity<TaskCompletedSearchContract.View, TaskCompletedSearchContract.Presenter>(), TaskCompletedSearchContract.View {
    override var mPresenter: TaskCompletedSearchContract.Presenter = TaskCompletedSearchPresenter()
    override fun layoutResId(): Int = R.layout.activity_task_complete_search


    companion object {
        const val SearchTypeKey = "SearchTypeKey"
        fun openSearch(type: WorkTypeEnum, activity: Activity) {
            val bundle = Bundle()
            bundle.putSerializable(SearchTypeKey, type)
            activity.go<TaskCompletedSearchActivity>(bundle)
        }
    }

    var pictureLoaderService: PictureLoaderService? = null
    var lastId: String = ""
    var searchKey: String = ""
    var isRefresh = false
    var isLoading = false
    val resultList = ArrayList<SearchWorkData>()
    var searchType: WorkTypeEnum = WorkTypeEnum.TaskCompleted // 默认已办

    val adapter: CommonRecycleViewAdapter<SearchWorkData> by lazy {
        object : CommonRecycleViewAdapter<SearchWorkData>(this, resultList, R.layout.item_todo_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, data: SearchWorkData?) {
                val time = data?.startTime?.substring(0, 10) ?:""
                holder?.setText(R.id.todo_card_view_title_id, data?.title)
                        ?.setText(R.id.todo_card_view_content_id, "【${data?.processName}】")
                        ?.setText(R.id.todo_card_view_node_id, data?.activityName)
                        ?.setText(R.id.todo_card_view_time_id, time)
                val icon = holder?.getView<CircleImageView>(R.id.todo_card_view_icon_id)
                icon?.setImageResource(R.mipmap.icon_process_app_default)
                icon?.tag = data?.application
                loadApplicationIcon(holder?.convertView, data?.application)
            }
        }
    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        searchType = (intent.extras?.getSerializable(SearchTypeKey) as? WorkTypeEnum) ?: WorkTypeEnum.TaskCompleted
        val toolBar = findViewById<Toolbar>(R.id.toolbar_task_completed_search_top_bar)
        toolBar?.title = ""
        setSupportActionBar(toolBar)
        toolBar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
        toolBar?.setNavigationOnClickListener { finish() }
        edit_task_completed_search_key.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ) {
                searchKey = edit_task_completed_search_key.text.toString()
                if (searchKey.isEmpty()) {
                    cleanResultList()
                } else {
                    searchTaskCompletedOnLine(searchKey)
                }
                edit_task_completed_search_key.clearFocus()
                ZoneUtil.toggleSoftInput(edit_task_completed_search_key, false)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        edit_task_completed_search_key.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 0) {
                    searchKey = ""
                    cleanResultList()
                }
//                else {
//                    searchTaskCompletedOnLine(s?.toString() ?: "")
//                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
        //SwipeRefreshLayout 下拉刷新控件的颜色 最多4个
        refresh_task_completed_layout.o2oaColorScheme()
        refresh_task_completed_layout.recyclerViewPageNumber = O2.DEFAULT_PAGE_NUMBER
        refresh_task_completed_layout.setOnRefreshListener {
            if(!isLoading && !isRefresh){
                isRefresh = true
                searchTaskCompleted(true)
            }
        }
        refresh_task_completed_layout.setOnLoadMoreListener{
            if (!isLoading && !isRefresh && !TextUtils.isEmpty(lastId)) {
                isLoading = true
                searchTaskCompleted(false)
            }
        }

        adapter.setOnItemClickListener { _, position ->
            openWork(resultList[position])
        }
        recycler_task_completed_search_list.adapter = adapter
        recycler_task_completed_search_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onResume() {
        super.onResume()
        pictureLoaderService = PictureLoaderService(this)
    }

    override fun onPause() {
        super.onPause()
        pictureLoaderService?.close()
    }

    override fun searchFail() {
        finishAnimation()
        cleanResultList()
    }

    override fun searchResult(list: List<SearchWorkData>) {
        if (isRefresh) {
            resultList.clear()
            resultList.addAll(list)
            if (list.isEmpty()) {
                refresh_task_completed_layout.gone()
                ll_search_no_results.visible()
            } else {
                refresh_task_completed_layout.visible()
                ll_search_no_results.gone()
            }
        }else {
            resultList.addAll(list)
        }
        if (resultList.size>0) {
            lastId = resultList[resultList.size-1].id ?: ""
        }
        adapter.notifyDataSetChanged()
        finishAnimation()
    }

    private fun openWork(data: SearchWorkData) {
        when(searchType) {
            WorkTypeEnum.TaskCompleted -> showTaskCompletedWorkFragment(data.id ?: "")
            else -> showTask(data)
        }
    }
    private fun showTask(data: SearchWorkData) {
        go<TaskWebViewActivity>(TaskWebViewActivity.start(data.work, data.workCompleted, data.title))
    }

    private var taskCompletedWorkListFragment: TaskCompletedWorkListFragment? = null
    private fun showTaskCompletedWorkFragment(taskId: String) {
        taskCompletedWorkListFragment = TaskCompletedWorkListFragment.createFragmentInstance(taskId)
        taskCompletedWorkListFragment?.show(supportFragmentManager, TaskCompletedWorkListFragment.TASK_COMPLETED_WORK_LIST_FRAGMENT_TAG)
    }

    private fun searchTaskCompleted(flag: Boolean) {
        XLog.debug("查询开始 flag：$flag searchKey: $searchKey")
        showLoadingDialog()
        if (flag) {
            mPresenter.search(O2.FIRST_PAGE_TAG, searchKey, searchType)
        }else {
            mPresenter.search(lastId, searchKey, searchType)
        }
    }

    /**
     * 查询已办
     * @param s
     */
    private fun searchTaskCompletedOnLine(key: String) {
        if (TextUtils.isEmpty(key)) {
            searchKey = ""
            cleanResultList()
            return
        } else {
            XLog.debug("查询已办 key：$key")
            searchKey = key
            isRefresh = true
            searchTaskCompleted(true)
        }
    }

    /**
     * 清除列表
     */
    private fun cleanResultList() {
        resultList.clear()
        adapter.notifyDataSetChanged()
    }

    private fun finishAnimation() {
        hideLoadingDialog()
        if (isRefresh) {
            refresh_task_completed_layout.isRefreshing = false
            isRefresh = false
        }
        if (isLoading) {
            refresh_task_completed_layout.setLoading(false)
            isLoading = false
        }
    }

    fun loadApplicationIcon(convertView: View?, application: String?) {
        pictureLoaderService?.loadProcessAppIcon(convertView, application)
    }
}
