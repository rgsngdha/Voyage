package com.manchuan.tools.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.AnimRes
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.IntegerRes
import androidx.annotation.InterpolatorRes
import androidx.annotation.PluralsRes
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.TintTypedArray
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.dylanc.longan.context
import com.dylanc.longan.toast
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.PopNotification
import com.lxj.androidktx.core.drawable
import com.manchuan.tools.R
import com.manchuan.tools.compat.use
import com.manchuan.tools.interceptor.PermissionInterceptor
import com.nowfal.kdroidext.kex.clipboardManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

fun Context.copyFileFromAssets(
    assetName: String, savePath: String, saveName: String,
) {
    // 若目标文件夹不存在，则创建
    val dir = File(savePath)
    if (!dir.exists()) {
        if (!dir.mkdir()) {
            Timber.d("mkdir error: %s", savePath)
            return
        }
    }

    // 拷贝文件
    val filename = "$savePath/$saveName"
    val file = File(filename)
    if (!file.exists()) {
        try {
            val inStream = assets.open(assetName)
            val fileOutputStream = FileOutputStream(filename)
            var byteread: Int
            val buffer = ByteArray(1024)
            while (inStream.read(buffer).also { byteread = it } != -1) {
                fileOutputStream.write(buffer, 0, byteread)
            }
            fileOutputStream.flush()
            inStream.close()
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Timber.d("[copyFileFromAssets] copy asset file: $assetName to : $filename")
    } else {
        Timber.d("[copyFileFromAssets] file is exist: %s", filename)
    }
}

/**
 * 拷贝asset目录下所有文件到指定路径
 *
 * @param context    context
 * @param assetsPath asset目录
 * @param savePath   目标目录
 */
fun Context.copyFilesFromAssets(assetsPath: String, savePath: String) {
    try {
        // 获取assets指定目录下的所有文件
        val fileList = this.assets.list(assetsPath)
        if (!fileList.isNullOrEmpty()) {
            val file = File(savePath)
            // 如果目标路径文件夹不存在，则创建
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Timber.d("mkdir error: %s", savePath)
                    return
                }
            }
            for (fileName in fileList) {
                copyFileFromAssets("$assetsPath/$fileName", savePath, fileName)
            }
        }
    } catch (e: java.lang.Exception) {
        // TODO Auto-generated catch block
        e.printStackTrace()
    }
}

val uriEmpty: Uri by lazy {
    Uri.EMPTY
}

fun Context.requirePermissions(
    vararg permission: String,
    callback: (denied: List<String>, all: Boolean) -> Unit = { denied, all ->

    },
    allCallback: () -> Unit = {},
) {
    if (!XXPermissions.isGranted(this, permission)) {
        XXPermissions.with(this).permission(permission).interceptor(PermissionInterceptor())
            .request { strings, all ->
                callback.invoke(strings, all)
                if (all) allCallback.invoke()
            }
    }
}

fun Activity.requirePermissions(
    vararg permission: String,
    callback: (denied: List<String>, all: Boolean) -> Unit = { denied, all ->

    },
    allCallback: () -> Unit = {},
) {
    if (!XXPermissions.isGranted(this, permission)) {
        XXPermissions.with(this).permission(permission).interceptor(PermissionInterceptor())
            .request { strings, all ->
                callback.invoke(strings, all)
                if (all) allCallback.invoke()
            }
    }
}

fun <T> List<T>.moveHeadToTail(size: Int): MutableList<T> {
    val temp = this.take(size).toMutableList()
    temp.addAll(0, this.drop(size))
    return temp
}

fun Cursor.getSongId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media._ID)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongTitle(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.TITLE)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getAlbumId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getAlbumTitle(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ALBUM)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getArtistId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
    return if (index < 0) 0 else this.getLong(index)
}

fun Cursor.getArtist(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ARTIST)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getArtists(): List<String> {
    return this.getArtist().split("/")
}

