package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_main_app.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPViewPagerFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.ApplicationEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.persistence.MyAppListObject
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.BitmapUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderOptions

/**
 * Created by fancy on 2017/6/9.
 * Copyright © 2017 O2. All rights reserved.
 */

class AppFragment: BaseMVPViewPagerFragment<MyAppContract.View,MyAppContract.Presenter>(), MyAppContract.View{
    override var mPresenter: MyAppContract.Presenter = MyAppPresenter()
    override fun layoutResId(): Int = R.layout.fragment_main_app

    private val nativeAppBeanList = ArrayList<MyAppListObject>()
    private val portalAppBeanList = ArrayList<MyAppListObject>()
    private val myAppBeanList = ArrayList<MyAppListObject>()
    private val oldMyAppBeanList = ArrayList<MyAppListObject>()
    private var isEdit = false




    override fun initUI() {
        initNativeApp()
        initPortalApp()
        initMyApp()
        // 设置顶部大图 
        val path = O2CustomStyle.applicationTopImagePath(activity)
        if (!TextUtils.isEmpty(path)) {
            BitmapUtil.setImageFromFile(path!!, my_app_top_image)
        }
    }


    override fun lazyLoad() {
        mPresenter.getNativeAppList()
        mPresenter.getPortalAppList()
        mPresenter.getMyAppList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_my_app,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (isEdit) {
            menu.findItem(R.id.menu_app_edit)?.title = getString(R.string.completed)
        } else {
            menu.findItem(R.id.menu_app_edit)?.title = getString(R.string.edit)
        }
        if (activity is MainActivity) {
            (activity as MainActivity).refreshMenu()
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        isEdit = when (isEdit) {
            false -> {
                myAppEditAdapter.notifyDataSetChanged()
                nativeAppAdapter.notifyDataSetChanged()
                portalAppAdapter.notifyDataSetChanged()
                true
            }
            true -> {
                mPresenter.addAndDelMyAppList(oldMyAppBeanList, myAppBeanList)
                false
            }

        }
        return super.onOptionsItemSelected(item)
    }


    private fun initNativeApp(){
        native_app_recycler_view.layoutManager = GridLayoutManager(activity, 5)
        native_app_recycler_view.adapter = nativeAppAdapter
        native_app_recycler_view.isNestedScrollingEnabled = false
        nativeAppAdapter.setOnItemClickListener { _, position ->
            if (isEdit) {
                if (!nativeAppBeanList[position].isClick) {
                    nativeAppBeanList[position].isClick = true
                    myAppBeanList.add(nativeAppBeanList[position])
                    nativeAppAdapter.notifyItemChanged(position)
                    myAppEditAdapter.notifyItemInserted(myAppBeanList.size)
                }
            } else {
                IndexFragment.go(nativeAppBeanList[position].appId!!, activity!!, nativeAppBeanList[position].appTitle?:"")
            }
        }
    }

    private fun initPortalApp() {
        rv_portal_app.isNestedScrollingEnabled = false
        rv_portal_app.layoutManager = GridLayoutManager(activity, 5)
        rv_portal_app.adapter = portalAppAdapter
        portalAppAdapter.setOnItemClickListener { _, position ->
            if (isEdit) {
                if (!portalAppBeanList[position].isClick) {
                    portalAppBeanList[position].isClick = true
                    myAppBeanList.add(portalAppBeanList[position])
                    portalAppAdapter.notifyItemChanged(position)
                    myAppEditAdapter.notifyItemInserted(myAppBeanList.size)
                }
            } else {
                IndexFragment.go(portalAppBeanList[position].appId!!, activity!!, portalAppBeanList[position].appTitle?:"")
            }
        }

    }

    private fun initMyApp(){
        my_app_recycler_view.isNestedScrollingEnabled = false
        my_app_recycler_view.layoutManager = GridLayoutManager(activity, 5)
        my_app_recycler_view.adapter = myAppEditAdapter
        myAppEditAdapter.setOnItemClickListener { _, position ->
            if (isEdit) {
                nativeAppBeanList.forEachIndexed { index, myAppListObject ->
                    if (myAppListObject.appId == myAppBeanList[position].appId) {
                        myAppListObject.isClick = false
                        nativeAppAdapter.notifyItemChanged(index)
                    }
                }
                portalAppBeanList.forEachIndexed { index, myAppListObject ->
                    if (myAppListObject.appId == myAppBeanList[position].appId) {
                        myAppListObject.isClick = false
                        portalAppAdapter.notifyItemChanged(index)
                    }
                }
                myAppBeanList.removeAt(position)
                myAppEditAdapter.notifyItemRemoved(position)
                myAppEditAdapter.notifyItemRangeChanged(position,myAppBeanList.size)
            } else {
                IndexFragment.go(myAppBeanList[position].appId!!,activity!!, myAppBeanList[position].appTitle?:"")
            }
        }
    }


    override fun setAllAppList(allList: ArrayList<MyAppListObject>) {
    }

    override fun setNativeAppList(allList: ArrayList<MyAppListObject>) {
        nativeAppBeanList.clear()
        nativeAppBeanList.addAll(allList)
        nativeAppAdapter.notifyDataSetChanged()
    }

    override fun setPortalAppList(allList: ArrayList<MyAppListObject>) {
        portalAppBeanList.clear()
        portalAppBeanList.addAll(allList)
        portalAppAdapter.notifyDataSetChanged()
    }

    override fun setMyAppList(myAppList: ArrayList<MyAppListObject>) {
        oldMyAppBeanList.clear()
        oldMyAppBeanList.addAll(myAppList)
        myAppBeanList.clear()
        myAppBeanList.addAll(myAppList)
        for (app: MyAppListObject in nativeAppBeanList) {
            for (myApp: MyAppListObject in myAppList){
                if (app.appId == myApp.appId) {
                    app.isClick = true
                    break
                }
            }
        }
        for (app: MyAppListObject in portalAppBeanList) {
            for (myApp: MyAppListObject in myAppList){
                if (app.appId == myApp.appId) {
                    app.isClick = true
                    break
                }
            }
        }
        myAppEditAdapter.notifyDataSetChanged()
    }

    override fun addAndDelMyAppList(isSuccess: Boolean) {
        if (isSuccess) {
            oldMyAppBeanList.clear()
            oldMyAppBeanList.addAll(myAppBeanList)
            myAppEditAdapter.notifyDataSetChanged()
            nativeAppAdapter.notifyDataSetChanged()
            portalAppAdapter.notifyDataSetChanged()
        }
    }

    private val nativeAppAdapter: CommonRecycleViewAdapter<MyAppListObject> by lazy {
        object : CommonRecycleViewAdapter<MyAppListObject>(activity, nativeAppBeanList, R.layout.item_all_app_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: MyAppListObject?) {
                val resId = ApplicationEnum.getApplicationByKey(t?.appId)?.iconResId
                if (resId!=null) {
                    holder?.setImageViewResource(R.id.app_id, resId)
                }else {
                    if (t?.appId != null){
                        val portalIconUrl = APIAddressHelper.instance().getPortalIconUrl(t.appId!!)
                        val icon = holder?.getView<ImageView>(R.id.app_id)
                        if (icon !=null) {
                            O2ImageLoaderManager.instance().showImage(icon, portalIconUrl, O2ImageLoaderOptions(placeHolder = R.mipmap.process_default))
                        }
                    }
                }

                holder?.setText(R.id.app_name_id,t?.appTitle)
                if (isEdit) {
                    val delete = holder?.getView<ImageView>(R.id.delete_app_iv)
                    delete?.visibility = View.VISIBLE
                    if (t!!.isClick){
                        delete?.setImageResource(R.mipmap.icon__app_chose)
                    } else {
                        delete?.setImageResource(R.mipmap.icon_app_add)
                    }
                } else {
                    holder?.getView<ImageView>(R.id.delete_app_iv)?.visibility = View.GONE
                }
            }
        }
    }

