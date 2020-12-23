package com.chat.albumlib

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat.albumlib.ui.AlbumAdapter
import com.chat.albumlib.ui.DirectoryAdapter
import com.chat.albumlib.util.PermissionUtil
import kotlinx.android.synthetic.main.activity_album.*

/**
 * create by caiyx in 2020/12/21
 *
 * 相册的UI
 */
class AlbumActivity : AppCompatActivity(), View.OnClickListener {

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
            if (initData) {
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
        initListen()
        initData(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        AlbumControl.listens.remove(albumControlListen)
    }

    private var albumAdapter: AlbumAdapter? = null
    private var directoryAdapter: DirectoryAdapter? = null

    private fun initView() {
        val lm = GridLayoutManager(this, 3)
        rv_album.layoutManager = lm
        albumAdapter = AlbumAdapter()
        rv_album.adapter = albumAdapter

        val lm1 = LinearLayoutManager(this)
        lm1.orientation = LinearLayoutManager.VERTICAL
        rv_directory_select.layoutManager = lm1
        directoryAdapter = DirectoryAdapter()
        rv_directory_select.adapter = directoryAdapter
    }

    private fun initListen() {
        iv_close.setOnClickListener(this)
        fl_directory_bn.setOnClickListener(this)
        vw_directory_select_mask.setOnClickListener(this)

        directoryAdapter?.listen = object : DirectoryAdapter.DirectoryAdapterListen {
            override fun onClickDirectory(directory: String) {
                setCurPanel(directory)
                hideDirectoryLayout()
            }
        }
    }

    override fun onClick(v: View?) {

        if (inAnim) {
            return
        }

        when (v) {
            iv_close -> {
                finish()
            }
            fl_directory_bn -> {
                if (isShowDirectory) {
                    hideDirectoryLayout()
                } else {
                    showDirectoryLayout()
                }
            }
            vw_directory_select_mask -> {
                hideDirectoryLayout()
            }
        }
    }

    private fun initData(intent: Intent?) {
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

    private var curDirectory = "ALL" // 当前正在选址的路径

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
                allList.addAll(AlbumControl.allImage ?: ArrayList())

                // 按照路径添加
                for (p in AlbumControl.imageDirectories ?: ArrayList()) {

                    // 目录不存在
                    if (!directories.contains(p)) {
                        directories.add(p)
                    }

                    var imageList = allImage[p]
                    if (imageList == null) {
                        imageList = ArrayList()
                        allImage[p] = imageList
                    }

                    imageList.addAll(AlbumControl.imageOfSort?.get(p) ?: ArrayList())
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
                allList.addAll(AlbumControl.allVideo ?: ArrayList())

                // 按照路径添加
                for (p in AlbumControl.videoDirectories ?: ArrayList()) {

                    // 目录不存在
                    if (!directories.contains(p)) {
                        directories.add(p)
                    }

                    var imageList = allImage[p]
                    if (imageList == null) {
                        imageList = ArrayList()
                        allImage[p] = imageList
                    }
                    imageList.addAll(AlbumControl.videoOfSort?.get(p) ?: ArrayList())
                }

                initTypeFinish = initTypeFinish or AlbumControl.VIDEO
            }
        }

        setCurPanel("ALL")
        initDirectoryLayout()
    }

    /**
     * 设置当前选择图片路径
     */
    private fun setCurPanel(directory: String) {
        curDirectory = directory
        albumAdapter?.setData(allImage[directory] ?: ArrayList())
        albumAdapter?.notifyDataSetChanged()
        rv_album.scrollToPosition(0)

        // 设置当前路径名
        tv_directory.text = AlbumControl.getDirectoryName(directory)
    }

    //*******************路径选择***********************

    private var inAnim = false
    private var isShowDirectory = false

    private fun initDirectoryLayout() {
        directoryAdapter?.images = allImage
        directoryAdapter?.directories = directories
        directoryAdapter?.notifyDataSetChanged()
    }

    private fun showDirectoryLayout() {
        val alpha = AlphaAnimation(0f, 1f)
        alpha.duration = 200
        vw_directory_select_mask.startAnimation(alpha)
        fl_directory_select.visibility = View.VISIBLE

        rv_directory_select.visibility = View.INVISIBLE
        val anim = TranslateAnimation(
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            -1f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0f
        )
        anim.duration = 200
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                inAnim = false
                isShowDirectory = true
            }

            override fun onAnimationStart(animation: Animation?) {
                rv_directory_select.visibility = View.VISIBLE
            }
        })
        inAnim = true
        rv_directory_select.startAnimation(anim)

    }

    private fun hideDirectoryLayout() {
        val anim = TranslateAnimation(
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            0f,
            TranslateAnimation.RELATIVE_TO_SELF,
            -1f
        )
        anim.duration = 200
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                rv_directory_select.visibility = View.INVISIBLE
                fl_directory_select.visibility = View.GONE
                isShowDirectory = false
                inAnim = false
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        inAnim = true
        rv_directory_select.startAnimation(anim)

        val alpha = AlphaAnimation(1f, 0f)
        alpha.duration = 200
        vw_directory_select_mask.startAnimation(alpha)
    }

    //******************************************

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