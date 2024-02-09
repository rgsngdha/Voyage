package com.manchuan.tools.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources.getSystem
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.math.MathUtils.clamp
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.dylanc.longan.asActivity
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.ViewUtils
import com.google.android.material.snackbar.Snackbar
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.application.App
import com.manchuan.tools.user.timeMills
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.flac.FlacFileReader
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.lang.reflect.Field
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.CancellationException
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


var statusBarHeight = 0
var navBarHeight = 0
val Int.dp: Float get() = (this / getSystem().displayMetrics.density)
val Float.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

fun currActivity(): Activity? {
    return App.context.asActivity()
}

fun logger(e: Any?, print: Boolean = true) {
    if (print) println(e)
}

/**
 * bitmap保存为file
 */
@Throws(IOException::class)
fun bitmapToFile(
    filePath: String,
    bitmap: Bitmap?, quality: Int,
): File? {
    if (bitmap != null) {
        val file = File(
            filePath.substring(
                0, filePath.lastIndexOf(File.separator)
            )
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        val bos = BufferedOutputStream(
            FileOutputStream(filePath)
        )
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos)
        bos.flush()
        bos.close()
        return file
    }
    return null
}


/**
 * # 设置歌曲信息
 * @param file 歌曲文件
 * @param musicName 歌名
 * @param artist 作者
 * @param album 专辑
 * @throws Exception exception
 */
fun writeMusicInfo(
    file: File?,
    musicName: String?,
    artist: String?,
    album: String?,
    cover: File? = null,
) {
    if (file != null) {
        if (file.extension.contains("flac")) {
            runCatching {
                val file = FlacFileReader().read(file)
                val tag = file.tag
                tag.setField(FieldKey.TITLE, musicName)
                tag.setField(FieldKey.ARTIST, artist)
                file.tag = tag
                AudioFileIO.write(file)
            }.onFailure {
                loge("Flac 歌曲写入操作", "失败", it)
            }/*
            val reader = FlacFileReader().read(file)
            val tag = reader.tag
            tag.setField(FieldKey.TITLE, musicName)
            tag.setField(FieldKey.ARTIST, artist)
            AudioFileIO.write(reader)*/
        } else {
            val mp3File = AudioFileIO.read(file) as MP3File
            val tag = mp3File.iD3v2TagAsv24
            if (tag != null) {
                //歌曲名
                tag.setField(FieldKey.TITLE, musicName)
                //歌手
                tag.setField(FieldKey.ARTIST, artist)
                if (cover != null) {
                    val artwork = ArtworkFactory.createArtworkFromFile(cover)
                    tag.setField(artwork)
                }
//        //专辑
//        tag.setField(FieldKey.ALBUM, album)
            }
            //设置标签
            mp3File.iD3v2Tag = tag
            //保存歌曲文件
            mp3File.save(file)
        }
    }
}


fun saveData(fileName: String, data: Any?, activity: Context? = null) {
    tryWith {
        val a = activity ?: com.dylanc.longan.topActivity
        val fos: FileOutputStream = a.openFileOutput(fileName, Context.MODE_PRIVATE)
        val os = ObjectOutputStream(fos)
        os.writeObject(data)
        os.close()
        fos.close()
    }
}

fun Context.hasNFC(): Boolean {
    val packageManager = packageManager
    return packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
}

fun <T> tryWith(call: () -> T): T? {
    return try {
        call.invoke()
    } catch (e: Exception) {
        loge("tryWith", "失败", e)
        null
    }
}

suspend fun <T> tryWithSuspend(call: suspend () -> T): T? {
    return try {
        call.invoke()
    } catch (e: Exception) {
        logError(e)
        null
    } catch (e: CancellationException) {
        null
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> loadData(fileName: String, activity: Context? = null, toast: Boolean = true): T? {
    val a = activity ?: com.dylanc.longan.topActivity
    try {
        if (a.fileList() != null) if (fileName in a.fileList()) {
            val fileIS: FileInputStream = a.openFileInput(fileName)
            val objIS = ObjectInputStream(fileIS)
            val data = objIS.readObject() as T
            objIS.close()
            fileIS.close()
            return data
        }
    } catch (e: Exception) {
        if (toast) toastString("Error loading data $fileName")
        e.printStackTrace()
    }
    return null
}

@Suppress("DEPRECATION")
fun Activity.hideSystemBars() {
    window.decorView.systemUiVisibility =
        (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
}


@Suppress("DEPRECATION")
fun Activity.showSystemBars() {
    window.decorView.systemUiVisibility =
        (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
}

@Suppress("DEPRECATION")
fun Activity.hideStatusBar() {
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

open class BottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun onStart() {
        super.onStart()
        if (this.resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            val behavior = BottomSheetBehavior.from(requireView().parent as View)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }
}


class FadingEdgeRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun isPaddingOffsetRequired(): Boolean {
        return !clipToPadding
    }

    override fun getLeftPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingLeft
    }

    override fun getTopPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingTop
    }

    override fun getRightPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingRight
    }

    override fun getBottomPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingBottom
    }
}

fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
    if (lhs == rhs) {
        return 0
    }
    if (lhs.isEmpty()) {
        return rhs.length
    }
    if (rhs.isEmpty()) {
        return lhs.length
    }

    val lhsLength = lhs.length + 1
    val rhsLength = rhs.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1 until rhsLength) {
        newCost[0] = i

        for (j in 1 until lhsLength) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength - 1]
}

fun String.findBetween(a: String, b: String): String? {
    val start = this.indexOf(a)
    val end = if (start != -1) this.indexOf(b, start) else return null
    return if (end != -1) this.subSequence(start, end).removePrefix(a).removeSuffix(b)
        .toString() else null
}


class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeCLick: (View) -> Unit,
) : View.OnClickListener {

    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}


class FTActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    var currentActivity: Activity? = null
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}
}

abstract class GesturesListener : GestureDetector.SimpleOnGestureListener() {
    private var timer: Timer? = null //at class level;
    private val delay: Long = 200

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        processSingleClickEvent(e)
        return super.onSingleTapUp(e)
    }

    override fun onLongPress(e: MotionEvent) {
        processLongClickEvent(e)
        super.onLongPress(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        processDoubleClickEvent(e)
        return super.onDoubleTap(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        onScrollYClick(distanceY)
        onScrollXClick(distanceX)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    private fun processSingleClickEvent(e: MotionEvent?) {
        val handler = Handler(Looper.getMainLooper())
        val mRunnable = Runnable {
            onSingleClick(e)
        }
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    handler.post(mRunnable)
                }
            }, delay)
        }
    }

    private fun processDoubleClickEvent(e: MotionEvent?) {
        timer?.apply {
            cancel()
            purge()
        }
        onDoubleClick(e)
    }

    private fun processLongClickEvent(e: MotionEvent?) {
        timer?.apply {
            cancel()
            purge()
        }
        onLongClick(e)
    }

    open fun onSingleClick(event: MotionEvent?) {}
    open fun onDoubleClick(event: MotionEvent?) {}
    open fun onScrollYClick(y: Float) {}
    open fun onScrollXClick(y: Float) {}
    open fun onLongClick(event: MotionEvent?) {}
}

fun View.circularReveal(ex: Int, ey: Int, subX: Boolean, time: Long) {
    ViewAnimationUtils.createCircularReveal(
        this, if (subX) (ex - x.toInt()) else ex, ey - y.toInt(), 0f, max(height, width).toFloat()
    ).setDuration(time).start()
}

fun saveImageToDownloads(title: String, bitmap: Bitmap, context: Context) {
    val contentUri = FileProvider.getUriForFile(
        context, BuildConfig.APPLICATION_ID + ".provider", saveImage(
            bitmap,
            Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DOWNLOADS,
            title
        ) ?: return
    )
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(contentUri, "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(intent)
}

fun shareImage(title: String, bitmap: Bitmap, context: Context) {

    val contentUri = FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID + ".provider",
        saveImage(bitmap, context.cacheDir.absolutePath, title) ?: return
    )

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "image/png"
    intent.putExtra(Intent.EXTRA_TEXT, title)
    intent.putExtra(Intent.EXTRA_STREAM, contentUri)
    context.startActivity(Intent.createChooser(intent, "Share $title"))
}

@SuppressLint("ViewConstructor")
class ExtendedTimeBar(
    context: Context,
    attrs: AttributeSet?,
) : DefaultTimeBar(context, attrs) {
    private var enabled = false
    private var forceDisabled = false
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        super.setEnabled(!forceDisabled && this.enabled)
    }

    fun setForceDisabled(forceDisabled: Boolean) {
        this.forceDisabled = forceDisabled
        isEnabled = enabled
    }
}


