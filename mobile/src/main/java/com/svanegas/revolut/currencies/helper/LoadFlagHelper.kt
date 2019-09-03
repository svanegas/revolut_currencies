package com.svanegas.revolut.currencies.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.security.InvalidParameterException

@SuppressLint("DefaultLocale")
private fun getResourceImage(context: Context, imageName: String?) =
    context.resources.getIdentifier(imageName?.toLowerCase(), "drawable", context.packageName)

@BindingAdapter(
    value = ["app:imageResName", "app:imageCircular", "app:roundedCorners", "app:imagePlaceholder"],
    requireAll = false
)
fun loadFlag(
    imageView: ImageView,
    resName: String?,
    isCircular: Boolean? = false,
    roundedCorners: Float? = 0f,
    placeholder: Drawable? = null
) {
    val glideRequest = Glide
        .with(imageView.context)
        .load(getResourceImage(imageView.context, resName))
        .placeholder(placeholder?.let { DrawableCompat.wrap(it) })

    if (roundedCorners != null && roundedCorners > 0 && isCircular != null && isCircular) {
        throw InvalidParameterException("Image can't be rounded and circular at the same time")
    }

    when {
        isCircular != null && isCircular -> {
            glideRequest
                .circleCrop()
                .into(imageView)
        }

        roundedCorners != null && roundedCorners > 0 -> {
            glideRequest
                .transforms(CenterCrop(), RoundedCorners(roundedCorners.toInt()))
                .transforms(RoundedCorners(roundedCorners.toInt()))
                .into(imageView)
        }

        else -> glideRequest.into(imageView)
    }
}