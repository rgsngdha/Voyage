package com.manchuan.tools.about

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.manchuan.tools.R
import com.manchuan.tools.about.multitype.ItemViewBinder
import timber.log.Timber

/**
 * @author drakeet
 */
class RecommendationViewBinder(private val activity: AbsAboutActivity) :
    ItemViewBinder<Recommendation, RecommendationViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(
            inflater.inflate(R.layout.about_page_item_recommendation, parent, false),
            activity
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Recommendation) {
        holder.setRecommendation(item, activity.imageLoader)
    }

    override fun getItemId(item: Recommendation): Long {
        return item.hashCode().toLong()
    }

    class ViewHolder(itemView: View, protected val activity: AbsAboutActivity) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var icon: ImageView
        var name: TextView
        var packageName: TextView
        var sizeView: TextView
        var description: TextView
        var recommendation: Recommendation? = null
        private var bottomSheet: BottomSheetDialog? = null

        init {
            icon = itemView.findViewById(R.id.icon)
            name = itemView.findViewById(R.id.name)
            packageName = itemView.findViewById(R.id.packageName)
            sizeView = itemView.findViewById(R.id.size)
            description = itemView.findViewById(R.id.description)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (v.id == R.id.google_play && bottomSheet != null) {
                openWithMarket(
                    v.context,
                    recommendation!!.packageName!!,
                    recommendation!!.downloadUrl!!
                )
                bottomSheet!!.dismiss()
            } else if (v.id == R.id.web && bottomSheet != null) {
                openWithWeb(v.context, recommendation!!)
                bottomSheet!!.dismiss()
            } else if (recommendation != null) {
                val listener = activity.onRecommendationClickedListener
                if (listener != null && listener.onRecommendationClicked(v, recommendation!!)) {
                    return
                }
                if (recommendation!!.openWithGooglePlay) {
                    bottomSheet = BottomSheetDialog(v.context)
                    bottomSheet!!.setContentView(R.layout.about_page_dialog_market_chooser)
                    bottomSheet!!.show()
                    // noinspection ConstantConditions
                    bottomSheet!!.findViewById<View>(R.id.web)!!.setOnClickListener(this)
                    // noinspection ConstantConditions
                    bottomSheet!!.findViewById<View>(R.id.google_play)!!
                        .setOnClickListener(this)
                } else {
                    openWithWeb(v.context, recommendation!!)
                }
            }
        }

        fun setRecommendation(recommendation: Recommendation, imageLoader: ImageLoader?) {
            this.recommendation = recommendation
            if (imageLoader != null) {
                icon.visibility = View.VISIBLE
                imageLoader.load(icon, recommendation.iconUrl!!)
            } else {
                icon.visibility = View.GONE
                Timber.tag(TAG)
                    .e("You should call AbsAboutActivity.setImageLoader() otherwise the icon will be gone.")
            }
            name.text = recommendation.appName
            packageName.text = recommendation.packageName
            description.text = recommendation.description
            @SuppressLint("SetTextI18n") val size = recommendation.downloadSize.toString() + "MB"
            sizeView.text = size
        }

        protected fun openWithWeb(context: Context, recommendation: Recommendation) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recommendation.downloadUrl)))
        }

        private fun openWithMarket(
            context: Context,
            targetPackage: String,
            defaultDownloadUrl: String
        ) {
            try {
                val googlePlayIntent =
                    context.packageManager.getLaunchIntentForPackage("com.android.vending")
                val comp = ComponentName(
                    "com.android.vending",
                    "com.google.android.finsky.activities.LaunchUrlHandlerActivity"
                )
                // noinspection ConstantConditions
                googlePlayIntent!!.component = comp
                googlePlayIntent.data = Uri.parse("market://details?id=$targetPackage")
                context.startActivity(googlePlayIntent)
            } catch (e: Throwable) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(defaultDownloadUrl)))
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "about-page"
    }
}