fun saveImage(
    image: Bitmap,
    path: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Voyage",
    imageFileName: String = "PICTURE_$timeMills",
): File? {
    if (!File(path).exists()) File(path).mkdirs()
    val imageFile = File(path, "$imageFileName.png")
    return tryWith(call = {
        val fOut: OutputStream = FileOutputStream(imageFile)
        image.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.close()
        currActivity()?.let {
            toast("已保存至相册")
        }
        scanFile(imageFile.absolutePath, currActivity()!!)
        imageFile
    })
}

private fun scanFile(path: String, context: Context) {
    MediaScannerConnection.scanFile(context, arrayOf(path), null) { p, _ ->
        logger("Finished scanning $p")
    }
}


fun copyToClipboard(string: String, toast: Boolean = false) {
    val activity = currActivity() ?: return
    val clipboard = getSystemService(activity, ClipboardManager::class.java)
    val clip = ClipData.newPlainText("label", string)
    clipboard?.setPrimaryClip(clip)
    if (toast) toastString("Copied \"$string\"")
}

class EmptyAdapter(private val count: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EmptyViewHolder(View(parent.context))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun getItemCount(): Int = count

    inner class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}

fun toast(string: String?, activity: Activity? = null) {
    if (string != null) {
        (activity ?: currActivity())?.apply {
            runOnUiThread {
                Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
            }
        }
        logger(string)
    }
}

fun toastString(s: String?, activity: Activity? = null) {
    if (s != null) {
        (activity ?: currActivity())?.apply {
            runOnUiThread {
                val snackBar = Snackbar.make(
                    window.decorView.findViewById(android.R.id.content), s, Snackbar.LENGTH_LONG
                )
                snackBar.view.apply {
                    updateLayoutParams<FrameLayout.LayoutParams> {
                        gravity = (Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
                        width = WRAP_CONTENT
                    }
                    translationY = -(navBarHeight.dp + 32f)
                    translationZ = 32f
                    updatePadding(16f.px, right = 16f.px)
                    setOnClickListener {
                        snackBar.dismiss()
                    }
                    setOnLongClickListener {
                        copyToClipboard(s, false)
                        toast("Copied to Clipboard")
                        true
                    }
                }
                snackBar.show()
            }
        }
        logger(s)
    }
}

open class NoPaddingArrayAdapter<T>(context: Context, layoutId: Int, items: List<T>) :
    ArrayAdapter<T>(context, layoutId, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
        (view as TextView).setTextColor(Color.WHITE)
        return view
    }
}

@SuppressLint("ClickableViewAccessibility")
class SpinnerNoSwipe : androidx.appcompat.widget.AppCompatSpinner {
    private var mGestureDetector: GestureDetector? = null

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        setup()
    }

    private fun setup() {
        mGestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return performClick()
                }
            })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mGestureDetector!!.onTouchEvent(event)
        return true
    }
}

@SuppressLint("RestrictedApi")
class CustomBottomNavBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : BottomNavigationView(context, attrs) {
    init {
        ViewUtils.doOnApplyWindowInsets(
            this
        ) { view, insets, initialPadding ->
            initialPadding.bottom = 0
            updateLayoutParams<MarginLayoutParams> { bottomMargin = navBarHeight }
            initialPadding.applyToView(view)
            insets
        }
    }
}

fun getCurrentBrightnessValue(context: Context): Float {
    fun getMax(): Int {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val fields: Array<Field> = powerManager.javaClass.declaredFields
        for (field in fields) {
            if (field.name.equals("BRIGHTNESS_ON")) {
                field.isAccessible = true
                return try {
                    field.get(powerManager)?.toString()?.toInt() ?: 255
                } catch (e: IllegalAccessException) {
                    255
                }
            }
        }
        return 255
    }

    fun getCur(): Float {
        return Settings.System.getInt(
            context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 127
        ).toFloat()
    }

    return brightnessConverter(getCur() / getMax(), true)
}

fun brightnessConverter(it: Float, fromLog: Boolean) = clamp(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) if (fromLog) log2((it * 256f)) * 12.5f / 100f else 2f.pow(
        it * 100f / 12.5f
    ) / 256f
    else it, 0.001f, 1f
)


