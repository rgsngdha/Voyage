package com.manchuan.tools.user

import android.net.Uri
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.dylanc.longan.encryptMD5
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.database.Global
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.model.AddSites
import com.manchuan.tools.user.model.AlertName
import com.manchuan.tools.user.model.AlertPass
import com.manchuan.tools.user.model.BindEmail
import com.manchuan.tools.user.model.CardPay
import com.manchuan.tools.user.model.ForgetPassword
import com.manchuan.tools.user.model.LoginModel
import com.manchuan.tools.user.model.QQBindModel
import com.manchuan.tools.user.model.RegisterModel
import com.manchuan.tools.user.model.SetUp
import com.manchuan.tools.user.model.SignModel
import com.manchuan.tools.user.model.UploadAvatarModel
import com.manchuan.tools.user.model.UserInfoById
import com.manchuan.tools.user.model.UserInfoModel
import com.manchuan.tools.user.model.VerifyCode
import com.manchuan.tools.user.model.VerifyRole
import com.manchuan.tools.user.model.VerifyVipModel
import java.io.File
import java.net.Proxy

const val appId = ""
const val appKey = ""
const val host = "https://user.zhongyi.team/api.php"
const val login = "$host?act=user_logon&app=$appId"
const val tencentLoginUrl = "$host?act=qq_login&app=$appId"
const val register = "$host?act=user_reg&app=$appId"
const val forgetPassword = "$host?act=seek_pass&app=$appId"
const val userInfo = "$host?act=get_info&app=$appId"
const val avatar = "$host?act=upic&app=$appId"
const val sign = "$host?act=clock&app=$appId"
const val verifyVip = "$host?act=get_vip&app=$appId"
const val verifyRole = "$host?act=is_role_can_add&app=$appId"
const val alertName = "$host?act=alter_name&app=$appId"
const val bindEmail = "$host?act=email_bind&app=$appId"
const val verifyCode = "$host?act=afcrc&app=$appId"
const val setUp = "$host?act=set_up&app=$appId"
const val alertPass = "$host?act=alter_pass&app=$appId"
const val cardPay = "$host?act=card&app=$appId"
const val notification = "$host?act=notice&app=$appId"
const val addSite = "$host?act=add_sites&app=$appId"
const val userInfoById = "$host?act=get_user_info&app=$appId"
const val qqBind = "$host?act=qq_bind&app=$appId"


val timeMills: Long
    get() = System.currentTimeMillis() / 100L

val idVerify: String
    get() = Global.idVerify


private fun getLoginSign(account: String, password: String): String {
    return ("account=$account&password=$password&markcode=$idVerify&t=$timeMills&$appKey").encryptMD5()
}

fun tencentLoginSign(openid: String, accessToken: String, inviteId: String): String {
    return ("openid=$openid&access_token=$accessToken&qqappid=${Global.AppId}&inv=$inviteId&markcode=$idVerify&t=$timeMills&$appKey").encryptMD5()
}

fun tencentBindSign(token: String, openid: String, accessToken: String): String {
    return ("token=$token&openid=$openid&access_token=$accessToken&qqappid=${Global.AppId}&t=$timeMills&$appKey").encryptMD5()
}

private fun getForgetPasswordSign(
    email: String,
    verifyCode: String,
    password: String,
): String {
    return ("email=$email&crc=$verifyCode&newpassword=$password&t=$timeMills&$appKey").encryptMD5()
}

private fun getRegSign(name: String, account: String, password: String, inv: String): String {
    return ("name=$name&user=$account&password=$password&inv=$inv&markcode=$idVerify&t=$timeMills&$appKey").encryptMD5()
}

private fun getHeaderSign(token: String, upt: String): String {
    return ("token=$token&upt=$upt&t=$timeMills&$appKey").encryptMD5()
}

private fun getUserInfoSign(token: String): String {
    return ("token=$token&t=$timeMills&$appKey").encryptMD5()
}

private fun getUserInfoByIdSign(token: String, id: String): String {
    return ("token=$token&id=$id&t=$timeMills&$appKey").encryptMD5()
}

private fun clockSign(token: String): String {
    return ("token=$token&t=$timeMills&$appKey").encryptMD5()
}

private fun verifyVipSign(token: String): String {
    return ("token=$token&t=$timeMills&$appKey").encryptMD5()
}

private fun verifyRoleSign(token: String): String {
    return ("token=$token&t=$timeMills&$appKey").encryptMD5()
}

private fun alertNameSign(token: String, name: String): String {
    return ("token=$token&name=$name&t=$timeMills&$appKey").encryptMD5()
}

private fun bindEmailSign(token: String, email: String, verifyCode: String): String {
    return ("token=$token&email=$email&crc=$verifyCode&t=$timeMills&$appKey").encryptMD5()
}

