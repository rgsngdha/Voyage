package com.manchuan.tools.about

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.itxca.spannablex.activateClick
import com.itxca.spannablex.spannable
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.MultiTypeAdapter
import com.manchuan.tools.activity.app.PrivacyActivity
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.extensions.addAlpha
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.textColorPrimary
import com.manchuan.tools.user.verifyRole
import com.skydoves.whatif.whatIfNotNullOrEmpty

/**
 * @author drakeet
 */
abstract class AbsAboutActivity : BaseActivity() {
    var toolbar: Toolbar? = null
        private set
    var collapsingToolbar: SubtitleCollapsingToolbarLayout? = null
        private set
    private var headerContentLayout: MaterialCardView? = null
    var items: MutableList<Any>? = null
        private set
    var adapter: MultiTypeAdapter? = null
        private set
    var sloganTextView: TextView? = null
        private set
    private var versionTextView: TextView? = null
    private var recyclerView: RecyclerView? = null
    var imageLoader: ImageLoader? = null
        private set
    private var initialized = false
    var onRecommendationClickedListener: OnRecommendationClickedListener? = null
    var onContributorClickedListener: OnContributorClickedListener? = null
    protected abstract fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView)
    protected abstract fun onItemsCreated(items: MutableList<Any>)
    private fun onTitleViewCreated() {}

    @SuppressLint("NotifyDataSetChanged")
    fun setImageLoader(imageLoader: ImageLoader) {
        this.imageLoader = imageLoader
        if (initialized) {
            adapter!!.notifyDataSetChanged()
        }
    }

    @LayoutRes
    protected fun layoutRes(): Int {
        return R.layout.about_page_main_activity
    }


    val COUNTS = 10 // 点击次数

    val DURATION: Long = 4000 // 规定有效时间

    var mHits = LongArray(COUNTS)

    private fun continuousClick() {
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        //为数组最后一位赋值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] >= SystemClock.uptimeMillis() - DURATION) {
            mHits = LongArray(COUNTS) //重新初始化数组
            WaitDialog.show("验证权限...")
            Global.token.value.whatIfNotNullOrEmpty {
                verifyRole(it, success = {
                    TipDialog.show("验证完成", WaitDialog.TYPE.SUCCESS)
                    if (Global.isEnabledHideFunction.value?.not() == true) {
                        Global.isEnabledHideFunction.value = true
                        toast("成功激活隐藏功能，请返回至首页查看")
                    } else {
                        toast("你已激活隐藏功能，请勿重复激活")
                    }
                }, failed = {
                    TipDialog.show("无权限激活", WaitDialog.TYPE.ERROR)
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(layoutRes())
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "关于"
        }
        val icon = findViewById<ImageView>(R.id.icon)
        sloganTextView = findViewById(R.id.slogan)
        versionTextView = findViewById(R.id.version)
        collapsingToolbar = findViewById(R.id.ctl)
        headerContentLayout = findViewById(R.id.header_content_layout)
        val containers = findViewById<LinearLayout>(R.id.containers)
        val privacy = findViewById<TextView>(R.id.privacy)
        val useKotlin = findViewById<MaterialButton>(R.id.use_kotlin)
        val buildTime = findViewById<TextView>(R.id.build_time)
        val icpMiit = findViewById<TextView>(R.id.icp_miit)
        icpMiit.text = "ICP备案号:鄂ICP备2023017804号-1A"
        buildTime.text = "构建时间:${BuildConfig.BUILD_TIME}"
        containers.addNavigationBarHeightToMarginBottom()
        useKotlin.doOnClick {
            startActivity<WebActivity>("url" to "https://kotlinlang.org/")
        }
        headerContentLayout?.doOnClick {
            continuousClick()
        }
        privacy.activateClick(false).text = spannable {
            "用户协议".span {
                color(colorPrimary())
                clickable(onClick = { view: View, s: String ->
                    startActivity<PrivacyActivity>("type" to 2, "isGuide" to false)
                })
            }
            " | ".color(textColorPrimary().addAlpha(0.5F))
            "隐私政策".span {
                color(colorPrimary())
                clickable(onClick = { view: View, s: String ->
                    startActivity<PrivacyActivity>("type" to 1, "isGuide" to false)
                })
            }
        }
        onTitleViewCreated()
        onCreateHeader(icon, sloganTextView!!, versionTextView!!)
        onApplyPresetAttrs()
        recyclerView = findViewById(R.id.list)
        applyEdgeToEdge()
    }

    private var givenInsetsToDecorView = false
    private fun applyEdgeToEdge() {
        val window = window
        val navigationBarColor = ContextCompat.getColor(this, R.color.about_page_navigationBarColor)
        window.navigationBarColor = navigationBarColor
        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)
        val decorView = window.decorView
        val originalRecyclerViewPaddingBottom = recyclerView!!.paddingBottom
        givenInsetsToDecorView = false
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(decorView) { v: View?, windowInsets: WindowInsetsCompat ->
            val navigationBarsInsets =
                windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val isGestureNavigation =
                navigationBarsInsets.bottom <= 24 * resources.displayMetrics.density
            if (!isGestureNavigation) {
                ViewCompat.onApplyWindowInsets(decorView, windowInsets)
                givenInsetsToDecorView = true
            } else if (givenInsetsToDecorView) {
                ViewCompat.onApplyWindowInsets(
                    decorView, WindowInsetsCompat.Builder().setInsets(
                        WindowInsetsCompat.Type.navigationBars(), Insets.of(
                            navigationBarsInsets.left,
                            navigationBarsInsets.top,
                            navigationBarsInsets.right,
                            0
                        )
                    ).build()
                )
            }
            decorView.setPadding(
                windowInsets.systemWindowInsetLeft,
                decorView.paddingTop,
                windowInsets.systemWindowInsetRight,
                decorView.paddingBottom
            )
            appBarLayout.setPadding(
                appBarLayout.paddingLeft,
                windowInsets.systemWindowInsetTop,
                appBarLayout.paddingRight,
                appBarLayout.paddingBottom
            )
            recyclerView!!.setPadding(
                recyclerView!!.paddingLeft,
                recyclerView!!.paddingTop,
                recyclerView!!.paddingRight,
                originalRecyclerViewPaddingBottom + navigationBarsInsets.bottom
            )
            windowInsets
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        adapter = MultiTypeAdapter()
        adapter!!.register(Category::class.java, CategoryViewBinder())
        adapter!!.register(Card::class.java, CardViewBinder())
        adapter!!.register(Line::class.java, LineViewBinder())
        adapter!!.register(Contributor::class.java, ContributorViewBinder(this))
        adapter!!.register(License::class.java, LicenseViewBinder())
        adapter!!.register(Recommendation::class.java, RecommendationViewBinder(this))
        items = ArrayList()
        onItemsCreated(items!!)
        adapter!!.items = items!!
        adapter!!.setHasStableIds(true)
        recyclerView!!.addItemDecoration(DividerItemDecoration(adapter!!))
        recyclerView!!.adapter = adapter
        initialized = true
    }

    private fun onApplyPresetAttrs() {
        val a = obtainStyledAttributes(R.styleable.AbsAboutActivity)
        val headerBackground = a.getDrawable(R.styleable.AbsAboutActivity_aboutPageHeaderBackground)
        headerBackground?.let { setHeaderBackground(it) }
        val headerContentScrim =
            a.getDrawable(R.styleable.AbsAboutActivity_aboutPageHeaderContentScrim)
        headerContentScrim?.let { setHeaderContentScrim(it) }
        @ColorInt val headerTextColor =
            a.getColor(R.styleable.AbsAboutActivity_aboutPageHeaderTextColor, -1)
        if (headerTextColor != -1) {
            setHeaderTextColor(headerTextColor)
        }
        val navigationIcon = a.getDrawable(R.styleable.AbsAboutActivity_aboutPageNavigationIcon)
        navigationIcon?.let { setNavigationIcon(it) }
        a.recycle()
    }

    /**
     * Use [.setHeaderBackground] instead.
     *
     * @param resId The resource id of header background
     */
    @Deprecated("")
    fun setHeaderBackgroundResource(@DrawableRes resId: Int) {
        setHeaderBackground(resId)
    }

    fun setHeaderBackground(@DrawableRes resId: Int) {
        setHeaderBackground(ContextCompat.getDrawable(this, resId)!!)
    }

    fun setHeaderBackground(drawable: Drawable) {
        ViewCompat.setBackground(headerContentLayout!!, drawable)
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * @param drawable the drawable to display
     */
    fun setHeaderContentScrim(drawable: Drawable) {
        collapsingToolbar!!.contentScrim = drawable
    }

    fun setHeaderContentScrim(@DrawableRes resId: Int) {
        ContextCompat.getDrawable(this, resId)?.let { setHeaderContentScrim(it) }
    }

    fun setHeaderTextColor(@ColorInt color: Int) {
        collapsingToolbar!!.setCollapsedTitleTextColor(color)
        sloganTextView!!.setTextColor(color)
        versionTextView!!.setTextColor(color)
    }

    private fun setNavigationIcon(drawable: Drawable) {
        toolbar!!.navigationIcon = drawable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}
