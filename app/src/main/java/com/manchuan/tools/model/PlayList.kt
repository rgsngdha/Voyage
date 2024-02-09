package com.manchuan.tools.model

import androidx.databinding.BaseObservable

data class PlayList(var checked: Boolean = false, var title: String, var url: String) :
    BaseObservable()