private fun verifyCodeSign(email: String, type: String): String {
    return ("email=$email&type=$type&t=$timeMills&$appKey").encryptMD5()
}

private fun setUpSign(token: String, account: String, password: String): String {
    return ("token=$token&user=$account&password=$password&t=$timeMills&$appKey").encryptMD5()
}

private fun addSiteSign(
    token: String,
    name: String,
    description: String,
    url: String,
    image: String,
    author: String,
    authorEmail: String,
    authorId: String,
): String {
    return ("token=$token&name=$name&description=$description&url=$url&image=$image&author=$author&author_email=$authorEmail&author_id=$authorId&t=$timeMills&$appKey").encryptMD5()
}

private fun alertPassSign(account: String, password: String, newPassword: String): String {
    return ("user=$account&password=$password&newpassword=$newPassword&t=$timeMills&$appKey").encryptMD5()
}

private fun cardPaySign(account: String, mainCard: String = "", card: String): String {
    return ("account=$account&mainkm=$mainCard&kami=$card&t=$timeMills&$appKey").encryptMD5()
}

private fun cardPayByTokenSign(token: String, mainCard: String = "", card: String): String {
    return ("token=$token&mainkm=$mainCard&kami=$card&t=$timeMills&$appKey").encryptMD5()
}

fun qqBindAccount(
    token: String,
    openid: String,
    accessToken: String,
    success: (QQBindModel) -> Unit,
    failed: ((String) -> Unit)? = null
) {
    WaitDialog.show("正在绑定...")
    scopeNet {
        val qqBindModel = Post<QQBindModel>(qqBind) {
            param("token", token)
            param("openid", openid)
            param("access_token", accessToken)
            param("qqappid", Global.AppId)
            param("t", timeMills)
            param("sign", tencentBindSign(token, openid, accessToken))
            converter = SerializationConverter("200", "code", "code")
        }.await()
        success.invoke(qqBindModel)
    }.catch {
        failed?.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}


fun userInfoById(
    token: String, id: String, success: (UserInfoById) -> Unit, failed: (String) -> Unit
) {
    scopeNet {
        val userInfoById = Post<UserInfoById>(userInfoById) {
            param("token", token)
            param("id", id)
            param("t", timeMills)
            param("sign", getUserInfoByIdSign(token, id))
            converter = SerializationConverter("200", "code", "code")
        }.await()
        success.invoke(userInfoById)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}


fun verifyRole(token: String, success: (VerifyRole) -> Unit, failed: (String) -> Unit) {
    scopeNet {
        val verifyRole = Post<VerifyRole>(verifyRole) {
            param("token", token)
            param("t", timeMills)
            param("sign", verifyRoleSign(token))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(verifyRole)
    }.catch {
        it.printStackTrace()
        loge(it)
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun addSites(
    token: String,
    name: String,
    description: String,
    url: String,
    image: String = "",
    author: String,
    authorEmail: String = "",
    authorId: String,
    success: (AddSites) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val addSites = Post<AddSites>(addSite) {
            param("token", token)
            param("name", name)
            param("description", description)
            param("url", url)
            param("image", image)
            param("author", author)
            param("author_email", authorEmail)
            param("author_id", authorId)
            param("t", timeMills)
            param(
                "sign",
                addSiteSign(token, name, description, url, image, author, authorEmail, authorId)
            )
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(addSites)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun cardPay(
    account: String,
    mainCard: String = "",
    card: String,
    success: (CardPay) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val cardPay = Post<CardPay>(cardPay) {
            param("account", account)
            param("mainkm", mainCard)
            param("kami", card)
            param("t", timeMills)
            param("sign", cardPaySign(account, mainCard, card))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(cardPay)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun cardPayByToken(
    token: String,
    mainCard: String = "",
    card: String,
    success: (CardPay) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val cardPay = Post<CardPay>(cardPay) {
            param("token", token)
            param("mainkm", mainCard)
            param("kami", card)
            param("t", timeMills)
            param("sign", cardPayByTokenSign(token, mainCard, card))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(cardPay)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun alertPass(
    account: String,
    password: String,
    newPassword: String,
    success: (AlertPass) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val alertPass = Post<AlertPass>(alertPass) {
            param("user", account)
            param("password", password)
            param("newpassword", newPassword)
            param("t", timeMills)
            param("sign", alertPassSign(account, password, newPassword))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(alertPass)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun setUp(
    token: String,
    account: String,
    password: String,
    success: (SetUp) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val setUp = Post<SetUp>(setUp) {
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            param("token", token)
            param("user", account)
            param("password", password)
            param("t", timeMills)
            param("sign", setUpSign(token, account, password))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(setUp)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun verifyCode(
    email: String,
    type: String = "reg",
    success: (VerifyCode) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val verifyCodeModel = Post<VerifyCode>(verifyCode) {
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            param("email", email)
            param("type", type)
            param("t", timeMills)
            param("sign", verifyCodeSign(email, type))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(verifyCodeModel)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun bindEmail(
    token: String,
    email: String,
    verifyCode: String,
    success: (BindEmail) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val bindEmailModel = Post<BindEmail>(bindEmail) {
            param("token", token)
            param("email", email)
            param("crc", verifyCode)
            param("t", timeMills)
            param("sign", bindEmailSign(token, email, verifyCode))
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(bindEmailModel)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun alertName(token: String, name: String, success: (AlertName) -> Unit, failed: (String) -> Unit) {
    scopeNet {
        val alertNameModel = Post<AlertName>(alertName) {
            param("token", token)
            param("name", name)
            param("t", timeMills)
            param("sign", alertNameSign(token, name))
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(alertNameModel)
    }.catch {
        it.printStackTrace()
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}


fun verifyVip(token: String, success: (VerifyVipModel) -> Unit, failed: (String) -> Unit) {
    scopeNet {
        val verifyVipModel = Post<VerifyVipModel>(verifyVip) {
            param("token", token)
            param("t", timeMills)
            param("sign", verifyVipSign(token))
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "code")
        }.await()
        success.invoke(verifyVipModel)
    }.catch {
        it.printStackTrace()
        loge(it)
        runCatching {
            if (it.message?.toInt() == 201) {
                failed.invoke("该功能仅会员可用")
            }
        }.onFailure {
            failed.invoke(it.message.toString().ifEmpty { "未知错误" })
        }
    }
}

fun sign(token: String, success: (SignModel) -> Unit, failed: (String) -> Unit) {
    scopeNet {
        val signModel = Post<SignModel>(sign) {
            param("token", token)
            param("t", timeMills)
            param("sign", clockSign(token))
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(signModel)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}


fun login(
    account: String,
    password: String, success: (LoginModel) -> Unit, failed: (String) -> Unit,
) {
    scopeNet {
        WaitDialog.show("正在登录")
        val string = Post<LoginModel>(login) {
            param("account", account)
            param("password", password)
            param("markcode", idVerify)
            param("t", timeMills)
            param("sign", getLoginSign(account, password))
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(string)
    }.catch {
        it.printStackTrace()
        loge(it.message)
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
        TipDialog.show("登录失败", WaitDialog.TYPE.ERROR)
    }
}


fun register(
    name: String,
    account: String,
    password: String,
    invite: String,
    success: (RegisterModel) -> Unit, failed: (String) -> Unit,
) {
    scopeNet {
        WaitDialog.show("正在注册...")
        val string = Post<RegisterModel>(register) {
            param("name", name)
            param("user", account)
            param("password", password)
            param("inv", invite)
            param("markcode", idVerify)
            param("t", timeMills)
            param("sign", getRegSign(name, account, password, invite))
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(string)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
    WaitDialog.dismiss()
}


fun userInfo(token: String, success: (UserInfoModel) -> Unit, failed: (String) -> Unit) {
    scopeNet {
        val userInfoModel = Post<UserInfoModel>(userInfo) {
            param("token", token)
            param("t", timeMills)
            param("sign", getUserInfoSign(token))
            setClient {
                proxy(Proxy.NO_PROXY)
            }
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(userInfoModel)
    }.catch {
        loge(it)
        it.printStackTrace()
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}


fun uploadAvatar(
    token: String,
    file: File,
    success: (UploadAvatarModel) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val uploadAvatarModel = Post<UploadAvatarModel>(avatar) {
            param("file", file)
            param("token", token)
            param("upt", "bbp")
            param("t", timeMills)
            param("sign", getHeaderSign(token, "bbp"))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(uploadAvatarModel)
    }.catch {
        it.printStackTrace()
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun uploadAvatar(
    token: String,
    file: Uri,
    success: (UploadAvatarModel) -> Unit,
    failed: (String) -> Unit,
) {
    scopeNet {
        val uploadAvatarModel = Post<UploadAvatarModel>(avatar) {
            param("file", file)
            param("token", token)
            param("upt", "bbp")
            param("t", timeMills)
            param("sign", getHeaderSign(token, "bbp"))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(uploadAvatarModel)
    }.catch {
        it.printStackTrace()
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}

fun forgetPassword(
    email: String,
    verifyCode: String,
    newpassword: String, success: (ForgetPassword) -> Unit, failed: (String) -> Unit,
) {
    scopeNet {
        val forgetPasswordModel = Post<ForgetPassword>(forgetPassword) {
            param("email", email)
            param("crc", verifyCode)
            param("newpassword", newpassword)
            param("t", timeMills)
            param("sign", getForgetPasswordSign(email, verifyCode, newpassword))
            converter = SerializationConverter("200", "code", "msg")
        }.await()
        success.invoke(forgetPasswordModel)
    }.catch {
        failed.invoke(it.message.toString().ifEmpty { "未知错误" })
    }
}