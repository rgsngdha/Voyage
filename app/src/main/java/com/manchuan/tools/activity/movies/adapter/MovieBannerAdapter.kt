package com.manchuan.tools.activity.movies.adapter

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.manchuan.tools.activity.movies.fragments.model.HistarTV
import com.manchuan.tools.databinding.MovieBannerItemBinding
import com.manchuan.tools.extensions.layoutInflater
import com.manchuan.tools.extensions.load
import com.youth.banner.adapter.BannerAdapter

class MovieBannerAdapter(data: List<HistarTV.PageProps.Header> = emptyList()) :
    BannerAdapter<HistarTV.PageProps.Header, MovieBannerAdapter.BannerViewHolder>(data) {

    override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BannerViewHolder {
        val img = MovieBannerItemBinding.inflate(parent.context.layoutInflater)
        img.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        return BannerViewHolder(img.root)
    }

    override fun onBindView(
        holder: BannerViewHolder,
        data: HistarTV.PageProps.Header,
        position: Int,
        size: Int,
    ) {
        val img = holder.itemView as ConstraintLayout
        val binding = MovieBannerItemBinding.bind(img)
        binding.image.load(
            data.posterImg, isCrossFade = true
        )
        binding.name.text = data.name
        binding.description.text = data.focusName
    }

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}