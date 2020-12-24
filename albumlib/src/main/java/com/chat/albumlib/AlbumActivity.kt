package com.chat.albumlib

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chat.albumlib.ui.AlbumAdapter
import com.chat.albumlib.ui.DirectoryAdapter
import com.chat.albumlib.ui.GridItemDecoration
import com.chat.albumlib.util.PermissionUtil
import kotlinx.android.synthetic.main.activity_album.*

/**
 * create by caiyx in 2020/12/21
 *
 * 相册的UI
 */
class AlbumActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        fun start(
            context: Activity,
            data: ArrayList<Image>,
            type: Int,
            selectSize: Int,
            requestCode: Int
        ) {
            PermissionUtil
                .requestRuntimePermissions(context, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), object : PermissionUtil.IPermissionCallback {
                    override fun nextStep() {
                        AlbumControl.initImage(context.applicationContext)
                        AlbumControl.initVideo(context.applicationContext)
                        startInner(context, data, type, selectSize, requestCode)
                    }

                    override fun cancel() {

                    }

                })
        }

        const val KEY_SELECT_DATA = "key_select_data"
        const val KEY_TYPE = "key_type"
        const val KEY_MAX_SELECT_SIZE = "key_max_select_size"

        fun startInner(
            context: Activity,
            data: ArrayList<Image>,
            type: Int,
            selectSize: Int,
            requestCode: Int
        ) {
            val intent = Intent(context, AlbumActivity::class.java)
            intent.putExtra(KEY_SELECT_DATA, data)
            intent.putExtra(KEY_TYPE, type)
            intent.putExtra(KEY_MAX_SELECT_SIZE, selectSize)
            context.startActivityForResult(intent, requestCode)
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
    private var maxSelectSize = 9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlbumControl.listens.add(albumControlListen)
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
        rv_album.addItemDecoration(GridItemDecoration(this))

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
        tv_done.setOnClickListener(this)

        iv_preview_close.setOnClickListener(this)
        fl_preview_select.setOnClickListener(this)

        directoryAdapter?.listen = object : DirectoryAdapter.DirectoryAdapterListen {
            override fun onClickDirectory(directory: String) {
                setCurPanel(directory)
                hideDirectoryLayout()
            }
        }

        albumAdapter?.listen = object : AlbumAdapter.AlbumAdapterListen {
            override fun onSelectStatus(image: Image, selectStatus: Boolean) {
                setSelectStatus(image, selectStatus)
            }

            override fun onClickPreview(image: Image, position: Int) {
                clickPreview(image)
            }

        }
    }

    override fun onClick(v: View?) {

        if (inAnim) {
            return
        }

        when (v) {
            tv_done -> {
                if (selectData.isEmpty()) {
                    Toast.makeText(this, "Select at least one", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent()
                    intent.putExtra(KEY_SELECT_DATA, selectData)
                    setResult(200, intent)
                    finish()
                }
            }
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
            iv_preview_close -> {
                hidePreviewLayout()
            }
            fl_preview_select -> {
                clickPreviewSelect()
            }
        }
    }

    private fun initData(intent: Intent?) {
        selectData =
            intent?.getSerializableExtra(KEY_SELECT_DATA) as? ArrayList<Image> ?: ArrayList()
        selectType =
            intent?.getIntExtra(KEY_TYPE, AlbumControl.IMAGEANDVIDEO) ?: AlbumControl.IMAGEANDVIDEO
        maxSelectSize = intent?.getIntExtra(KEY_MAX_SELECT_SIZE, 9) ?: 9

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

                Log.d("xx", "初始图片")

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

            Log.d("xx", "初始视频")

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

    /**
     * 列表的选中和未选
     */
    private fun setSelectStatus(image: Image, selectStatus: Boolean) {
        if (selectStatus) {
            if (selectData.size >= maxSelectSize) {
                Toast.makeText(this, "Upload up to ${maxSelectSize} images", Toast.LENGTH_SHORT)
                    .show()
            } else {
                selectData.add(image.clone())

                // 刷新显示状态
                albumAdapter?.notifyDataSetChanged()
            }
        } else {
            var temp: Image? = null
            for (m in selectData) {
                if (m.equals(image)) {
                    temp = m
                    break
                }
            }
            selectData.remove(temp)

            // 刷新显示状态
            albumAdapter?.notifyDataSetChanged()
        }

        tv_done.text = "Done (${selectData.size})"
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

    //********************预览选中*************************

    var curPreviewImage: Image? = null

    private fun clickPreview(image: Image) {

        if (image.type == AlbumControl.VIDEO) {
            Toast.makeText(this, "Preview is temporarily not supported", Toast.LENGTH_SHORT).show()
            return
        }

        curPreviewImage = image

        val options = RequestOptions().placeholder(R.drawable.ic_placeholder)
        Glide.with(this).load(image.path).apply(options)
            .into(ziv_preview)

        showPreviewLayout()

        upDataPreviewSelectStatus()
    }

    private var isShowPreviewLayout = false

    private fun showPreviewLayout() {
        ll_preview.visibility = View.INVISIBLE
        val scale = ScaleAnimation(
            0.8f,
            1f,
            0.8f,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )


        val alpha = AlphaAnimation(0f, 1f)
        vw_directory_select_mask.startAnimation(alpha)

        val animSet = AnimationSet(false)
        animSet.addAnimation(alpha)
        animSet.addAnimation(scale)

        animSet.duration = 200
        animSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                inAnim = false
                isShowPreviewLayout = true
            }

            override fun onAnimationStart(animation: Animation?) {
                ll_preview.visibility = View.VISIBLE
            }
        })

        inAnim = true

        ll_preview.startAnimation(animSet)
    }

    private fun hidePreviewLayout() {
        val scale = ScaleAnimation(
            1f,
            0.8f,
            1f,
            0.8f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )


        val alpha = AlphaAnimation(1f, 0f)
        vw_directory_select_mask.startAnimation(alpha)

        val animSet = AnimationSet(false)
        animSet.addAnimation(alpha)
        animSet.addAnimation(scale)

        animSet.duration = 200
        animSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                inAnim = false
                isShowPreviewLayout = false
                ll_preview.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
                ll_preview.visibility = View.VISIBLE
            }
        })

        inAnim = true
        ll_preview.startAnimation(animSet)
    }

    private fun upDataPreviewSelectStatus() {
        curPreviewImage?.let {
            val select = Image.isSelectStatus(selectData, it)

            if (select) {
                iv_preview_select_bg.setImageResource(R.drawable.ic_album_choose)
                tv_preview_select_item.text = Image.inListItem(selectData, it).toString()
            } else {
                iv_preview_select_bg.setImageResource(R.drawable.ic_album_unchoose)
                tv_preview_select_item.text = ""
            }
        }
    }

    private fun clickPreviewSelect() {
        curPreviewImage?.let {
            val select = Image.isSelectStatus(selectData, it)
            setSelectStatus(it, !select)

            upDataPreviewSelectStatus()
        }
    }

    //**********************************************

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


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (inAnim) {
                return true
            }

            if (isShowPreviewLayout) {
                hidePreviewLayout()
                return true
            }

            if (isShowDirectory) {
                hideDirectoryLayout()
                return true
            }

        }
        return super.onKeyDown(keyCode, event)
    }
}