fun Cursor.getSongSize(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.SIZE)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongData(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.DATA)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getSongDuration(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.DURATION)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongMimeType(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
    return if (index < 0) "" else this.getString(index)
}

@RequiresApi(Build.VERSION_CODES.R)
fun Cursor.getSongGenre(): String? {
    val index = this.getColumnIndex(MediaStore.Audio.AudioColumns.GENRE)
    return if (index == -1) null else this.getString(index)
}

fun Cursor.getAlbumArt(): Uri {
    return ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart/"), getAlbumId()
    )
}

fun Cursor.getMediaUri(): Uri {
    return Uri.withAppendedPath(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getSongId().toString()
    )
}


fun Context.joinGroup(group: String) {
    runCatching {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data =
            Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$group&card_type=group&source=qrcode")
        startActivity(intent)
    }.onFailure {
        snack("未安装QQ")
    }
}

fun Activity.notFitsSystemWindows() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

val job = Job()
val ioScope = CoroutineScope(Dispatchers.IO + job)
val uiScope = CoroutineScope(Dispatchers.Main + job)

fun Activity.joinGroup(group: String) {
    context.joinGroup(group)
}

fun Fragment.joinGroup(group: String) {
    requireContext().joinGroup(group)
}

fun Context.viewQQPersonal(personal: String) {
    runCatching {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data =
            Uri.parse("mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=$personal")
        startActivity(intent)
    }.onFailure {
        snack("未安装QQ")
    }
}

fun Activity.viewQQPersonal(personal: String) {
    context.viewQQPersonal(personal)
}

fun Fragment.viewQQPersonal(personal: String) {
    requireContext().viewQQPersonal(personal)
}

fun Fragment.snack(
    message: String?,
    activity: Activity? = null,
    actionText: String? = null,
    action: (() -> Unit?)? = null,
) {
    requireContext().snack(message, activity, actionText, action)
}

fun Context.snack(
    message: String?,
    activity: Activity? = null,
    actionText: String? = null,
    action: (() -> Unit?)? = null,
) {
    if (message != null) {
        (activity ?: com.dylanc.longan.topActivity).apply {
            runOnUiThread {
                val snackBar = Snackbar.make(
                    window.decorView.findViewById(android.R.id.content),
                    message,
                    Snackbar.LENGTH_LONG
                )
                if (actionText != null && action != null) {
                    snackBar.setAction(actionText) {
                        action.invoke()
                    }
                }
                snackBar.view.apply {
                    setOnClickListener {
                        snackBar.dismiss()
                    }
                    setOnLongClickListener {
                        copyToClipboard(message, false)
                        toast("Copied to Clipboard")
                        true
                    }
                }
                snackBar.show()
            }
        }
        logger(message)
    }
}

fun Context.firstClipboardText(): String {
    val clipboardManager = this.clipboardManager
    // 获取剪贴板的剪贴数据集
    var content = ""
    val clipData = clipboardManager?.primaryClip
    if (null != clipData && clipData.itemCount > 0) {
        // 从数据集中获取（粘贴）第一条文本数据
        val item = clipData.getItemAt(0)
        if (null != item) {
            runCatching {
                content = item.text.toString()
            }
        }
    }
    return content
}

fun Activity.snack(message: String?, actionText: String? = null, action: (() -> Unit)? = null) {
    context.snack(message, this, actionText, action)
}

/**
 * Resize the bitmap
 *
 * @param bitmap 图片引用
 * @param width  宽度
 * @param height 高度
 * @return 缩放之后的图片引用
 */
fun zoomBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    val matrix = Matrix()
    val scaleWidth = width.toFloat() / w
    val scaleHeight = height.toFloat() / h
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true)
}

