package com.manchuan.tools.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.ItemViewBinder

/**
 * @author drakeet
 */
class CategoryViewBinder : ItemViewBinder<Category, CategoryViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.about_page_item_category, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Category) {
        holder.category.text = item.title
        holder.actionIcon.setImageDrawable(item.actionIcon)
        holder.actionIcon.contentDescription = item.actionIconContentDescription
        if (item.actionIcon != null) {
            holder.actionIcon.visibility = View.VISIBLE
        } else {
            holder.actionIcon.visibility = View.GONE
        }
        holder.actionIcon.setOnClickListener(item.onActionClickListener)
    }

    override fun getItemId(item: Category): Long {
        return item.hashCode().toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var category: TextView
        var actionIcon: ImageButton

        init {
            category = itemView.findViewById(R.id.category)
            actionIcon = itemView.findViewById(R.id.actionIcon)
        }
    }
}