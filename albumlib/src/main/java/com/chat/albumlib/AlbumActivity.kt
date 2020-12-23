package com.chat.albumlib

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chat.albumlib.ui.AlbumAdapter
import com.chat.albumlib.util.PermissionUtil
import kotlinx.android.synthetic.main.activity_album.*

/**
 * create by caiyx in 2020/12/21
 *
 * 相册的UI
 */
class AlbumActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, data: ArrayList<Image>, type: Int) {
            PermissionUtil
                .requestRuntimePermissions(context, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), object : PermissionUtil.IPermissionCallback {
                    override fun nextStep() {
                        AlbumControl.initImage(context.applicationContext)
                        AlbumControl.initVideo(context.applicationContext)
                        startInner(context, data, type)
                    }

                    override fun cancel() {

                    }

                })
        }

        const val KEY_SELECT_DATA = "key_select_data"
        const val KEY_TYPE = "key_type"

        fun startInner(context: Context, data: ArrayList<Image>, type: Int) {
            val intent = Intent(context, AlbumActivity::class.java)
            intent.putExtra(KEY_SELECT_DATA, data)
            intent.putExtra(KEY_TYPE, type)
            context.startActivity(intent)
        }

    }

    private val albumControlListen = object : AlbumControl.AlbumControlListen {
        override fun onInitFinish() {
            if (initData){
                initData()
            }
        }
    }

    private var selectData = ArrayList<Image>()
    private var selectType = AlbumControl.IMAGEANDVIDEO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        initView()
        init(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        AlbumControl.listens.remove(albumControlListen)
    }

    private var albumAdapter: AlbumAdapter? = null

    private fun initView() {
        val lm = GridLayoutManager(this, 3)
        rv_album.layoutManager = lm
        albumAdapter = AlbumAdapter()
        rv_album.adapter = albumAdapter
    }

    private fun init(intent: Intent?) {
        selectData =
            intent?.getSerializableExtra(KEY_SELECT_DATA) as? ArrayList<Image> ?: ArrayList()
        selectType =
            intent?.getIntExtra(KEY_TYPE, AlbumControl.IMAGEANDVIDEO) ?: AlbumControl.IMAGEANDVIDEO

        albumAdapter?.selectImages = selectData

        resetData()
        initData()
    }

    // 已经显示的数据类型（由于数据异步加载）
    private var initTypeFinish = 0

    private var directories = ArrayList<String>()  /*需要包括All*/
    private var allImage = HashMap<String, ArrayList<Image>>()

    private var curDirectory = "ALL"

    /**
     * singTop
     */
    private fun resetData() {
        initTypeFinish = 0

        directories.clear()
        directories.add("ALL")

        allImage.clear()
        allImage["ALL"] = ArrayList<Image>()

        curDirectory = "ALL"
    }

    private var initData = false

    private fun initData() {
        initData = true
        // 支持选择图片
        if ((selectType and AlbumControl.IMAGE) == AlbumControl.IMAGE) {

            // 原数据准备好,还没初始化数据源
            if (AlbumControl.initImageFinish.get() && (initTypeFinish and AlbumControl.IMAGE) != AlbumControl.IMAGE) {

                // ALL
                var allList = allImage["ALL"]
                if (allList == null) {
                    allList = ArrayList()
                    allImage["ALL"] = allList
                }
                allList.addAll(AlbumControl.allImage!!)

                // 按照路径添加
                for (p in AlbumControl.imageDirectories!!) {

                    // 目录不存在
                    if (!directories.contains(p)) {
                        directories.add(p)
                    }

                    var imageList = AlbumControl.imageOfSort!![p]
                    if (imageList == null) {
                        imageList = ArrayList()
                        allImage[p] = imageList
                    }
                }

                initTypeFinish = initTypeFinish or AlbumControl.IMAGE
            }
        }

        // 支持选择视频
        if ((selectType and AlbumControl.VIDEO) == AlbumControl.VIDEO) {

            // 原数据准备好,还没初始化数据源
            if (AlbumControl.initVideoFinish.get() && (initTypeFinish and AlbumControl.VIDEO) != AlbumControl.VIDEO) {

                // ALL
                var allList = allImage["ALL"]
                if (allList == null) {
                    allList = ArrayList()
                    allImage["ALL"] = allList
                }
                allList.addAll(AlbumControl.allVideo!!)

                // 按照路径添加
                for (p in AlbumControl.videoDirectories!!) {

                    // 目录不存在
                    if (!directories.contains(p)) {
                        directories.add(p)
                    }

                    var imageList = AlbumControl.videoOfSort!![p]
                    if (imageList == null) {
                        imageList = ArrayList()
                        allImage[p] = imageList
                    }
                }

                initTypeFinish = initTypeFinish or AlbumControl.VIDEO
            }
        }

        setCurPanel("ALL")
    }

    private fun setCurPanel(directory: String) {
        curDirectory = directory
        albumAdapter?.setData(allImage[directory] ?: ArrayList())
        albumAdapter?.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PermissionUtil.OnActivityResult(this, requestCode, resultCode, data)
    }

}