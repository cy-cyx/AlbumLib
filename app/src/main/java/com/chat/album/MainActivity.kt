package com.chat.album

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chat.ablum.R
import com.chat.albumlib.AlbumActivity
import com.chat.albumlib.AlbumControl
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListen()
    }

    private fun initListen() {
        bn_1.setOnClickListener(this)
        bn_2.setOnClickListener(this)
        bn_3.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            bn_1 -> {
                AlbumActivity.start(this, arrayListOf(), AlbumControl.IMAGE)
            }
            bn_2 -> {
                AlbumActivity.start(this, arrayListOf(), AlbumControl.VIDEO)
            }
            bn_3 -> {
                AlbumActivity.start(this, arrayListOf(), AlbumControl.IMAGEANDVIDEO)
            }
        }
    }

}
