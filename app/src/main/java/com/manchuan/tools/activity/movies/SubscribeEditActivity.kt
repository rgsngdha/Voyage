package com.manchuan.tools.activity.movies

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.manchuan.tools.databinding.ActivitySubscribeEditBinding

class SubscribeEditActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySubscribeEditBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}