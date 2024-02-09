package com.manchuan.tools.activity.movies

import ando.file.core.FileUri
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.lifecycle.lifecycleScope
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.dylanc.longan.activity
import com.dylanc.longan.activityresult.registerForGetContentResult
import com.dylanc.longan.activityresult.registerForPickContentResult
import com.dylanc.longan.context
import com.dylanc.longan.externalFilesDirPath
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isJson
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.shareFile
import com.dylanc.longan.toast
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.lxj.androidktx.core.decryptAES
import com.lxj.androidktx.core.encryptAES
import com.lxj.androidktx.core.toJson
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.database.SourceEntity
import com.manchuan.tools.activity.movies.database.SourcesDatabase
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivitySubcribeManageBinding
import com.manchuan.tools.databinding.ItemSubscribeBinding
import com.manchuan.tools.extensions.base64Decoded
import com.manchuan.tools.extensions.base64Encode
import com.manchuan.tools.extensions.bitmapFromUri
import com.manchuan.tools.extensions.decodeFromPhoto
import com.manchuan.tools.extensions.inputDialog
import com.manchuan.tools.extensions.json
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.readTextFromUri
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.startActivity
import com.manchuan.tools.extensions.text
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File


class SubscribeManageActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySubcribeManageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "订阅管理"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.recyclerView.setup {
            addType<SourceEntity>(R.layout.item_subscribe)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemSubscribeBinding.bind(itemView)
                val model = getModel<SourceEntity>()
                binding.avatar.load(model.avatar, isCrossFade = true)
                binding.name.text(model.name)
                binding.summary.text(model.description)
                binding.switchs.isChecked = model.sourceSwitch
                binding.switchs.setOnCheckedChangeListener { buttonView, isChecked ->
                    model.sourceSwitch = isChecked
                    sourcesDatabase.getSourcesDao().updateSource(model)
                }
            }
            R.id.item.onLongClick {
                val model = getModel<SourceEntity>()
                BottomMenu.show(arrayOf("详情信息", "检查更新", "分享订阅", "删除订阅"))
                    .setMessage(model.name).onMenuItemClickListener =
                    OnMenuItemClickListener { dialog, text, index ->
                        when (text) {
                            "详情信息" -> {
                                BaseAlertDialogBuilder(activity).setTitle("详细信息").setMessage(
                                    "订阅名:${model.name}\n" + "订阅版本号:${model.version}\n" + "子订阅数:${model.sources?.size}\n" + "更新链接:${model.updateUrl?.ifEmpty { "未配置" }}"
                                ).setNegativeButton("确定", null).create().show()
                            }

                            "分享订阅" -> {
                                selector(
                                    listOf("文件分享"), "分享 ${model.name}"
                                ) { dialogInterface, s, i ->
                                    when (s) {
                                        "文件分享" -> {
                                            runCatching {
                                                val file =
                                                    File(externalFilesDirPath + "/${model.name}.vmsub")
                                                if (file.exists()) {
                                                    file.createNewFile()
                                                }
                                                file.writeText(
                                                    model.toJson(lenient = true).base64Encode()
                                                        .encryptAES("Voyager209900000"),
                                                    Charsets.UTF_8
                                                )
                                                snack("已保存到 ${file.absolutePath}")
                                                FileUri.getShareUri(file)?.let { it1 ->
                                                    shareFile(
                                                        it1, "将订阅文件分享到"
                                                    )
                                                }
                                            }.onFailure {
                                                snack("分享失败:${it.message}")
                                            }
                                        }
                                    }
                                }
                            }

                            "检查更新" -> {
                                if (model.updateUrl?.isNotEmpty()!!) {
                                    scopeDialog {

                                    }
                                } else {
                                    snack("该订阅未配置更新链接")
                                }
                            }

                            "删除订阅" -> {
                                runBlocking {
                                    runCatching {
                                        sourcesDatabase.getSourcesDao().deleteSource(model)
                                    }.onFailure {
                                        snack("订阅删除失败,可能是该订阅存在问题,或数据库已损坏")
                                    }.onSuccess {
                                        snack("删除成功")
                                        binding.state.refresh()
                                    }
                                }
                            }
                        }
                        false
                    }
            }
            R.id.item.onClick {
                val model = getModel<SourceEntity>()
                startActivity<SubscribeListActivity>(
                    bundle = arrayOf(
                        "id" to model.id
                    )
                )
            }
        }
        binding.state.onRefresh {
            scope {
                binding.recyclerView.models =
                    sourcesDatabase.getSourcesDao().queryAllSources()
            }.catch {
                it.printStackTrace()
                toast("订阅数据库错误，请尝试清空订阅数据库")
            }
        }.autoRefresh()
        intent.data?.let { uri ->
            runCatching {
                scopeLife {
                    withIO {
                        if (readTextFromUri(uri).decryptAES("Voyager209900000").base64Decoded()
                                .isJson()
                        ) {
                            val data = json.decodeFromString<SourceEntity>(
                                readTextFromUri(uri).decryptAES("Voyager209900000").base64Decoded()
                            )
                            data.id = if (sourcesDatabase.getSourcesDao().queryAllSources()
                                    .isEmpty()
                            ) 1 else sourcesDatabase.getSourcesDao().queryAllSources().size.inc()
                            sourcesDatabase.getSourcesDao().insertSource(data)
                            withMain {
                                snack("导入成功")
                                binding.state.refresh()
                            }
                        } else {
                            withMain {
                                snack("该订阅内容有误")
                            }
                        }
                    }
                }
            }.onFailure {
                snack("无法导入，该订阅内容有误：${it.message}")
            }
        }
    }

    private val sourcesDatabase by lazy {
        SourcesDatabase.getInstance(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishAfterTransition()
            R.id.imports -> {
                BottomMenu.show(
                    "导入订阅", null, arrayOf("从文件导入", "从二维码导入", "从订阅链接导入")
                ).setOnMenuItemClickListener { dialog, text, index ->
                    when (text) {

                        "从文件导入" -> {
                            textFileImport.launch("*/*")
                        }

                        "从二维码导入" -> {
                            importForQRCode.launchForImage()
                        }

                        "从订阅链接导入" -> {
                            inputDialog(
                                "从链接导入订阅", "请输入合规的订阅链接", "订阅"
                            ) { inputStr ->
                                if (inputStr.isWebUrl()) {
                                    scopeDialog {
                                        val string = Get<String>(inputStr).await()
                                        val data = json.decodeFromString<SourceEntity>(
                                            string.decryptAES("Voyager209900000").base64Decoded()
                                        )
                                        withIO {
                                            data.id = if (sourcesDatabase.getSourcesDao()
                                                    .queryAllSources().isEmpty()
                                            ) 1 else sourcesDatabase.getSourcesDao()
                                                .queryAllSources().size.inc()
                                            sourcesDatabase.getSourcesDao().insertSource(data)
                                            withMain {
                                                snack("导入成功")
                                                binding.state.refreshing()
                                            }
                                        }
                                    }.catch {
                                        snack("导入失败，该规则有误。")
                                    }
                                } else {
                                    toast("错误的订阅链接")
                                }
                            }
                        }
                    }
                    false
                }
            }

            R.id.create -> {
                InputSheet().show(context) {
                    title("创建订阅")
                    with(InputEditText("name") {
                        required()
                        label("必填")
                        hint("订阅名称")
                    })
                    with(InputEditText("description") {
                        required()
                        label("必填")
                        hint("订阅描述")
                    })
                    with(InputEditText("version") {
                        label("可选，默认为0")
                        hint("订阅版本号")
                        inputType(InputType.TYPE_CLASS_NUMBER)
                    })
                    onPositive { result ->
                        val name = result.getString("name")
                        val description = result.getString("description")
                        val version = result.getString("version")
                        if (name?.isNotEmpty()!! && description?.isNotEmpty()!!) {
                            runBlocking {
                                sourcesDatabase.getSourcesDao().insertSource(
                                    SourceEntity(
                                        if (sourcesDatabase.getSourcesDao().queryAllSources()
                                                .isEmpty()
                                        ) 1 else sourcesDatabase.getSourcesDao()
                                            .queryAllSources().size.inc(),
                                        avatar = (if (Global.userModel?.msg?.info?.pic != null) Global.userModel?.msg?.info?.pic else "").toString(),
                                        description = description,
                                        version = if (version?.isNotEmpty() == true) version.toInt() else 0,
                                        name = name,
                                        sourceSwitch = true
                                    )
                                )
                                binding.state.refreshing()
                            }
                        }
                        //val check = result.getBoolean("binge_watching") // Read value by passed key
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.subscribe_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val importForQRCode = registerForPickContentResult { uri ->
        if (uri != null) {
            // 处理 uri
            lifecycleScope.launch {
                runCatching {
                    val rawResult = decodeFromPhoto(bitmapFromUri(uri))
                    if (rawResult != null) {
                        rawResult.text
                        if (rawResult.text.isWebUrl()) {
                            scopeDialog {
                                val string = Get<String>(rawResult.text).await()
                                val gson: SourceEntity = Gson().fromJson(
                                    string.decryptAES("Voyager209900000").base64Decoded(),
                                    SourceEntity::class.java
                                )
                                gson.id = if (sourcesDatabase.getSourcesDao().queryAllSources()
                                        .isEmpty()
                                ) 1 else sourcesDatabase.getSourcesDao()
                                    .queryAllSources().size.inc()
                                sourcesDatabase.getSourcesDao().insertSource(gson)
                                snack("导入成功")
                                binding.state.refreshing()
                            }.catch {
                                snack("导入失败，订阅内容错误")
                            }
                        } else if (rawResult.text.isEmpty()) {
                            snack("识别内容为空")
                        } else {
                            val gson: SourceEntity = Gson().fromJson(
                                rawResult.text.decryptAES("Voyager209900000").base64Decoded(),
                                SourceEntity::class.java
                            )
                            gson.id = if (sourcesDatabase.getSourcesDao().queryAllSources()
                                    .isEmpty()
                            ) 1 else sourcesDatabase.getSourcesDao().queryAllSources().size.inc()
                            sourcesDatabase.getSourcesDao().insertSource(gson)
                            snack("导入成功")
                            binding.state.refreshing()
                        }
                    } else {
                        snack("识别失败")
                    }
                }.onFailure {
                    snack("导入失败")
                }
            }
        }
    }

    private val textFileImport = registerForGetContentResult {
        lifecycleScope.launch {
            it?.let { uri ->
                runCatching {
                    loge(readTextFromUri(uri).decryptAES("Voyager209900000").base64Decoded())
                    if (readTextFromUri(uri).decryptAES("Voyager209900000").base64Decoded()
                            .isJson()
                    ) {
                        runCatching {
                            val gson: SourceEntity = Gson().fromJson(
                                readTextFromUri(uri).decryptAES("Voyager209900000").base64Decoded(),
                                SourceEntity::class.java
                            )
                            gson.id = if (sourcesDatabase.getSourcesDao().queryAllSources()
                                    .isEmpty()
                            ) 1 else sourcesDatabase.getSourcesDao().queryAllSources().size.inc()
                            sourcesDatabase.getSourcesDao().insertSource(gson)
                        }.onFailure {
                            snack("订阅错误")
                        }.onSuccess {
                            snack("导入成功")
                            binding.state.refreshing()
                        }
                    } else {
                        snack("该订阅内容有误")
                    }
                }.onFailure {
                    snack("无法导入，该订阅内容有误。")
                }
            }
        }
    }
}