fun decodeFromPhoto(photo: Bitmap?): Result? {
    var rawResult: Result? = null
    if (photo != null) {
        val smallBitmap = zoomBitmap(
            photo, photo.width / 2, photo.height / 2
        ) // 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
        photo.recycle() // 释放原始图片占用的内存，防止out of memory异常发生
        val multiFormatReader = MultiFormatReader()

        // 解码的参数
        val hints = Hashtable<DecodeHintType, Any?>(2)
        // 可以解析的编码类型
        var decodeFormats = Vector<BarcodeFormat?>()
        if (decodeFormats.isEmpty()) {
            decodeFormats = Vector()
            val PRODUCT_FORMATS = Vector<BarcodeFormat?>(5)
            PRODUCT_FORMATS.add(BarcodeFormat.UPC_A)
            PRODUCT_FORMATS.add(BarcodeFormat.UPC_E)
            PRODUCT_FORMATS.add(BarcodeFormat.EAN_13)
            PRODUCT_FORMATS.add(BarcodeFormat.EAN_8)
            // PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
            val ONE_D_FORMATS = Vector<BarcodeFormat?>(PRODUCT_FORMATS.size + 4)
            ONE_D_FORMATS.addAll(PRODUCT_FORMATS)
            ONE_D_FORMATS.add(BarcodeFormat.CODE_39)
            ONE_D_FORMATS.add(BarcodeFormat.CODE_93)
            ONE_D_FORMATS.add(BarcodeFormat.CODE_128)
            ONE_D_FORMATS.add(BarcodeFormat.ITF)
            val QR_CODE_FORMATS = Vector<BarcodeFormat?>(1)
            QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE)
            val DATA_MATRIX_FORMATS = Vector<BarcodeFormat?>(1)
            DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX)

            // 这里设置可扫描的类型，我这里选择了都支持
            decodeFormats.addAll(ONE_D_FORMATS)
            decodeFormats.addAll(QR_CODE_FORMATS)
            decodeFormats.addAll(DATA_MATRIX_FORMATS)
        }
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        // 设置继续的字符编码格式为UTF8
        // hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        // 设置解析配置参数
        multiFormatReader.setHints(hints)

        // 开始对图像资源解码
        try {
            rawResult = multiFormatReader.decodeWithState(
                BinaryBitmap(
                    HybridBinarizer(
                        BitmapLuminanceSource(smallBitmap)
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return rawResult
}

class BitmapLuminanceSource(bitmap: Bitmap) : LuminanceSource(bitmap.width, bitmap.height) {
    private val bitmapPixels: ByteArray
    override fun getMatrix(): ByteArray {
        // 返回我们生成好的像素数据
        return bitmapPixels
    }

    override fun getRow(y: Int, row: ByteArray): ByteArray {
        // 这里要得到指定行的像素数据
        System.arraycopy(bitmapPixels, y * width, row, 0, width)
        return row
    }

    init {

        // 首先，要取得该图片的像素数组内容
        val data = IntArray(bitmap.width * bitmap.height)
        bitmapPixels = ByteArray(bitmap.width * bitmap.height)
        bitmap.getPixels(data, 0, width, 0, 0, width, height)

        // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
        for (i in data.indices) {
            bitmapPixels[i] = data[i].toByte()
        }
    }
}

/**
 * 读取Raw文件中的内容
 * @param rawId Raw文件ID:R.id.info
 */
fun Context.readRaw(rawId: Int): String {
    return BufferedReader(InputStreamReader(resources.openRawResource(rawId))).use {
        val sb = StringBuilder()
        it.forEachLine { s ->
            sb.append(s)
        }
        sb.toString()
    }
}

@Throws(IOException::class)
fun Context.bitmapFromUri(uri: Uri): Bitmap {
    val parcelFileDescriptor: ParcelFileDescriptor? =
        applicationContext.contentResolver.openFileDescriptor(uri, "r")
    val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
    val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor?.close()
    return image
}

@Throws(IOException::class)
fun Context.readTextFromUri(uri: Uri): String {
    val stringBuilder = StringBuilder()
    applicationContext.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
        }
    }
    return stringBuilder.toString()
}

@SuppressLint("DiscouragedApi", "Recycle")
@ColorRes
fun Context.foregroundColor(name: String, chroma: Int): Int {
    return resources.getIdentifier(
        "material_" + name + "_" + chroma, "color", packageName
    )
}

@ColorRes
fun Context.backgroundColor(name: String, chroma: Int): Int {
    return resources.getIdentifier(
        "material_" + name + "_" + chroma, "color", packageName
    )
}

val Context.activity: Activity?
    get() {
        var context = this
        while (true) {
            when (context) {
                is Activity -> return context
                is ContextWrapper -> context = context.baseContext
                else -> return null
            }
        }
    }


fun Context.getPermissionDescription(permission: String): String {
    val permissionInfo = packageManager.getPermissionInfo(permission, 0)
    val groupInfo = permissionInfo.group?.let { packageManager.getPermissionGroupInfo(it, 0) }
    if (groupInfo != null) {
        return permissionInfo.loadDescription(packageManager).toString()
    }
    return ""
}

fun Context.getPermissionName(permission: String): String {
    val permissionInfo = packageManager.getPermissionInfo(permission, 0)
    val groupInfo = permissionInfo.group?.let { packageManager.getPermissionGroupInfo(it, 0) }
    if (groupInfo != null) {
        return permissionInfo.loadLabel(packageManager).toString()
    }
    return ""
}

fun Context.textCopyThenPost(textCopied: String) {
    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    // 设置文本
    clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied))
    // 仅针对 Android 12 及更低版本显示吐司。
    runCatching {
        toast("已复制")
    }
}

