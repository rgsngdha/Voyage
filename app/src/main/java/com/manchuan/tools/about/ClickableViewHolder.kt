package com.manchuan.tools.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author drakeet
 */
open class ClickableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var url: String? = null

    init {
        itemView.setOnClickListener { v ->
            if (url != null) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                try {
                    v.context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setURL(url: String?) {
        this.url = url
    }
}