package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_search.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchEntry
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2SearchPageModel
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.ZoneUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.*
import org.jetbrains.anko.dip

class SearchActivity : BaseMVPActivity<SearchContract.View, SearchContract.Presenter>(), SearchContract.View, View.OnClickListener {


    override var mPresenter: SearchContract.Presenter = SearchPresenter()

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        toolbar = findViewById(R.id.toolbar_snippet_top_bar)
        toolbar?.title = ""
        setSupportActionBar(toolbar)
        toolbar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
        toolbar?.setNavigationOnClickListener { finish() }
        //获取焦点
        et_search_input.isFocusable = true
        et_search_input.requestFocus()
        //事件
        loadListener()
        // 加载搜索历史
        loadHistory()
        // 结果list
        initRecycler()
    }

    override fun layoutResId(): Int = R.layout.activity_search


    private val historyList:ArrayList<String> = arrayListOf()
    private val views: ArrayList<TextView> = arrayListOf()
    private var searchKey = ""
    private val resultList: ArrayList<O2SearchEntry>  = arrayListOf()

    private  val adapter: CommonRecycleViewAdapter<O2SearchEntry> by lazy { object : CommonRecycleViewAdapter<O2SearchEntry>(this, resultList, R.layout.item_search_result_list) {
        override fun convert(holder: CommonRecyclerViewHolder?, t: O2SearchEntry?) {
            if (t != null && holder != null) {
                if (t.type == "cms") {
                    holder.setText(R.id.tv_search_result_app_name, t.appName)
                        .setText(R.id.tv_search_result_type_name, getString(R.string.search_cms_category))
                        .setText(R.id.tv_search_result_type_value, t.categoryName)
                } else {
                    holder.setText(R.id.tv_search_result_app_name, t.applicationName)
                        .setText(R.id.tv_search_result_type_name, getString(R.string.search_process))
                        .setText(R.id.tv_search_result_type_value, t.processName)
                }
                holder.setText(R.id.tv_search_result_title, t.title)
                    .setText(R.id.tv_search_result_time, if(t.updateTime.length > 10) {t.updateTime.substring(0, 10)} else {t.updateTime} )
                    .setText(R.id.tv_search_result_summary, t.summary)
                    .setText(R.id.tv_search_result_dept, if (t.creatorUnit.contains("@")){t.creatorUnit.split("@")[0]}else{t.creatorUnit})
                    .setText(R.id.tv_search_result_person, if (t.creatorPerson.contains("@")){t.creatorPerson.split("@")[0]}else{t.creatorPerson})

            }
        }
    }}


    private fun initRecycler() {
        swipe_refresh_search_result.setColorSchemeResources(R.color.z_color_refresh_scuba_blue,
            R.color.z_color_refresh_red, R.color.z_color_refresh_purple, R.color.z_color_refresh_orange)
        swipe_refresh_search_result.setOnRefreshListener {
            XLog.debug("下拉刷新")
        }
        recycler_search_result_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_search_result_list.isNestedScrollingEnabled = false
        recycler_search_result_list.adapter = adapter

        adapter.setOnItemClickListener { _, position ->
            XLog.debug("点击了 position $position")
        }

    }

    private fun loadListener() {
        ll_search_delete_all_history_btn.setOnClickListener(this)
        // 输入框监听点击键盘搜索键的情况
        et_search_input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ) {
                XLog.debug("开始搜索")
                val key = et_search_input.text.toString()
                if (key.isNotBlank()) {
                    searchKey = key
                    addhistory(searchKey)
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

    override fun nextPage(model: O2SearchPageModel) {
        hideLoadingDialog()
        resultList.addAll(model.list)
        adapter.notifyDataSetChanged()
    }

    override fun searchResult(model: O2SearchPageModel) {
        hideLoadingDialog()
        if (model.list.isEmpty()) {
            ll_search_no_results.visible()
            ll_search_history.gone()
            swipe_refresh_search_result.gone()
        } else {
            ll_search_no_results.gone()
            ll_search_history.gone()
            swipe_refresh_search_result.visible()
            resultList.clear()
            resultList.addAll(model.list)
            adapter.notifyDataSetChanged()
        }

    }

    override fun error(err: String) {
        hideLoadingDialog()
        XToast.toastShort(this, err)
    }

    private fun search() {
        XLog.debug("搜索内容 $searchKey")
        et_search_input.clearFocus()
        ZoneUtil.toggleSoftInput(et_search_input, false)
        showLoadingDialog()
        mPresenter.search(searchKey)
    }

    private fun cleanInput() {
        XLog.debug("cleanInput ........")
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
        val distance = dip( 16f)
        for ((i, s) in historyList.withIndex()) {
            val view = LayoutInflater.from(this).inflate(R.layout.fragment_search_history_tag, frame_search_history_list, false) as TextView
            view.text = s
            frame_search_history_list.addView(view)
            view.setOnClickListener {
                XLog.debug("点击了 $s")
                searchKey = s
                et_search_input.setText(s)
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
        when(v?.id) {
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
        var historys = O2SDKManager.instance().prefs().getStringSet(O2.PRE_SEARCH_HISTORY_KEY, HashSet<String>())
        if (historys == null ) {
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
        var historys = O2SDKManager.instance().prefs().getStringSet(O2.PRE_SEARCH_HISTORY_KEY, setOf())
        if (historys != null ) {
            historyList.addAll(historys.map { it })
        }
    }

}