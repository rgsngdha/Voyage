package com.manchuan.tools.activity.life.database

import com.drake.serialize.serialize.serialLiveData
import kotlinx.serialization.Serializable

object StepDatabase {
    val historyAccount by serialLiveData(mutableListOf<StepAccountAndPassword>())
}

@Serializable
data class StepAccountAndPassword(var account: String, var password: String) : java.io.Serializable