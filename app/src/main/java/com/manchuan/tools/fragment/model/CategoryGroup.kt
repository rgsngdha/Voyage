package com.manchuan.tools.fragment.model

import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable
import com.drake.brv.item.ItemExpand
import com.drake.brv.item.ItemHover
import com.drake.brv.item.ItemPosition
import com.manchuan.tools.R

open class CategoryGroup(@DrawableRes var icon: Int, var title: String, var summary: String) :
    ItemExpand, ItemHover, ItemPosition, BaseObservable() {
    override var itemGroupPosition: Int = 0
    override fun getItemSublist(): List<Any?>? {
        return sublist
    }

    override var itemExpand: Boolean = true
        set(value) {
            field = value
            notifyChange()
        }
    var sublist: MutableList<FunctionModel> = mutableListOf()

    override var itemHover: Boolean = true
    override var itemPosition: Int = 0

    val expandIcon get() = if (itemExpand) R.drawable.primary_twotone_keyboard_arrow_down_24 else R.drawable.primary_twotone_keyboard_arrow_right_24

}