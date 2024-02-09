package com.manchuan.tools.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.drake.engine.base.app
import rikka.shizuku.Shizuku

class ShizukuService : BroadcastReceiver() {

    private val oneShotBinderReceivedListener = object : Shizuku.OnBinderReceivedListener {
        override fun onBinderReceived() {
            app.openFileOutput("on_boot", Context.MODE_PRIVATE).bufferedWriter().apply {
                write("binder received")
                newLine()
                write("permission granted: ${Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED}")
                newLine()
                //write("is_binding: ${isBinding.value}")
                newLine()
                //write("is_running: ${isRunning.value}")
                newLine()
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    write("starting service...")
                    //toggleService()
                    //isAutoStarted.value = true
                }
            }
            Shizuku.removeBinderReceivedListener(this)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Shizuku.addBinderReceivedListenerSticky(oneShotBinderReceivedListener)
        }
    }

}