package com.manchuan.tools.activity.lockscreen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.manchuan.tools.databinding.ActivityLockPictureBinding

class LockPictureActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLockPictureBinding.inflate(layoutInflater)
    }
    private val url = "https://www.photoworld.com.cn/feed"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}