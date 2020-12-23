package com.chat.albumlib

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


/**
 * create by caiyx in 2020/12/21
 *
 * 相册的统一管理中心
 */
object AlbumControl {

    const val IMAGE = 1
    const val VIDEO = 1 shl 1
    const val IMAGEANDVIDEO = IMAGE or VIDEO

    val mianHandler = Handler(Looper.getMainLooper())

    internal var initImageFinish = AtomicBoolean(false)
    internal var initVideoFinish = AtomicBoolean(false)

    internal var allImage: ArrayList<Image>? = null
    internal var imageDirectories: ArrayList<String>? = null
    internal var imageOfSort: HashMap<String, ArrayList<Image>>? = null

    internal var allVideo: ArrayList<Image>? = null
    internal var videoDirectories: ArrayList<String>? = null
    internal var videoOfSort: HashMap<String, ArrayList<Image>>? = null

    private var initImage = false

    internal fun initImage(context: Context) {

        if (initImage) return
        initImage = true

        thread {
            val data = ImageScanner().execute(context)

            val tempDirectories = ArrayList<String>()
            val tempDataOfSort = HashMap<String, ArrayList<Image>>()

            parseDirectory(data, tempDirectories, tempDataOfSort)

            allImage = data
            imageDirectories = tempDirectories
            imageOfSort = tempDataOfSort

            Log.d("xx", "图片准备好了，一共找到数量为${allImage!!.size}的图片，一共分为${imageDirectories!!.size}个目录")

            initImageFinish.set(true)

            // 存在异步需要通知一下外面
            mianHandler.post {
                listens.forEach { it.onInitFinish() }
            }
        }
    }

    private var initVideo = false

    internal fun initVideo(context: Context) {

        if (initVideo) return
        initVideo = true

        thread {
            val data = VideoScanner().execute(context)

            val tempDirectories = ArrayList<String>()
            val tempDataOfSort = HashMap<String, ArrayList<Image>>()

            parseDirectory(data, tempDirectories, tempDataOfSort)

            allVideo = data
            videoDirectories = tempDirectories
            videoOfSort = tempDataOfSort

            Log.d("xx", "图片准备好了，一共找到数量为${allVideo!!.size}的视频，一共分为${videoDirectories!!.size}个目录")

            initVideoFinish.set(true)

            // 存在异步需要通知一下外面
            mianHandler.post {
                listens.forEach { it.onInitFinish() }

            }
        }
    }

    private fun parseDirectory(
        data: List<Image>,
        directories: ArrayList<String>,
        dataOfSort: HashMap<String, ArrayList<Image>>
    ) {
        for (d in data) {

            val fileName = File(d.path).parentFile?.name ?: continue

            if (!directories.contains(fileName)) {
                directories.add(fileName)
            }

            var imageList = dataOfSort[fileName]
            if (imageList == null) {
                imageList = ArrayList()
                dataOfSort[fileName] = imageList
            }

            imageList.add(d)
        }
    }

    val listens = ArrayList<AlbumControlListen>()

    interface AlbumControlListen {
        fun onInitFinish()
    }
}