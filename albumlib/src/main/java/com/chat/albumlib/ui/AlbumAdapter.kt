package com.chat.albumlib.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chat.albumlib.Image
import com.chat.albumlib.R
import com.chat.albumlib.util.CommonUtils

/**
 * create by caiyx in 2020/12/23
 */
class AlbumAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var images = ArrayList<Image>()

    // 当前选中的集合用于显示选中
    var selectImages = ArrayList<Image>()

    fun setData(data: ArrayList<Image>) {
        images.clear()
        images.addAll(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, null)
        val lp = RecyclerView.LayoutParams(
            CommonUtils.getDisplayWidth(parent.context) / 3,
            CommonUtils.getDisplayWidth(parent.context) / 3
        )
        view.layoutParams = lp
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AlbumViewHolder) {
            holder.setImageContent(images[position].path)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class AlbumViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun setImageContent(path: String) {
            val options = RequestOptions().placeholder(R.drawable.ic_placeholder)
            Glide.with(itemView.context).load(path).apply(options)
                .into(itemView.findViewById<ImageView>(R.id.iv_content))
        }

    }
}