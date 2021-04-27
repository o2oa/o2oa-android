/*
 * Copyright 2017 jiajunhui<junhui_jia@163.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.bigkoo.convenientbanner;

import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

/**
 * Created by ShadowWalker on 2018/8/27
 * A util that can change a rectangle to round or oval
 * it only support SDK_INT >= 21 since.
 */

public class ViewStyleSetter {

    private View mView;

    public ViewStyleSetter(View view) {
        this.mView = view;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setRound(float radius) {
        this.mView.setClipToOutline(true);//用outline裁剪内容区域
        this.mView.setOutlineProvider(new RoundViewOutlineProvider(radius));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setOval() {
        this.mView.setClipToOutline(true);//用outline裁剪内容区域
        this.mView.setOutlineProvider(new OvalViewOutlineProvider());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void clearShapeStyle() {
        this.mView.setClipToOutline(false);
    }
}