const val SHARED_AXIS_KEY = "activity_shared_axis_axis"

fun Activity.buildContainerTransform(
    entering: Boolean,
    duration: Long? = 500,
): MaterialContainerTransform {
    val transform = MaterialContainerTransform(this, entering)
    transform.setAllContainerColors(
        MaterialColors.getColor(
            this.findViewById(android.R.id.content), com.google.android.material.R.attr.colorSurface
        )
    )
    transform.addTarget(android.R.id.content)
    transform.duration = duration!!
    return transform
}

@StyleRes
fun getSpecStyleResId(): Int {
    return com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
}

fun Context.androidLogo(): Drawable {
    return when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.ICE_CREAM_SANDWICH -> drawable(R.drawable.ic_android_i)
        Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> drawable(R.drawable.ic_android_i)
        Build.VERSION_CODES.JELLY_BEAN -> drawable(R.drawable.ic_android_j)
        Build.VERSION_CODES.JELLY_BEAN_MR1 -> drawable(R.drawable.ic_android_j)
        Build.VERSION_CODES.JELLY_BEAN_MR2 -> drawable(R.drawable.ic_android_j)
        Build.VERSION_CODES.KITKAT -> drawable(R.drawable.ic_android_k)
        Build.VERSION_CODES.KITKAT_WATCH -> drawable(R.drawable.ic_android_k)
        Build.VERSION_CODES.LOLLIPOP -> drawable(R.drawable.ic_android_l)
        Build.VERSION_CODES.LOLLIPOP_MR1 -> drawable(R.drawable.ic_android_l)
        Build.VERSION_CODES.M -> drawable(R.drawable.ic_android_m)
        Build.VERSION_CODES.N -> drawable(R.drawable.ic_android_n)
        Build.VERSION_CODES.N_MR1 -> drawable(R.drawable.ic_android_n)
        Build.VERSION_CODES.O -> drawable(R.drawable.ic_android_o)
        Build.VERSION_CODES.O_MR1 -> drawable(R.drawable.ic_android_o_mr1)
        Build.VERSION_CODES.P -> drawable(R.drawable.ic_android_p)
        Build.VERSION_CODES.Q -> drawable(R.drawable.ic_android_q)
        Build.VERSION_CODES.R -> drawable(R.drawable.ic_android_r)
        Build.VERSION_CODES.S -> drawable(R.drawable.ic_android_s)
        Build.VERSION_CODES.S_V2 -> drawable(R.drawable.ic_android_s)
        Build.VERSION_CODES.TIRAMISU -> drawable(R.drawable.ic_android_t)
        else -> drawable(android.R.drawable.progress_horizontal)
    }
}

