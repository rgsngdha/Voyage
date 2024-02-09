package com.manchuan.tools.activity.site

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.GeolocationPermissions
import android.webkit.JsResult
import android.webkit.MimeTypeMap
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.browser.customtabs.CustomTabsIntent
import com.blankj.utilcode.util.ClipboardUtils
import com.crazylegend.kotlinextensions.notifications.removeNotification
import com.crazylegend.kotlinextensions.notifications.setNotification
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNet
import com.drake.statusbar.immersive
import com.dylanc.longan.context
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.toast
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityWebBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.neutralButton
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.extensions.snack
import java.io.File


class WebActivity : BaseActivity() {

    private val binding by lazy {
        ActivityWebBinding.inflate(layoutInflater)
    }

    private lateinit var agentWeb: AgentWeb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(binding.toolbar, !isAppDarkMode)
        agentWeb =
            AgentWeb.with(this).setAgentWebParent(binding.relWeb, FrameLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(colorPrimary()).setWebChromeClient(webChromeClient)
                .setWebViewClient(webViewClient).createAgentWeb().ready()
                .go(safeIntentExtras<String>("url").value)
        agentWeb.webCreator.webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val fileName = URLUtil.guessFileName(
                url, contentDisposition, mimetype
            )
            alertDialog {
                title = "下载"
                message =
                    "你确定要下载这个文件吗？\n文件信息:\n文件名:$fileName\n保存目录:$publicDownloadsDirPath\n文件类型:${
                        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)
                    }"
                okButton {
                    val notificationId = (10000..50000).random()
                    setNotification(id = notificationId, "下载服务") {
                        setTitle("下载服务")
                        receiveEvent<Progress>("download_progress_$notificationId") {
                            message =
                                "文件名:$fileName · 已下载:${it.currentSize()} · 已开始:${it.useTime()} · 剩余大小:${it.remainSize()} · 剩余时间:${it.remainTime()}"
                            setProgress(100, it.progress(), false)
                        }
                        build()
                    }
                    scopeNet {
                        val file = Get<File>(url) {
                            setDownloadDir(publicDownloadsDirPath)
                            setDownloadFileName(
                                URLUtil.guessFileName(
                                    url, contentDisposition, mimetype
                                )
                            )
                            setDownloadFileNameConflict(true)
                            setDownloadFileNameDecode(true)
                            addDownloadListener(object : ProgressListener() {

                                override fun onProgress(p: Progress) {
                                    sendEvent(p, "download_progress_$notificationId")
                                }

                            })
                            toast("$fileName 已下载完成")
                        }.await()
                    }.catch {
                        removeNotification(notificationId)
                    }
                }
                cancelButton {

                }
            }.build()
        }
    }

    override fun onPause() {
        agentWeb.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onResume() {
        agentWeb.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        agentWeb.webLifeCycle.onDestroy()
        super.onDestroy()
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.web_refresh -> agentWeb.webCreator.webView.reload()
            R.id.copy_url -> {
                ClipboardUtils.copyText(agentWeb.webCreator.webView.url)
                PopTip.show("已复制网址到剪贴板")
            }

            R.id.open_in_browser -> CustomTabsIntent.Builder().build()
                .launchUrl(this@WebActivity, Uri.parse(agentWeb.webCreator.webView.url))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (agentWeb.handleKeyEvent(keyCode, event)) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private val webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
        }

    }

    private val webChromeClient: WebChromeClient = object : WebChromeClient() {

        override fun onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt()
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?,
        ) {
            snack("$origin 请求定位", "查看") {
                context.alertDialog {
                    title = "定位请求"
                    message = "网页 $origin 请求定位权限"
                    okButton("授予") {
                        callback?.invoke(origin, true, true)
                    }
                    cancelButton("拒绝") {
                        callback?.invoke(origin, false, true)
                    }
                    neutralButton("取消") {

                    }
                }.show()
            }
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            super.onPermissionRequest(request)
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest?) {
            super.onPermissionRequestCanceled(request)
        }

        override fun onJsAlert(
            webView: WebView,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            BaseAlertDialogBuilder(webView.context).setTitle("网页提示").setMessage(message)
                .setCancelable(false)
                .setPositiveButton("确定") { dialogInterface: DialogInterface, i: Int ->

                }.create().show()
            result.confirm()
            return true
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            supportActionBar?.title = title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.web_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        @SuppressLint("ObsoleteSdkInt")
        fun getUserAgent(ctx: Context?): String {
            val system_ua = System.getProperty("http.agent")
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                WebSettings.getDefaultUserAgent(ctx) + "__" + system_ua
            } else {
                WebView(ctx!!).settings.userAgentString + "__" + system_ua
            }
        }
    }
}