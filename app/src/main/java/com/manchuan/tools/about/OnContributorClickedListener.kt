package com.manchuan.tools.about

import android.view.View
import androidx.annotation.CheckResult

interface OnContributorClickedListener {
    @CheckResult
    fun onContributorClicked(itemView: View, contributor: Contributor): Boolean
}