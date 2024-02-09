package com.manchuan.tools.activity.images

import android.os.Bundle
import android.util.Base64
import com.crazylegend.viewbinding.viewBinding
import com.dylanc.longan.activityresult.registerForGetContentResult
import com.dylanc.longan.toast
import com.lxj.androidktx.core.click
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityImageCryptBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.publicPicturesDirPath
import com.manchuan.tools.user.timeMills
import com.mcxiaoke.koi.ext.addToMediaStore
import java.io.File

class ImageCryptActivity : BaseActivity() {

    private val binding by viewBinding(ActivityImageCryptBinding::inflate)
    private var path = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "图片Base64加解密"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.apply {
            colorPicker.click {
                pickContentLauncher.launch("*/*")
            }
            encrypt.click {
                if (path.isNotEmpty()) {
                    runCatching {
                        val file = File(path)
                        val str = Base64.encodeToString(file.readBytes(), Base64.DEFAULT)
                        val output = File(publicPicturesDirPath, "$timeMills.png")
                        output.writeText(str)
                        addToMediaStore(output)
                        toast("保存成功")
                    }.onFailure {
                        toast("保存失败")
                    }
                } else {
                    toast("请先选择文件")
                }
            }
            decrypt.click {
                if (path.isNotEmpty()) {
                    runCatching {
                        val file = File(path)
                        val str = Base64.decode(file.readText(), Base64.DEFAULT)
                        val output = File(publicPicturesDirPath, "$timeMills.png")
                        output.writeBytes(str)
                        addToMediaStore(output)
                        toast("保存成功")
                    }.onFailure {
                        toast("保存失败")
                    }
                } else {
                    toast("请先选择文件")
                }
            }
        }
    }

    private val pickContentLauncher = registerForGetContentResult { uri ->
        if (uri != null) {
            // 处理 uri
            binding.colorString.text = "已选择"
            path = getPath(uri)
        }
    }

}