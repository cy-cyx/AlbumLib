package com.chat.albumlib

import java.io.Serializable

/**
 * create by caiyx in 2020/12/22
 */
class Image : Serializable {

    companion object {
        @JvmStatic
        private val serialVersionUID = 1

        fun inListItem(images: ArrayList<Image>, image: Image): Int {
            var item = 1
            for (i in 0..images.size) {
                if (images[i].equals(image)) {
                    item = i + 1
                    break
                }
            }
            return item
        }

        fun isSelectStatus(images: ArrayList<Image>, image: Image): Boolean {
            for (m in images) {
                if (m.equals(image)) {
                    return true
                }
            }
            return false
        }
    }

    var type = AlbumControl.IMAGE
    var path = ""
    var fileName = ""
    var mineType = ""
    var time = 0L
    var duration = 0L

    fun equals(other: Image): Boolean {

        // 按照路径判断
        return other.path == this.path
    }

    fun clone(): Image {
        val image = Image()
        image.type = this.type
        image.path = this.path
        image.fileName = this.fileName
        image.mineType = this.mineType
        image.time = this.time
        return image
    }
}