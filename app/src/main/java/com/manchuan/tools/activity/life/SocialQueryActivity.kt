package com.manchuan.tools.activity.life

import android.os.Bundle
import android.view.MenuItem
import com.crazylegend.viewbinding.viewBinding
import com.manchuan.tools.databinding.ActivitySocialQueryBinding
import rikka.material.app.MaterialActivity

class SocialQueryActivity : MaterialActivity() {

    private val binding by viewBinding(ActivitySocialQueryBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}