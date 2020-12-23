package com.chat.albumlib.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chat.albumlib.AlbumControl
import com.chat.albumlib.Image
import com.chat.albumlib.R
import com.chat.albumlib.util.CommonUtils

/**
 * create by caiyx in 2020/12/23
 */
class DirectoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var directories = ArrayList<String>()
    var images = HashMap<String, ArrayList<Image>>()

    var listen: DirectoryAdapterListen? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_directory_select, null)
        val lp = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            CommonUtils.dp2px(parent.context, 60f)
        )
        view.layoutParams = lp
        return DirectoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DirectoryViewHolder) {
            holder.setLogo(images[directories[position]]?.get(0)?.path ?: "")
            holder.setDirectoryName(directories[position])
            holder.setClick(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    listen?.onClickDirectory(directories[position])
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return directories.size
    }

    class DirectoryViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun setLogo(path: String) {
            Glide.with(itemView.context).load(path)
                .into(itemView.findViewById<ImageView>(R.id.iv_logo))
        }

        fun setDirectoryName(path: String) {
            itemView.findViewById<TextView>(R.id.tv_directory_path).text =
                AlbumControl.getDirectoryName(path)
        }

        fun setClick(listen: View.OnClickListener) {
            itemView.setOnClickListener(listen)
        }
    }

    interface DirectoryAdapterListen {
        fun onClickDirectory(directory: String)
    }
}