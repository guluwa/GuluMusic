package cn.guluwa.gulumusic.utils

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import okhttp3.internal.Util

/**
 * Created by guluwa on 2018/1/12.
 */


@BindingAdapter("android:src")
fun ImageView.setSrc(bitmap: Bitmap) {
    this.setImageBitmap(bitmap)
}

@BindingAdapter("android:src")
fun ImageView.setSrc(resId: Int) {
    this.setImageResource(resId)
}

@BindingAdapter("circleImageUrl")
fun ImageView.loadCircleImage(url: String?) {
    Glide.with(context)
            .load(url)
            .apply(RequestOptions().circleCrop())
            .into(this)
}

@BindingAdapter("imageUrl")
fun ImageView.loadImage(url: String?) {
    Glide.with(context)
            .load(url)
            .apply(RequestOptions().centerCrop())
            .into(this)
}

@BindingAdapter("bigImageUrl")
fun ImageView.loadBigImage(url: String?) {
    Glide.with(context)
            .load(url)
            .into(this)
}
