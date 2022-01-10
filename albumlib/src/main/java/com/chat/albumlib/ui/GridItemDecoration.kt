package com.chat.albumlib.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.recyclerview.widget.RecyclerView
import com.chat.albumlib.util.CommonUtils


/**
 * create by caiyx in 2020/12/24
 */
class GridItemDecoration(var context: Context) : RecyclerView.ItemDecoration() {

    private val dividerPaint: Paint = Paint()
    private val dividerSize = CommonUtils.dp2px(context, 1f)

    init {
        dividerPaint.color = Color.parseColor("#ffffff")
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        drawLine(c, parent)
    }

    private fun drawLine(c: Canvas, parent: RecyclerView) {
        //获得RecyclerView中总条目数量
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val childView = parent.getChildAt(i)

            val x = childView.x
            val y = childView.y
            val width = childView.width
            val height = childView.height

            // 画right
            c.drawRect(RectF(x + width - dividerSize, y, x + width, y + height), dividerPaint)
            // 画bottom
            c.drawRect(RectF(x, y + height - dividerSize, x + width, y + height), dividerPaint)
        }
    }
}