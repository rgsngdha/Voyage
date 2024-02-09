package com.manchuan.tools.utils

import ando.file.core.FileUri
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.dylanc.longan.grantReadUriPermission
import com.manchuan.tools.utils.util.ImageUtil
import com.manchuan.tools.utils.util.RomUtil


fun Context.setWallpaper(imageFilePath: String?, authority: String?) {
    if (imageFilePath == null) {
        return
    }
    Log.d("SetWallpaper", "setWallpaper: imageFilePath = $imageFilePath")
    val uriPath: Uri = if (imageFilePath.lowercase().startsWith("content:")) {
        Uri.parse(imageFilePath)
    } else if (imageFilePath.lowercase().startsWith("file:")) {
        FileUri.getUriByPath(imageFilePath)!!
    } else {
        ImageUtil.getMediaUriFromPath(this, imageFilePath)!!
    }
    Log.d("SetWallpaper", "setWallpaper: uriPath = $uriPath")
    val intent: Intent
    if (RomUtil.isHuaweiRom()) {
        try {
            val componentName =
                ComponentName("com.android.gallery3d", "com.android.gallery3d.app.Wallpaper")
            intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uriPath, "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.setComponent(componentName)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultWay(this, uriPath)
        }
    } else if (RomUtil.isMiuiRom()) {
        try {
            val componentName = ComponentName(
                "com.android.thememanager",
                "com.android.thememanager.activity.WallpaperDetailActivity"
            )
            intent = Intent("miui.intent.action.START_WALLPAPER_DETAIL")
            intent.grantReadUriPermission()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uriPath, "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.setComponent(componentName)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultWay(this, uriPath)
        }
    } else if (RomUtil.isOppoRom()) {
        try {
            val componentName = ComponentName(
                "com.oplus.wallpapers",
                "com.oplus.wallpapers.wallpaperpreview.PreviewStatementActivity"
            )
            intent = Intent("miui.intent.action.START_WALLPAPER_DETAIL")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uriPath, "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.setComponent(componentName)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultWay(this, uriPath)
        }
    } else if (RomUtil.isVivoRom()) {
        try {
            val componentName =
                ComponentName("com.vivo.gallery", "com.android.gallery3d.app.Wallpaper")
            intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uriPath, "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.setComponent(componentName)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultWay(this, uriPath)
        }
    } else {
        try {
            intent = WallpaperManager.getInstance(applicationContext)
                .getCropAndSetWallpaperIntent(uriPath)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultWay(this, uriPath)
        }
    }
}

private fun defaultWay(context: Context, uri: Uri) {
    val bitmap: Bitmap?
    try {
        bitmap = MediaStore.Images.Media.getBitmap(context.applicationContext.contentResolver, uri)
        if (bitmap != null) {
            WallpaperManager.getInstance(context.applicationContext).setBitmap(bitmap)
            Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}