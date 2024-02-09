package com.lxj.androidktx.core

import android.view.View
import androidx.recyclerview.widget.*
import com.blankj.utilcode.util.AdaptScreenUtils
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.carousel.CarouselLayoutManager
import com.lxj.androidktx.util.SafeGridLayoutManager
import com.lxj.androidktx.util.SafeLinearLayoutManager
import com.lxj.androidktx.util.SafeStaggeredGridLayoutManager
import java.util.*

/**
 * Description: RecyclerView扩展
 * Create by lxj, at 2018/12/25
 */

fun RecyclerView.flexbox(): RecyclerView {
    layoutManager = FlexboxLayoutManager(context)
    return this
}

fun RecyclerView.carousel(): RecyclerView {
    layoutManager = CarouselLayoutManager()
    return this
}

fun RecyclerView.vertical(spanCount: Int = 0, isStaggered: Boolean = false): RecyclerView {
    layoutManager = SafeLinearLayoutManager(context, RecyclerView.VERTICAL, false)
    if (spanCount != 0) {
        layoutManager = SafeGridLayoutManager(context, spanCount)
    }
    if (isStaggered) {
        layoutManager =
            SafeStaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
    }
    return this
}

fun RecyclerView.horizontal(spanCount: Int = 0, isStaggered: Boolean = false): RecyclerView {
    layoutManager = SafeLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    if (spanCount != 0) {
        layoutManager =
            SafeGridLayoutManager(context, spanCount, GridLayoutManager.HORIZONTAL, false)
    }
    if (isStaggered) {
        layoutManager =
            SafeStaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL)
    }
    return this
}


fun RecyclerView.smoothScrollToEnd() {
    if (adapter != null && adapter!!.itemCount > 0) {
        smoothScrollToPosition(adapter!!.itemCount - 1)
    }
}

fun RecyclerView.scrollToEnd() {
    if (adapter != null && adapter!!.itemCount > 0) {
        scrollToPosition(adapter!!.itemCount - 1)
    }
}

/**
 * 滚动置顶，只支持线性布局
 */
fun RecyclerView.scrollTop(position: Int) {
    if (layoutManager is LinearLayoutManager) {
        (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }
}

fun RecyclerView.disableItemAnimation(): RecyclerView {
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    return this
}

/**
 * 边界模糊
 */
fun RecyclerView.fadeEdge(
    length: Int = AdaptScreenUtils.pt2Px(25f),
    isHorizontal: Boolean = false
): RecyclerView {
    if (isHorizontal) isHorizontalFadingEdgeEnabled = true
    else isVerticalFadingEdgeEnabled = true
    overScrollMode = View.OVER_SCROLL_ALWAYS
    setFadingEdgeLength(length)
    return this
}

/**
 * 示例代码如下：
 * class UserDiffCallback(var oldData: List<User>?, var newData: List<User>?) : DiffUtil.Callback() {
        override fun getOldListSize() = oldData?.size ?: 0
        override fun getNewListSize() = newData?.size ?: 0

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if(oldData.isNullOrEmpty() || newData.isNullOrEmpty()) return false
            return oldData!![oldItemPosition].id == newData!![newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData!![oldItemPosition].name == newData!![newItemPosition].name
        }

        //局部更新 areItemsTheSame==true && areContentsTheSame==false 调用
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldData!![oldItemPosition]
            val newItem = newData!![newItemPosition]
            val bundle = Bundle()
            if(oldItem.name != newItem.name){
            bundle.putString("name", newItem.name)
            }
            return bundle
        }
    }
 *
 */
open class DiffCallback<T>(var oldData: List<T>?, var newData: List<T>?) : DiffUtil.Callback() {
    override fun getOldListSize() = oldData?.size ?: 0
    override fun getNewListSize() = newData?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldData.isNullOrEmpty() || newData.isNullOrEmpty()) return false
        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldData.isNullOrEmpty() || newData.isNullOrEmpty()) return false
        return false
    }

    //局部更新
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? = null
}
