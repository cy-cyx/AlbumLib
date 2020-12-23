package com.chat.albumlib.util

import android.content.Context
import android.util.TypedValue
import android.view.WindowManager

/**
 * create by caiyx in 2020/12/23
 */
object CommonUtils {

    @JvmStatic
    fun dp2px(context: Context, dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            context.resources.displayMetrics
        ).toInt()
    }

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