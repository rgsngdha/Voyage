package com.manchuan.tools.activity.video.anime

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Post
import com.drake.net.utils.scope
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.gone
import com.lzx.starrysky.utils.formatTime
import com.manchuan.tools.R
import com.manchuan.tools.activity.video.model.TraceMoe
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityTraceMoeBinding
import com.manchuan.tools.databinding.ItemTraceMoeBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter
import com.mcxiaoke.koi.ext.fileNameWithoutExtension
import java.util.concurrent.TimeUnit

class TraceMoeActivity : BaseActivity() {

    private val binding by viewBinding(ActivityTraceMoeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "以图搜番"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.apply {
            select.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            recyclerView.linear().setup {
                addType<TraceMoe.Result>(R.layout.item_trace_moe)
                onBind {
                    val binding = getBinding<ItemTraceMoeBinding>()
                    val model = getModel<TraceMoe.Result>()
                    binding.title.text = model.anilist.title.native
                    binding.subtitle.text = model.anilist.title.romaji
                    if (model.anilist.synonyms.isEmpty()) {
                        binding.nameLay.gone()
                    } else {
                        binding.contentName.text =
                            model.anilist.synonyms.toTypedArray().contentToString().replace("[", "")
                                .replace("]", "")
                    }
                    binding.englishName.text = model.anilist.title.english
                    binding.episodeCount.text = model.episode.toString()
                    binding.playProgress.text = (model.from.toInt().toLong() * 1000L).formatTime()
                    binding.sameNum.text = "%.4f".format(model.similarity)
                    binding.image.load(
                        model.image,
                        isCrossFade = true,
                        isForceOriginalSize = true,
                        skipMemory = true
                    )
                }
            }
        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.aiPaint.animateGone()
            binding.page.animateVisible()
            binding.page.onRefresh {
                scope {
                    val cutBorders =
                        Post<TraceMoe>("https://api.trace.moe/search?anilistInfo&cutBorders") {
                            param("file", uri)
                            param("filename", getPath(uri).fileNameWithoutExtension())
                            setClient {
                                readTimeout(1, TimeUnit.MINUTES)
                            }
                            converter = SerializationConverter("", "", "error")
                        }.await()
                    binding.recyclerView.models = cutBorders.result
                }
            }.autoRefresh()
        } else {

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.trace_moe, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.add_photo -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        return super.onOptionsItemSelected(item)
    }

}