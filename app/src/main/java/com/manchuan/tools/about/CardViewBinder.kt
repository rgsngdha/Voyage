package com.manchuan.tools.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.ItemViewBinder

/**
 * @author drakeet
 */
class CardViewBinder : ItemViewBinder<Card, CardViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.about_page_item_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Card) {
        holder.content.setLineSpacing(
            item.lineSpacingExtra.toFloat(),
            holder.content.lineSpacingMultiplier
        )
        holder.content.text = item.content
    }

    override fun getItemId(item: Card): Long {
        return item.hashCode().toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var content: TextView

        init {
            content = itemView.findViewById(R.id.content)
        }
    }
}