    private val myAppEditAdapter: CommonRecycleViewAdapter<MyAppListObject> by lazy {
        object : CommonRecycleViewAdapter<MyAppListObject>(activity, myAppBeanList, R.layout.item_all_app_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: MyAppListObject?) {
                val resId = ApplicationEnum.getApplicationByKey(t?.appId)?.iconResId
                if (resId!=null) {
                    holder?.setImageViewResource(R.id.app_id, resId)
                }else {
                    if (t?.appId != null){
                        val portalIconUrl = APIAddressHelper.instance().getPortalIconUrl(t.appId!!)
                        val icon = holder?.getView<ImageView>(R.id.app_id)
                        if (icon !=null) {
                            O2ImageLoaderManager.instance().showImage(icon, portalIconUrl, O2ImageLoaderOptions(placeHolder = R.mipmap.process_default))
                        }
                    }
                }
                holder?.setText(R.id.app_name_id,t?.appTitle)
                if (isEdit) {
                    val delete = holder?.getView<ImageView>(R.id.delete_app_iv)
                    delete?.visibility = View.VISIBLE
                    delete?.setImageResource(R.mipmap.icon_app_del)
                    val text = holder?.getView<TextView>(R.id.app_name_id)
                    text?.visibility = View.VISIBLE
                    text?.text = t?.appTitle
                } else {
                    holder?.getView<ImageView>(R.id.delete_app_iv)?.visibility = View.GONE
//                    holder?.getView<TextView>(R.id.app_name_id)?.visibility = View.GONE
                }
            }
        }
    }

    private val portalAppAdapter: CommonRecycleViewAdapter<MyAppListObject> by lazy {
        object : CommonRecycleViewAdapter<MyAppListObject>(activity, portalAppBeanList, R.layout.item_all_app_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: MyAppListObject?) {
                val resId = ApplicationEnum.getApplicationByKey(t?.appId)?.iconResId
                if (resId!=null) {
                    holder?.setImageViewResource(R.id.app_id, resId)
                }else {
                    if (t?.appId != null){
                        val portalIconUrl = APIAddressHelper.instance().getPortalIconUrl(t.appId!!)
                        val icon = holder?.getView<ImageView>(R.id.app_id)
                        if (icon !=null) {
                            O2ImageLoaderManager.instance().showImage(icon, portalIconUrl, O2ImageLoaderOptions(placeHolder = R.mipmap.process_default))
                        }
                    }
                }
                holder?.setText(R.id.app_name_id,t?.appTitle)
                if (isEdit) {
                    val delete = holder?.getView<ImageView>(R.id.delete_app_iv)
                    delete?.visibility = View.VISIBLE
                    if (t!!.isClick){
                        delete?.setImageResource(R.mipmap.icon__app_chose)
                    } else {
                        delete?.setImageResource(R.mipmap.icon_app_add)
                    }
                } else {
                    holder?.getView<ImageView>(R.id.delete_app_iv)?.visibility = View.GONE
                }
            }
        }
    }
}