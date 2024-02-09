package com.manchuan.tools.activity.game

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.JsResult
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.FrameLayout
import com.dylanc.longan.safeIntentExtras
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityFullscreenGameBinding
import com.manchuan.tools.extensions.windowBackground

class FullscreenGameActivity : BaseActivity() {

    private lateinit var binding: ActivityFullscreenGameBinding
    private lateinit var agentWeb: AgentWeb

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_BAR).fullScreen(true).init()
        agentWeb =
            AgentWeb.with(this).setAgentWebParent(binding.layout, FrameLayout.LayoutParams(-1, -1))
                .useDefaultIndicator().setWebViewClient(webViewClient)
                .setWebChromeClient(webChromeClient)
                .setSecurityType(AgentWeb.SecurityType.DEFAULT_CHECK).createAgentWeb().ready()
                .go(safeIntentExtras<String>("url").value)
        agentWeb.webCreator.webParentLayout.setBackgroundColor(windowBackground())
        agentWeb.agentWebSettings?.webSettings?.userAgentString = WebActivity.getUserAgent(this)
        agentWeb.agentWebSettings?.webSettings?.javaScriptEnabled = true
    }

    override fun onStart() {
        agentWeb.webLifeCycle.onResume()
        super.onStart()
    }

    override fun onDestroy() {
        agentWeb.webLifeCycle.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        agentWeb.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (agentWeb.webCreator.webView.canGoBack()) {
                agentWeb.webCreator.webView.goBack()
            } else {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
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
        }
    }

}