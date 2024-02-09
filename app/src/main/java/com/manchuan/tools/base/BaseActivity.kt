package com.manchuan.tools.base

//noinspection SuspiciousImport
import android.R
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import com.anggrayudi.storage.SimpleStorageHelper
import com.highcapable.betterandroid.ui.extension.component.base.DisplayDensity
import com.hjq.language.MultiLanguages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import rikka.material.app.MaterialActivity


open class BaseActivity : MaterialActivity(), DisplayDensity {

    private val job = Job()
    protected val ioScope = CoroutineScope(Dispatchers.IO + job)
    protected val uiScope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var storageHelper: SimpleStorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageHelper = SimpleStorageHelper(this, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        storageHelper.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storageHelper.onRestoreInstanceState(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        // 绑定语种
        super.attachBaseContext(MultiLanguages.attach(newBase))
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}