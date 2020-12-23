package com.chat.albumlib

import android.content.Context
import android.provider.MediaStore

/**
 * create by caiyx in 2020/12/22
 */
class VideoScanner : BaseScanner() {


    companion object {

        const val MIME_MP4 = "video/mp4"
        const val MIME_AVI = "video/x-sgi-movie"

        // 目前扫描的格式
        val sScannerMime = arrayOf(MIME_MP4, MIME_AVI)
    }

    /**
     * 目前扫描的格式：mp4,avi
     */
    override fun execute(context: Context): ArrayList<Image> {
        val mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            mediaUri,
            arrayOf(
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.MIME_TYPE
            ),
            "${MediaStore.Video.Media.MIME_TYPE}=? or ${MediaStore.Video.Media.MIME_TYPE}=?",
            arrayOf("video/mp4", "video/x-sgi-movie"),
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val results = ArrayList<Image>()

        while (cursor?.moveToNext() == true) {

            // 解析
            val image = Image()
            image.type = AlbumControl.VIDEO
            image.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
            image.fileName =
                cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
            image.mineType =
                cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
            image.time =
                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))
            results.add(image)
        }

        cursor?.close()
        return results
    }
}