package com.manchuan.tools.interfaces

import com.drake.channel.sendEvent
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.extensions.json
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.model.QQLogin
import com.manchuan.tools.model.TencentLoginModel
import com.tencent.tauth.DefaultUiListener
import com.tencent.tauth.UiError
import org.json.JSONObject

class TencentLoginInterfaces(val success: (QQLogin) -> Unit) : DefaultUiListener() {
    override fun onComplete(response: Any?) {
        if (response == null) {
            sendEvent(TencentLoginModel(false, null), "tencent_login")
            loge("返回为空,登录失败")
            return
        }
        val jsonResponse = response as JSONObject
        if (jsonResponse.length() == 0) {
            sendEvent(TencentLoginModel(false, null), "tencent_login")
            loge("返回为空,登录失败")
            return
        }
        loge("登录成功")
        WaitDialog.show("正在处理")
        doComplete(response)
    }

    private fun doComplete(values: JSONObject?) {
        loge(tag = "QQ登录回调", values)
        this.success.invoke(json.decodeFromString(values.toString()))
    }

    override fun onError(e: UiError) {
        loge("fund", "onError: ${e.errorDetail}")
    }

    override fun onCancel() {
        loge("取消登录")
    }

}