package com.manchuan.tools.fragment.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class FunctionModel(
    @Serializable var title: String,
    var unit: () -> Unit,
) : java.io.Serializable, Parcelable