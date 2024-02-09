package com.manchuan.tools.activity.site

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import com.anggrayudi.storage.extension.launchOnUiThread
import com.drake.engine.utils.throttleClick
import com.dylanc.longan.context
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPortsScanBinding
import com.stealthcopter.networktools.PortScan
import com.stealthcopter.networktools.PortScan.PortListener
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class PortsScanActivity : BaseActivity() {
    private val binding by lazy {
        ActivityPortsScanBinding.inflate(layoutInflater)
    }

    private val type = arrayOf(
        "TCP", "UDP"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "端口扫描"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.apply {
            this.types.setAdapter(
                ArrayAdapter(
                    context, R.layout.cat_exposed_dropdown_popup_item, type
                )
            )
            scan.throttleClick {
                if (address.isTextEmpty()) {
                    addressInput.error = "不能为空"
                } else {
                    if (port.isTextEmpty()) {
                        if (types.textString == "TCP") {
                            WaitDialog.show("扫描中...")
                            PortScan.onAddress(address.textString).setPortsAll().setMethodTCP()
                                .setTimeOutMillis(5.seconds.toInt(DurationUnit.SECONDS))
                                .doScan(object : PortListener {
                                    override fun onResult(portNo: Int, open: Boolean) {

                                    }

                                    @SuppressLint("SetTextI18n")
                                    override fun onFinished(openPorts: ArrayList<Int>) {
                                        launchOnUiThread {
                                            TipDialog.show("扫描完成", WaitDialog.TYPE.SUCCESS)
                                            resultText.setText(
                                                "已开放端口:${
                                                    arrayOf<ArrayList<*>>(
                                                        openPorts
                                                    ).contentToString().replace("[[", "")
                                                        .replace("]]", "")
                                                }"
                                            )
                                        }
                                    }

                                })
                        } else {
                            WaitDialog.show("扫描中...")
                            PortScan.onAddress(address.textString).setPortsAll().setMethodUDP()
                                .setTimeOutMillis(5.seconds.toInt(DurationUnit.SECONDS))
                                .doScan(object : PortListener {
                                    override fun onResult(portNo: Int, open: Boolean) {

                                    }

                                    @SuppressLint("SetTextI18n")
                                    override fun onFinished(openPorts: ArrayList<Int>) {
                                        launchOnUiThread {
                                            TipDialog.show("扫描完成", WaitDialog.TYPE.SUCCESS)
                                            resultText.setText(
                                                "已开放端口:${
                                                    arrayOf<ArrayList<*>>(
                                                        openPorts
                                                    ).contentToString().replace("[[", "")
                                                        .replace("]]", "")
                                                }"
                                            )
                                        }
                                    }

                                })
                        }
                    } else {
                        if (types.textString == "TCP") {
                            WaitDialog.show("扫描中...")
                            PortScan.onAddress(address.textString).setPort(port.textString.toInt())
                                .setMethodTCP()
                                .setTimeOutMillis(5.seconds.toInt(DurationUnit.SECONDS))
                                .doScan(object : PortListener {
                                    @SuppressLint("SetTextI18n")
                                    override fun onResult(portNo: Int, open: Boolean) {
                                        launchOnUiThread {
                                            resultText.setText(
                                                "端口号:$portNo,状态:" + getOpenState(
                                                    open
                                                )
                                            )
                                        }
                                    }

                                    @SuppressLint("SetTextI18n")
                                    override fun onFinished(openPorts: ArrayList<Int>) {
                                        launchOnUiThread {
                                            TipDialog.show("扫描完成", WaitDialog.TYPE.SUCCESS)
                                        }
                                    }

                                })
                        } else {
                            WaitDialog.show("扫描中...")
                            PortScan.onAddress(address.textString).setPort(port.textString.toInt())
                                .setMethodUDP()
                                .setTimeOutMillis(5.seconds.toInt(DurationUnit.SECONDS))
                                .doScan(object : PortListener {
                                    @SuppressLint("SetTextI18n")
                                    override fun onResult(portNo: Int, open: Boolean) {
                                        launchOnUiThread {
                                            resultText.setText(
                                                "端口号:$portNo,状态:" + getOpenState(
                                                    open
                                                )
                                            )
                                        }
                                    }

                                    @SuppressLint("SetTextI18n")
                                    override fun onFinished(openPorts: ArrayList<Int>) {
                                        launchOnUiThread {
                                            TipDialog.show("扫描完成", WaitDialog.TYPE.SUCCESS)
                                        }
                                    }

                                })
                        }
                    }
                }
            }
        }
    }

    private fun getOpenState(state: Boolean): String {
        return if (state) {
            "开启"
        } else {
            "关闭"
        }
    }
}