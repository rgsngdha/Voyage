package com.manchuan.tools.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.manchuan.tools.R
import java.util.Objects

@SuppressLint("AppCompatCustomView")
class MaterialCircleIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialCircleIconViewStyle,
    defStyleRes: Int = R.style.MaterialCircleIconViewLight
) : ImageView(context, attrs, defStyleAttr, defStyleRes) {
    private var mIconForegroundChroma: String? = null
    private var mIconBackgroundChroma: String? = null
    private var mColorName: String? = null

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.MaterialCircleIconView, defStyleAttr, defStyleRes
        )
        mIconBackgroundChroma =
            if (a.hasValue(R.styleable.MaterialCircleIconView_iconBackgroundChroma)) ({
                Objects.requireNonNull(
                    a.getString(
                        R.styleable.MaterialCircleIconView_iconBackgroundChroma
                    )
                )
            }).toString() else {
                "50"
            }
        mIconForegroundChroma =
            if (a.hasValue(R.styleable.MaterialCircleIconView_iconForegroundChroma)) ({
                Objects.requireNonNull(
                    a.getString(
                        R.styleable.MaterialCircleIconView_iconForegroundChroma
                    )
                )
            }).toString() else {
                "50"
            }
        mColorName =
            if (a.hasValue(R.styleable.MaterialCircleIconView_iconColorName)) ({
                Objects.requireNonNull(
                    a.getString(
                        R.styleable.MaterialCircleIconView_iconColorName
                    )
                )
            }).toString() else {
                "blue"
            }
        a.recycle()
        updateIconBackgroundColor()
        updateIconForegroundColor()
    }

    var iconForegroundChroma: String?
        get() = mIconForegroundChroma
        set(iconForegroundChroma) {
            mIconForegroundChroma = iconForegroundChroma
            updateIconForegroundColor()
        }
    var iconBackgroundChroma: String?
        get() = mIconBackgroundChroma
        set(iconBackgroundChroma) {
            mIconBackgroundChroma = iconBackgroundChroma
            updateIconBackgroundColor()
        }
    var colorName: String?
        get() = mColorName
        set(colorName) {
            mColorName = colorName
            updateIconBackgroundColor()
            updateIconForegroundColor()
        }

    @get:ColorRes
    val iconForegroundColorResource: Int
        get() = resources.getIdentifier(
            "material_" + mColorName + "_" + mIconForegroundChroma, "color", context.packageName
        )

    @get:ColorRes
    val iconBackgroundColorResource: Int
        get() = resources.getIdentifier(
            "material_" + mColorName + "_" + mIconBackgroundChroma, "color", context.packageName
        )

    @get:ColorInt
    val iconForegroundColor: Int
        get() = ContextCompat.getColor(context, iconForegroundColorResource)

    @get:ColorInt
    val iconBackgroundColor: Int
        get() = ContextCompat.getColor(context, iconBackgroundColorResource)

    private fun updateIconForegroundColor() {
        imageTintList = ColorStateList.valueOf(iconForegroundColor)
    }

    private fun updateIconBackgroundColor() {
        backgroundTintList = ColorStateList.valueOf(iconBackgroundColor)
    }
}