fun androidString(sdkVersion: Int = Build.VERSION.SDK_INT): String {
    return when (sdkVersion) {
        Build.VERSION_CODES.ICE_CREAM_SANDWICH -> "Android 4.0.1-2"
        Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> "Android 4.0.3-4"
        Build.VERSION_CODES.JELLY_BEAN -> "Android 4.1"
        Build.VERSION_CODES.JELLY_BEAN_MR1 -> "Android 4.2"
        Build.VERSION_CODES.JELLY_BEAN_MR2 -> "Android 4.3"
        Build.VERSION_CODES.KITKAT -> "Android 4.4"
        Build.VERSION_CODES.KITKAT_WATCH -> "Android 4.4.4"
        Build.VERSION_CODES.LOLLIPOP -> "Android 5.0"
        Build.VERSION_CODES.LOLLIPOP_MR1 -> "Android 5.1"
        Build.VERSION_CODES.M -> "Android 6.0"
        Build.VERSION_CODES.N -> "Android 7.0"
        Build.VERSION_CODES.N_MR1 -> "Android 7.1"
        Build.VERSION_CODES.O -> "Android 8.0"
        Build.VERSION_CODES.O_MR1 -> "Android 8.1"
        Build.VERSION_CODES.P -> "Android 9"
        Build.VERSION_CODES.Q -> "Android 10"
        Build.VERSION_CODES.R -> "Android 11"
        Build.VERSION_CODES.S -> "Android 12"
        Build.VERSION_CODES.S_V2 -> "Android 12.1"
        Build.VERSION_CODES.TIRAMISU -> "Android 13"
        Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> "Android 14"
        else -> "未知"
    }
}

fun Context.readFileFromAssets(fileName: String?): String {
    if (null == fileName) return "无法读取内容，请检查资源文件是否被移动！"
    val am: AssetManager = assets
    val input: InputStream = am.open(fileName)
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int
    while (input.read(buffer).also { len = it } != -1) {
        output.write(buffer, 0, len)
    }
    output.close()
    input.close()
    return output.toString()
}

fun Context.readAssetsTxt(fileName: String): String {
    val input = assets.open(fileName)
    val reactivestreams = BufferedReader(InputStreamReader(input))
    try {
        Timber.tag("获取的assets文本内容----").e(reactivestreams.readLines().toString())
        return reactivestreams.readLines().toString()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        reactivestreams.close()
    }
    return "读取失败,请检查文件名称及文件是否存在!"
}


fun Context.getAnimation(@AnimRes id: Int): Animation = AnimationUtils.loadAnimation(this, id)

fun Context.getBoolean(@BoolRes id: Int) = resources.getBoolean(id)

fun Context.getDimension(@DimenRes id: Int) = resources.getDimension(id)

fun Context.getDimensionPixelOffset(@DimenRes id: Int) = resources.getDimensionPixelOffset(id)

fun Context.getDimensionPixelSize(@DimenRes id: Int) = resources.getDimensionPixelSize(id)

fun Context.getFloat(@DimenRes id: Int) = resources.getFloatCompat(id)

fun Context.getInteger(@IntegerRes id: Int) = resources.getInteger(id)

fun notification(title: String, message: String) {
    PopNotification.show(R.mipmap.ic_voyage, title, message)
}

fun Context.getInterpolator(@InterpolatorRes id: Int): Interpolator =
    AnimationUtils.loadInterpolator(this, id)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int): String =
    resources.getQuantityString(id, quantity)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?): String =
    resources.getQuantityString(id, quantity, *formatArgs)

fun Context.getQuantityText(@PluralsRes id: Int, quantity: Int): CharSequence =
    resources.getQuantityText(id, quantity)

fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

