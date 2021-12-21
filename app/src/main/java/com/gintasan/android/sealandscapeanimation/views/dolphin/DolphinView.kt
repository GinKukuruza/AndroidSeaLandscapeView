package com.gintasan.android.sealandscapeanimation.views.dolphin

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.gintasan.android.sealandscapeanimation.R

class DolphinView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    private val dolphinDrawable = ResourcesCompat.getDrawable(resources, R.drawable.dolphin_anim, null)

    // TODO add dolphin size support
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        background = dolphinDrawable
        (dolphinDrawable as AnimatedVectorDrawable?)?.start()
    }
}