package com.manchuan.tools.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.ItemViewBinder

/**
 * @author drakeet
 */
@Deprecated(
    """You do not need to use Line now,
  we use {@link DividerItemDecoration} to automatically generate Lines."""
)
class LineViewBinder : ItemViewBinder<Line, LineViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.about_page_item_line, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Line) {}
    override fun getItemId(item: Line): Long {
        return item.hashCode().toLong()
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(
        itemView!!
    )
}