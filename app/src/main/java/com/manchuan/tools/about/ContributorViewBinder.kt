package com.manchuan.tools.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.ItemViewBinder

/**
 * @author drakeet
 */
class ContributorViewBinder(private val activity: AbsAboutActivity) :
    ItemViewBinder<Contributor, ContributorViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(
            inflater.inflate(R.layout.about_page_item_contributor, parent, false),
            activity
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Contributor) {
        if (item.avatarResId != 0) {
            holder.avatar.setImageResource(item.avatarResId)
        } else {
            Glide.with(holder.avatar).load(item.avatarUrl)
                .transition(DrawableTransitionOptions.withCrossFade()).into(holder.avatar)
        }
        holder.name.text = item.name
        holder.desc.text = item.desc
        holder.data = item
    }

    override fun getItemId(item: Contributor): Long {
        return item.hashCode().toLong()
    }

    class ViewHolder(itemView: View, protected val activity: AbsAboutActivity) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var avatar: ImageView
        var name: TextView
        var desc: TextView
        var data: Contributor? = null

        init {
            avatar = itemView.findViewById(R.id.avatar)
            name = itemView.findViewById(R.id.name)
            desc = itemView.findViewById(R.id.desc)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val listener = activity.onContributorClickedListener
            if (listener != null && listener.onContributorClicked(v, data!!)) {
                return
            }
            if (data!!.url != null) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data!!.url)
                try {
                    v.context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }
}