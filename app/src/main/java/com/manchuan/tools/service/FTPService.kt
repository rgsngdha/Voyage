package com.manchuan.tools.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.dylanc.longan.toast
import com.manchuan.tools.database.FTPProperties
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission


class FTPService : Service() {

    var ftpServer: FtpServer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        val isStart = MutableLiveData(false)
    }

    override fun onCreate() {
        super.onCreate()
        runCatching {
            init()
        }.onFailure {
            toast("FTP服务启动失败")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
        toast("FTP服务已关闭")
    }

    @Throws(FtpException::class)
    fun init() {
        release()
        startFtp()
    }

    @Throws(FtpException::class)
    private fun startFtp() {
        val serverFactory = FtpServerFactory()
        //设置访问用户名和密码还有共享路径
        val baseUser = BaseUser()
        if (FTPProperties.isAnonymous) {
            baseUser.name = "anonymous" //匿名
        } else {
            baseUser.name = FTPProperties.user
            baseUser.password = FTPProperties.password
        }
        baseUser.homeDirectory = FTPProperties.rootPath
        val authorities: MutableList<Authority> = ArrayList()
        authorities.add(WritePermission())
        baseUser.authorities = authorities
        serverFactory.userManager.save(baseUser)
        val factory = ListenerFactory()
        factory.port = FTPProperties.port
        serverFactory.addListener("default", factory.createListener())
        ftpServer = serverFactory.createServer()
        ftpServer?.start()
        isStart.value = true
        toast("FTP服务启动成功")
    }

    fun release() {
        stopFtp()
        isStart.value = false
    }

    private fun stopFtp() {
        ftpServer?.stop()
        ftpServer = null
    }


}