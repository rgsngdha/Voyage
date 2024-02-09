package com.manchuan.tools.about

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.ItemViewBinder

/**
 * @author drakeet
 */
class LicenseViewBinder : ItemViewBinder<License, LicenseViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.about_page_item_license, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, item: License) {
        holder.content.text = item.name + " - " + item.author
        holder.hint.text = """
            ${item.url}
            ${item.type}
            """.trimIndent()
        holder.setURL(item.url)
    }

    override fun getItemId(item: License): Long {
        return item.hashCode().toLong()
    }

    class ViewHolder(itemView: View) : ClickableViewHolder(itemView) {
        var content: TextView
        var hint: TextView

        init {
            content = itemView.findViewById(R.id.content)
            hint = itemView.findViewById(R.id.hint)
        }
    }
}