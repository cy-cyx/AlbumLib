package com.chat.albumlib

import android.content.Context

/**
 * create by caiyx in 2020/12/22
 */
abstract class BaseScanner {
    abstract fun execute(context: Context): ArrayList<Image>
}