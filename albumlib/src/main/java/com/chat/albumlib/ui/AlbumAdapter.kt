package com.chat.albumlib.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chat.albumlib.AlbumControl
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

    var listen: AlbumAdapterListen? = null

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
            holder.setSelectStatus(images[position])
            holder.setTimeNeed(images[position])
            holder.setSelectListen(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    listen?.onSelectStatus(
                        images[position],
                        !Image.isSelectStatus(selectImages, images[position])
                    )
                }
            })
            holder.setItemClick(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    listen?.onClickPreview(images[position], position)
                }
            })
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

        fun setSelectStatus(image: Image) {
            val isSelect = Image.isSelectStatus(selectImages, image)
            if (isSelect) {
                itemView.findViewById<ImageView>(R.id.iv_select_bg)
                    .setImageResource(R.drawable.ic_album_choose)
                itemView.findViewById<TextView>(R.id.tv_select_item).text =
                    Image.inListItem(selectImages, image).toString()
                itemView.findViewById<View>(R.id.vw_mask).visibility = View.VISIBLE
            } else {
                itemView.findViewById<ImageView>(R.id.iv_select_bg)
                    .setImageResource(R.drawable.ic_album_unchoose)
                itemView.findViewById<TextView>(R.id.tv_select_item).text = ""
                itemView.findViewById<View>(R.id.vw_mask).visibility = View.GONE
            }
        }

        fun setTimeNeed(image: Image) {
            if (image.type == AlbumControl.IMAGE) {
                itemView.findViewById<TextView>(R.id.tv_time).visibility = View.GONE
            } else {
                itemView.findViewById<TextView>(R.id.tv_time).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.tv_time).text = getDuration(image.duration)
            }
        }

        fun setSelectListen(listen: View.OnClickListener) {
            itemView.findViewById<FrameLayout>(R.id.fl_select).setOnClickListener(listen)
        }

        fun setItemClick(listen: View.OnClickListener) {
            itemView.setOnClickListener(listen)
        }
    }

    private fun getDuration(duration: Long): String {
        val hour = duration / (1000 * 60 * 60)
        val minute = duration % (1000 * 60 * 60) / (1000 * 60)
        val minute00 = if (minute > 10) {
            "$minute"
        } else {
            "0$minute"
        }
        val second = duration % (1000 * 60) / 1000
        val second00 = if (second > 10) {
            "$second"
        } else {
            "0$second"
        }
        return if (hour == 0L) {
            "$minute00:$second00 "
        } else {
            "$hour:$minute00:$second00 "
        }

    }

    interface AlbumAdapterListen {
        fun onSelectStatus(image: Image, selectStatus: Boolean)
        fun onClickPreview(image: Image, position: Int)
    }
}