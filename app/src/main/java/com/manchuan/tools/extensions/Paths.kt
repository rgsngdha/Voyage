package com.manchuan.tools.extensions

import ando.file.core.FileUri
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.dylanc.longan.externalCacheDirPath
import com.manchuan.tools.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt


inline val publicMoviesDirPath: String
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath

inline val publicAudiosDirPath: String
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath

inline val publicPicturesDirPath: String
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath

inline val publicDownloadsDirPath: String
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

inline val publicDocumentsDirPath: String
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath

fun readableFileSize(size: Long): String {
    if (size <= 0) return "0"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
        size / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
}


/**
 * file --> uri
 * @param context
 * @param file
 *
 * @return
 */
fun File.getUriFromFile(context: Context?): Uri? {
    if (context == null) {
        throw NullPointerException()
    }
    return FileProvider.getUriForFile(
        context.applicationContext, BuildConfig.APPLICATION_ID + ".provider", this
    )
}


/**
 * 通过文件路径 uri的转字符也可以
 * @param filePath
 * @return
 */
fun getMimeType(filePath: String?): String? {
    val ext = MimeTypeMap.getFileExtensionFromUrl(filePath)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
}

fun Context.documentFileToFile(uri: String): List<File> {
    val allFile: MutableList<File> = ArrayList()
    val dirUri = Uri.parse(uri)
    val documentFile = DocumentFile.fromTreeUri(this, dirUri)
    //遍历DocumentFile
    val files = documentFile!!.listFiles()
    //LogUtil.d(Constant.TAG, "documentFileToFile files count=" + files.size)
    for (file in files) {
        val fileName = file.name
        val fileUri = file.uri
        //LogUtil.d(Constant.TAG, "documentFileToFile fileName=$fileName fileUri=$fileUri")
        try {
            //DocumentFile输入流
            val `in`: InputStream? = this.contentResolver.openInputStream(fileUri)
            val newFile: File = File(externalCacheDirPath, fileName)
            val out: OutputStream = FileOutputStream(newFile)
            val buf = ByteArray(1024)
            var len: Int
            if (`in` != null) {
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
            `in`?.close()
            out.close()
            allFile.add(newFile)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    return allFile
}


/**
 * # 获取Uri的真实路径
 * @since 如果你在活动中使用这个，[val path = getPath(uri)]就可以了
 * @author bohong
 * @param uri 文件Uri
 * @return [String] 如果 [getFileAbsolutePath] 为 null 或为空，则返回 [FileUri.getPathByUri] 如果为空，则返回 [""]
 */

fun Context.getPath(uri: Uri): String {
    return getFileAbsolutePath(this, uri) ?: FileUri.getPathByUri(uri) ?: ""
}

fun Activity.getPath(uri: Uri): String {
    return getFileAbsolutePath(this, uri) ?: FileUri.getPathByUri(uri) ?: ""
}

fun Fragment.getPath(uri: Uri): String {
    return getFileAbsolutePath(requireContext(), uri) ?: FileUri.getPathByUri(uri) ?: ""
}

/**
 * 使用第三方qq文件管理器打开
 *
 * @param uri
 *
 * @return
 */
fun isQQMediaDocument(uri: Uri): Boolean {
    return "com.tencent.mtt.fileprovider" == uri.authority
}


/**
 * 根据Uri获取文件绝对路径，解决Android4.4以上版本Uri转换 兼容Android 10
 *
 * @param context
 * @param imageUri
 */
@SuppressLint("ObsoleteSdkInt")
fun getFileAbsolutePath(context: Context?, imageUri: Uri?): String? {
    if (context == null || imageUri == null) {
        return null
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
        return getRealFilePath(context, imageUri)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && DocumentsContract.isDocumentUri(
            context, imageUri
        )
    ) {
        if (isExternalStorageDocument(imageUri)) {
            val docId = DocumentsContract.getDocumentId(imageUri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (isDownloadsDocument(imageUri)) {
            val id = DocumentsContract.getDocumentId(imageUri)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(imageUri)) {
            val docId = DocumentsContract.getDocumentId(imageUri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                "download" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                }

                "document" -> {
                    //   解决Android 11 获取像PDF、Excel文件的路径
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    }
                }
            }
            val selection = MediaStore.Images.Media._ID + "=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    }
    // MediaStore (and general)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return uriToFileApiQ(context, imageUri)
    } else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
        // Return the remote address
        return if (isGooglePhotosUri(imageUri)) {
            imageUri.lastPathSegment
        } else getDataColumn(context, imageUri, null, null)
    } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
        return imageUri.path
    }
    return getDataColumn(context, imageUri)
}

//此方法 只能用于4.4以下的版本
private fun getRealFilePath(context: Context, uri: Uri?): String? {
    if (null == uri) {
        return null
    }
    val scheme = uri.scheme
    var data: String? = null
    if (scheme == null) {
        data = uri.path
    } else if (ContentResolver.SCHEME_FILE == scheme) {
        data = uri.path
    } else if (ContentResolver.SCHEME_CONTENT == scheme) {
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

//            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (index > -1) {
                    data = cursor.getString(index)
                }
            }
            cursor.close()
        }
    }
    return data
}


/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun getDataColumn(
    context: Context, uri: Uri?, selection: String? = null, selectionArgs: Array<String>? = null
): String? {
    var cursor: Cursor? = null
    val column = MediaStore.Images.Media.DATA
    val projection = arrayOf(column)
    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index: Int = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        if (cursor != null) {
            cursor.close()
        }
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
private fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}


/**
 * Android 10 以上适配 另一种写法
 * @param context
 * @param uri
 * @return
 */
@SuppressLint("Range")
private fun getFileFromContentUri(context: Context, uri: Uri?): String? {
    if (uri == null) {
        return null
    }
    val filePath: String
    val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
    val contentResolver = context.contentResolver
    val cursor: Cursor? = contentResolver.query(
        uri, filePathColumn, null, null, null
    )
    if (cursor != null) {
        cursor.moveToFirst()
        try {
            filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
            return filePath
        } catch (e: Exception) {
        } finally {
            cursor.close()
        }
    }
    return ""
}

/**
 * Android 10 以上适配
 * @param context
 * @param uri
 * @return
 */
@SuppressLint("Range")
@RequiresApi(api = Build.VERSION_CODES.Q)
private fun uriToFileApiQ(context: Context, uri: Uri): String? {
    var file: File? = null
    //android10以上转换
    if (uri.scheme == ContentResolver.SCHEME_FILE) {
        file = File(uri.path)
    } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        //把文件复制到沙盒目录
        val contentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val displayName: String =
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                try {
                    val `is` = contentResolver.openInputStream(uri)
                    val cache = File(
                        context.externalCacheDir!!.absolutePath,
                        ((Math.random() + 1) * 1000).roundToInt().toString() + displayName
                    )
                    val fos = FileOutputStream(cache)
                    if (`is` != null) {
                        FileUtils.copy(`is`, fos)
                    }
                    file = cache
                    fos.close()
                    `is`!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    return file?.absolutePath
}


/**
 * 通过文件路径 uri的转字符也可以
 * @param filePath
 * @return
 */