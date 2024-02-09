package com.manchuan.tools.activity.life

import android.animation.LayoutTransition
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.okhttp.trustSSLCertificate
import com.drake.net.utils.scopeNetLife
import com.drake.softinput.hideSoftInput
import com.drake.softinput.setWindowSoftInput
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.dp
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.androidktx.core.tip
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityTranslateBinding
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.utils.UiUtils
import org.json.JSONArray
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URLEncoder
import java.util.Locale

class TranslateActivity : BaseActivity(), OnInitListener {

    override fun onInit(p1: Int) {
        if (p1 == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.getDefault())
            ttsParam()
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tip("语音包丢失或语音不支持")
            }
        }
    }

    private lateinit var textToSpeech: TextToSpeech
    private var langId: List<*>? = null
    private var targetLang = "zh-CN"
    private var minLang = "auto"
    private fun ttsParam() {
        textToSpeech.setPitch(1.0f) // 设置音调，,1.0是常规
        textToSpeech.setSpeechRate(1.0f) //设定语速，1.0正常语速
    }

    private val binding by lazy {
        ActivityTranslateBinding.inflate(layoutInflater)
    }

    private val TRANSLATE_BASE_URL = "https://translate.zhongyi.team/" // 不需要翻墙即可使用


    fun translate(
        sourceLan: String,
        targetLan: String,
        content: String,
        success: (String) -> Unit,
        failure: (String) -> Unit,
    ) {
        var result = ""
        scopeNetLife {
            val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("44.202.150.166", 11257))
            val string = Get<String>(
                getTranslateUrl(
                    sourceLan, targetLan, content
                )
            ) {
                setHeader("User-Agent", userAgent())
                setClient {
                    trustSSLCertificate()
                    //proxy(proxy)
                }
            }.await()
            val jsonArray = JSONArray(string).getJSONArray(0)
            for (i in 0 until jsonArray.length()) {
                result += jsonArray.getJSONArray(i).getString(0)
            }
            success.invoke(result)
        }.catch {
            failure.invoke(it.message.toString())
        }
    }

    private fun getTranslateUrl(sourceLan: String, targetLan: String, content: String): String {
        return try {
            TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + URLEncoder.encode(
                content, "UTF-8"
            )
        } catch (e: Exception) {
            TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + content
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Google Translate"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        langId = listOf(*resources.getStringArray(R.array.language_id))
        textToSpeech = TextToSpeech(this, this)
        binding.sourceLanguage.setOnClickListener { view: View ->
            if (targetLang == minLang) {
                snack("不能选择同一语言")
            } else {
                showMinLangMenu(view)
            }
        }
        setWindowSoftInput(float = binding.translate, setPadding = true, margin = 16.dp.toInt())
        binding.apply {
            translateLayout.addNavigationBarHeightToMarginBottom()
            translate.addNavigationBarHeightToMarginBottom()
            scrollLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            sourceLanguage.setOnClickListener { view: View ->
                if (targetLang == minLang) {
                    snack("不能选择同一语言")
                } else {
                    showMinLangMenu(view)
                }
            }
            targetLanguage.setOnClickListener { view: View ->
                if (minLang == targetLang) {
                    snack("不能选择同一语言")
                } else {
                    showTarLangMenu(view)
                }
            }
            sourceCopy.throttleClick {
                if (editText.isTextEmpty()) {
                    snack("请先输入文字")
                } else {
                    textCopyThenPost(editText.textString)
                    snack("已复制")
                }
            }
            targetCopy.throttleClick {
                if (resultText.isTextEmpty()) {
                    snack("请先翻译文字")
                } else {
                    textCopyThenPost(resultText.textString)
                    snack("已复制")
                }
            }
            sourceSpeak.throttleClick {
                if (editText.isTextNotEmpty()) {
                    textToSpeech.speak(
                        binding.editText.textString, TextToSpeech.QUEUE_FLUSH, null, null
                    )
                } else {
                    snack("无文字")
                }
            }
            targetSpeak.throttleClick {
                if (resultText.isTextNotEmpty()) {
                    textToSpeech.speak(
                        resultText.textString, TextToSpeech.QUEUE_FLUSH, null, null
                    )
                } else {
                    snack("无文字")
                }
            }
            translate.throttleClick {
                if (editText.isTextEmpty()) {
                    snack("请输入文字")
                } else {
                    translate(minLang, targetLang, editText.textString, success = {
                        resultText.text(it)
                    }, failure = {
                        toast(it)
                    })
                }
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    private fun showMinLangMenu(view: View) {
        // View当前PopupMenu显示的相对View的位置
        val popupMenu = PopupMenu(this, view)
        // menu布局
        val language: List<*> = listOf(*resources.getStringArray(R.array.language))
        language.size
        var i = 0
        while (popupMenu.menu.size() < language.size) {
            popupMenu.menu.add(0, Menu.FIRST + i, i, language[i].toString())
            i++
        }
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener { item ->
            binding.sourceLanguage.text = item.title
            minLang = langId!![item.itemId - 1].toString()
            false
        }
        popupMenu.show()
    }

    private fun showTarLangMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val language: List<*> = listOf(*resources.getStringArray(R.array.language))
        language.size
        var i = 0
        while (popupMenu.menu.size() < language.size) {
            popupMenu.menu.add(0, Menu.FIRST + i, i, language[i].toString())
            i++
        }
        popupMenu.menu.removeItem(1)
        popupMenu.setOnMenuItemClickListener { item ->
            binding.targetLanguage.text = item.title
            targetLang = langId!![item.itemId - 1].toString()
            false
        }
        popupMenu.show()
    }

    override fun onPause() {
        super.onPause()
        hideSoftInput()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}