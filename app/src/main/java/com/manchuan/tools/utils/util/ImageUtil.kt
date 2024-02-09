package com.manchuan.tools.utils.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

object ImageUtil {
    @SuppressLint("Range")
    fun getMediaUriFromPath(context: Context, path: String): Uri? {
        val mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = context.contentResolver.query(
            mediaUri,
            null,
            MediaStore.Images.Media.DISPLAY_NAME + "= ?",
            arrayOf(path.substring(path.lastIndexOf("/") + 1)),
            null
        )
        var uri: Uri? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                uri = ContentUris.withAppendedId(
                    mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                )
            }
        }
        cursor?.close()
        return uri
    }
}