package com.chat.album

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.chat.ablum.R
import com.chat.albumlib.AlbumActivity
import com.chat.albumlib.AlbumControl
import com.chat.albumlib.Image
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

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
                AlbumActivity.start(this, list1, AlbumControl.IMAGE, 5, 1)
            }
            bn_2 -> {
                AlbumActivity.start(this, list2, AlbumControl.VIDEO, 6, 2)
            }
            bn_3 -> {
                AlbumActivity.start(this, list3, AlbumControl.IMAGEANDVIDEO, 6, 3)
            }
        }
    }

    var list1 = ArrayList<Image>()
    var list2 = ArrayList<Image>()
    var list3 = ArrayList<Image>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            list1 = data?.getSerializableExtra(AlbumActivity.KEY_SELECT_DATA) as? ArrayList<Image> ?: ArrayList()
        } else if (requestCode == 2) {
            list2 = data?.getSerializableExtra(AlbumActivity.KEY_SELECT_DATA) as? ArrayList<Image> ?: ArrayList()
        } else if (requestCode == 3) {
            list3 = data?.getSerializableExtra(AlbumActivity.KEY_SELECT_DATA) as? ArrayList<Image> ?: ArrayList()
        }
    }
}
