package net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by fancyLou on 2021-04-27.
 * Copyright © 2021 O2. All rights reserved.
 */
class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = space
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space
        }
    }
}