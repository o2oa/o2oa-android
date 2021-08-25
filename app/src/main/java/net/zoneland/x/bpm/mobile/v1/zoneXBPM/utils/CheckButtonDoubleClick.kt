package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils

/**
 * 按钮点击判断是否重复
 * Created by fancyLou on 2021-08-25.
 * Copyright © 2021 o2android. All rights reserved.
 */
object CheckButtonDoubleClick {

    private var records: HashMap<Int, Long> = HashMap()


    // 传入按钮的 资源ID 判断是不是同一个按钮点击
    fun isFastDoubleClick(buttonId: Int): Boolean {
        if (records.size > 1000) {
            records.clear()
        }

        var lastClickTime = records[buttonId]
        val thisClickTime = System.currentTimeMillis()
        records[buttonId] = thisClickTime
        if (lastClickTime == null) {
            lastClickTime = 0L
        }
        val timeDuration = thisClickTime - lastClickTime
        return timeDuration in 1..499 // 小于500毫秒算重复
    }
}