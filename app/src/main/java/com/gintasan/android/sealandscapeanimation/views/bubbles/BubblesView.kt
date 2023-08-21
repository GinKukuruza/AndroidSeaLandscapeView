package com.gintasan.android.sealandscapeanimation.views.bubbles

import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.gintasan.android.sealandscapeanimation.R

class BubblesView(
    context: Context
) : AppCompatImageView(context) {
    private val bubblesDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bubbles_anim, null)

    var bubblesHeight = 10
    val bubblesWidth: Double
        get() {
            return bubblesHeight * 1.31
        }


    lateinit var onBubblesListener: () -> Unit

    constructor(
        context: Context,
        bubblesHeight: Int,
    ) : this(context) {
        this.bubblesHeight = bubblesHeight
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        background = bubblesDrawable
        (bubblesDrawable as AnimatedVectorDrawable?)?.apply {
            registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    super.onAnimationEnd(drawable)
                    onBubblesListener()
                }
            })
        }?.start()
    }
}