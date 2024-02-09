package com.manchuan.tools.activity.app

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.debounce
import com.drake.net.utils.launchIn
import com.drake.statusbar.immersive
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.lxj.androidktx.core.flexbox
import com.manchuan.tools.R
import com.manchuan.tools.database.dailyTool
import com.manchuan.tools.database.imageTool
import com.manchuan.tools.database.mediaTool
import com.manchuan.tools.database.queryTool
import com.manchuan.tools.database.siteTool
import com.manchuan.tools.database.systemTool
import com.manchuan.tools.database.transformerTool
import com.manchuan.tools.databinding.ActivitySearchFunctionsBinding
import com.manchuan.tools.fragment.model.FunctionModel
import kotlinx.coroutines.flow.distinctUntilChanged
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class SearchFunctionsActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySearchFunctionsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "搜索功能"
        }
        immerseStatusBar(!isAppDarkMode)
        FastScrollerBuilder(binding.recyclerView).useMd2Style()
        binding.recyclerView.flexbox().setup {
            addType<FunctionModel>(R.layout.item_chip)
            R.id.item.onClick {
                val model = getModel<FunctionModel>()
                model.unit.invoke()
            }
        }
        binding.editText.debounce(200).distinctUntilChanged().launchIn(this) {
            if (binding.editText.isTextNotEmpty()) {
                binding.recyclerView.models = allFunctions.filter {
                    it.title.contains(
                        binding.editText.textString, ignoreCase = true
                    )
                }
            }
        }
    }

    private val allFunctions = arrayListOf<FunctionModel>().apply {
        addAll(dailyTool)
        addAll(queryTool)
        addAll(imageTool)
        addAll(mediaTool)
        addAll(systemTool)
        addAll(transformerTool)
        addAll(siteTool)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}