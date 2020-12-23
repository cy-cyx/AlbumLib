package com.chat.albumlib

import android.content.Context
import android.provider.MediaStore

/**
 * create by caiyx in 2020/12/22
 */
class ImageScanner : BaseScanner() {

    companion object {
        const val MIME_JPEG = "image/jpeg"
        const val MIME_PNG = "image/png"

        // 目前扫描的格式
        val sScannerMime = arrayOf(MIME_JPEG, MIME_PNG)
    }

    /**
     * 目前扫描的格式：jpeg,png
     */
    override fun execute(context: Context): ArrayList<Image> {
        val mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            mediaUri,
            arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.MIME_TYPE
            ),
            "${MediaStore.Images.Media.MIME_TYPE}=? or ${MediaStore.Images.Media.MIME_TYPE}=?",
            arrayOf("image/jpeg", "image/png"),
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val results = ArrayList<Image>()

        while (cursor?.moveToNext() == true) {

            // 解析
            val image = Image()
            image.type = AlbumControl.IMAGE
            image.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            image.fileName =
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
            image.mineType =
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
            image.time =
                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))
            results.add(image)
        }

        cursor?.close()
        return results
    }
}