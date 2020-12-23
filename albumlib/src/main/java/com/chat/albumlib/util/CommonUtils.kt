package com.chat.albumlib.util

import android.content.Context
import android.view.WindowManager

/**
 * create by caiyx in 2020/12/23
 */
object CommonUtils {

    @JvmStatic
    fun getDisplayWidth(c: Context): Int {
        val wm: WindowManager = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.width
    }

    @JvmStatic
    fun getDisplayHeight(c: Context): Int {
        val wm: WindowManager = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm.defaultDisplay.height
    }
}