package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.tbs;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tencent.smtt.sdk.TbsReaderView;

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileUtil;

import java.io.File;

public class WordReadView extends FrameLayout implements TbsReaderView.ReaderCallback {

    private static final String TAG = "WordReadView";
    private static TbsReaderView mTbsView;

    public WordReadView(Context context) {
        this(context, null);
    }

    public WordReadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WordReadView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WordReadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //init();
    }

    private void init() {
        mTbsView = new TbsReaderView(getContext(), this);
        this.addView(mTbsView);
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }


    public void loadFile(String filePath) {
        if (filePath == null) {
            return;
        }
        if (mTbsView != null) {
            mTbsView.onStop();
        }
        this.removeAllViews();
        mTbsView = new TbsReaderView(getContext(), this);
        //去除最近文件按钮以及设置背景色为浅灰色
        mTbsView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View childView) {
                if (childView instanceof FrameLayout) {
                    final FrameLayout frameLayout = (FrameLayout) childView;
                    for (int i = 0; i < frameLayout.getChildCount(); i++) {
                        View view = frameLayout.getChildAt(i);
                        if (view.getClass().getSimpleName().contains("FileReaderContentView")) {
                            view.setBackgroundColor(Color.parseColor("#F5F6FA"));
                            break;
                        }
                    }
                    mTbsView.setOnHierarchyChangeListener(null);
                    frameLayout.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View childView) {
                            if (childView.getClass().getSimpleName().equals("MenuView")) {
                                childView.setVisibility(View.GONE);
                                frameLayout.setOnHierarchyChangeListener(null);
                                //移除监听
                            }
                        }

                        @Override
                        public void onChildViewRemoved(View view, View view1) {

                        }
                    });
                }
            }

            @Override
            public void onChildViewRemoved(View view, View view1) {

            }
        });
        this.addView(mTbsView);
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        File tempPath = FileUtil.INSTANCE.appExternalCacheDir(getContext());
        if (tempPath != null) {
            bundle.putString("tempPath", tempPath.getPath() + File.separator + "tbs");
        }
        boolean result = mTbsView.preOpen(parseFileType(filePath), false);
        if (result) {
            mTbsView.openFile(bundle);
        } else {
            Log.e(TAG, "Type is not support");
        }

    }

    public void destroy() {
        if (mTbsView != null) {
            mTbsView.onStop();
        }
    }

    private String parseFileType(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        } else {
            return path.substring(path.lastIndexOf(".") + 1);
        }
    }


}