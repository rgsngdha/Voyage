package com.manchuan.tools.interfaces

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.manchuan.tools.activity.movies.fragments.model.HistarTV
import com.manchuan.tools.extensions.load
import com.manchuan.tools.model.banner.BannerModel
import com.youth.banner.adapter.BannerAdapter

class HomeBannerAdapter(data: List<Any> = emptyList()) :
    BannerAdapter<Any, HomeBannerAdapter.BannerViewHolder>(data) {

    override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BannerViewHolder {
        val img = AppCompatImageView(parent.context)
        img.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        img.scaleType = ImageView.ScaleType.CENTER_CROP
        return BannerViewHolder(img)
    }

    override fun onBindView(
        holder: BannerViewHolder,
        data: Any,
        position: Int,
        size: Int,
    ) {
        val img = holder.itemView as AppCompatImageView
        img.load(
            if (data is BannerModel.Data) data.image else (data as HistarTV.PageProps.Header).posterImg,
            isCrossFade = true,
            diskCacheStrategy = DiskCacheStrategy.ALL
        )
    }

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}