@SuppressLint("RestrictedApi")
fun Context.getBooleanByAttr(@AttrRes attr: Int): Boolean =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getBoolean(0, false) }

fun Context.getColorByAttr(@AttrRes attr: Int): Int = getColorStateListByAttr(attr).defaultColor

@SuppressLint("RestrictedApi")
fun Context.getColorStateListByAttr(@AttrRes attr: Int): ColorStateList =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getColorStateList(0) }

@SuppressLint("RestrictedApi")
fun Context.getDimensionByAttr(@AttrRes attr: Int): Float =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getDimension(0, 0f) }

@SuppressLint("RestrictedApi")
fun Context.getDimensionPixelOffsetByAttr(@AttrRes attr: Int): Int =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use {
        it.getDimensionPixelOffset(0, 0)
    }

@SuppressLint("RestrictedApi")
fun Context.getDimensionPixelSizeByAttr(@AttrRes attr: Int): Int =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getDimensionPixelSize(0, 0) }

@SuppressLint("RestrictedApi")
fun Context.getDrawableByAttr(@AttrRes attr: Int): Drawable =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getDrawable(0) }

@SuppressLint("RestrictedApi")
fun Context.getFloatByAttr(@AttrRes attr: Int): Float =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getFloat(0, 0f) }

@SuppressLint("RestrictedApi")
fun Context.getResourceIdByAttr(@AttrRes attr: Int): Int =
    obtainStyledAttributesCompat(attrs = intArrayOf(attr)).use { it.getResourceId(0, 0) }

@Dimension
fun Context.dpToDimension(@Dimension(unit = Dimension.DP) dp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

@Dimension
fun Context.dpToDimension(@Dimension(unit = Dimension.DP) dp: Int) = dpToDimension(dp.toFloat())

@Dimension
fun Context.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Float): Int =
    dpToDimension(dp).toInt()

@Dimension
fun Context.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Int) =
    dpToDimensionPixelOffset(dp.toFloat())

@Dimension
fun Context.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Float): Int {
    val value = dpToDimension(dp)
    val size = (if (value >= 0) value + 0.5f else value - 0.5f).toInt()
    return when {
        size != 0 -> size
        value == 0f -> 0
        value > 0 -> 1
        else -> -1
    }
}

@Dimension
fun Context.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Int) =
    dpToDimensionPixelSize(dp.toFloat())

val Context.shortAnimTime: Int
    get() = getInteger(android.R.integer.config_shortAnimTime)

val Context.mediumAnimTime: Int
    get() = getInteger(android.R.integer.config_mediumAnimTime)

val Context.longAnimTime: Int
    get() = getInteger(android.R.integer.config_longAnimTime)

val Context.displayWidth: Int
    get() = resources.displayMetrics.widthPixels

val Context.displayHeight: Int
    get() = resources.displayMetrics.heightPixels

fun Context.hasSwDp(@Dimension(unit = Dimension.DP) dp: Int): Boolean =
    resources.configuration.smallestScreenWidthDp >= dp

val Context.hasSw600Dp: Boolean
    get() = hasSwDp(600)

fun Context.hasWDp(@Dimension(unit = Dimension.DP) dp: Int): Boolean =
    resources.configuration.screenWidthDp >= dp

val Context.hasW600Dp: Boolean
    get() = hasWDp(600)

val Context.hasW960Dp: Boolean
    get() = hasWDp(960)


val Context.isOrientationLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.isOrientationPortrait: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun Context.withTheme(@StyleRes themeRes: Int): Context =
    if (themeRes != 0) ContextThemeWrapper(this, themeRes) else this

fun Resources.getFloatCompat(@DimenRes id: Int) = ResourcesCompat.getFloat(this, id)

@SuppressLint("RestrictedApi")
fun Context.obtainStyledAttributesCompat(
    set: AttributeSet? = null,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
): TintTypedArray =
    TintTypedArray.obtainStyledAttributes(this, set, attrs, defStyleAttr, defStyleRes)