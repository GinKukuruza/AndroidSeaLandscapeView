package com.gintasan.android.sealandscapeanimation.views.sea_float

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.gintasan.android.sealandscapeanimation.model.Cords
import com.gintasan.android.sealandscapeanimation.R

sealed class Callback {
    class Idle : Callback()
    class IdleReverse : Callback()
    class PullLeft : Callback()
    class PullRight : Callback()
}

class FloatView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private val floatIdleDrawable = ResourcesCompat.getDrawable(resources, R.drawable.float_anim_def, null)
    private val floatIdleReverseDrawable = ResourcesCompat.getDrawable(resources, R.drawable.float_anim_def_reverse, null)
    private val floatPullRightDrawable = ResourcesCompat.getDrawable(resources, R.drawable.float_anim_def_r, null)
    private val floatPullLeftDrawable = ResourcesCompat.getDrawable(resources, R.drawable.float_anim_def_l, null)

    private var currentCallback: Callback? = null

    var defaultFloatWidth = 120

    var floatWidth = 120
    val floatHeight: Float
        get() {
            return floatWidth * 1.6f
        }

    var isFloatTouched = false
    var startCords: Cords? = null

    constructor(
        context: Context,
        attrs: AttributeSet,
        floatWidth: Int,
    ) : this(context, attrs) {
        this.defaultFloatWidth = floatWidth
        this.floatWidth = floatWidth
    }

    fun startIdleAnimation() {
        if (currentCallback !is Callback.Idle) {
            clearCallbacks()
            (floatIdleDrawable as AnimatedVectorDrawable?)?.apply {
                val callback = object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        start()
                    }
                }
                registerAnimationCallback(callback)
                currentCallback = Callback.Idle()
                background = this
                start()
            }
        }
    }

    fun startIdleReverseAnimation() {
        if (currentCallback !is Callback.IdleReverse) {
            clearCallbacks()
            (floatIdleReverseDrawable as AnimatedVectorDrawable?)?.apply {
                val callback = object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        start()
                    }
                }
                registerAnimationCallback(callback)
                currentCallback = Callback.IdleReverse()
                background = this
                start()
            }
        }
    }

    fun startPullRightAnimation() {
        if (currentCallback !is Callback.PullRight) {
            clearCallbacks()
            (floatPullRightDrawable as AnimatedVectorDrawable?)?.apply {
                val callback = object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        start()
                    }
                }
                registerAnimationCallback(callback)
                currentCallback = Callback.PullRight()
                background = this
                start()
            }
        }
    }

    fun startPullLeftAnimation() {
        if (currentCallback !is Callback.PullLeft) {
            clearCallbacks()
            (floatPullLeftDrawable as AnimatedVectorDrawable?)?.apply {
                val callback = object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        start()
                    }
                }
                registerAnimationCallback(callback)
                currentCallback = Callback.PullLeft()
                background = this
                start()
            }
        }
    }

    private fun clearCallbacks() {
        currentCallback = null
        background = null
        (floatPullRightDrawable as AnimatedVectorDrawable?)?.clearAnimationCallbacks()
        (floatIdleReverseDrawable as AnimatedVectorDrawable?)?.clearAnimationCallbacks()
        (floatPullLeftDrawable as AnimatedVectorDrawable?)?.clearAnimationCallbacks()
        (floatIdleDrawable as AnimatedVectorDrawable?)?.clearAnimationCallbacks()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isFloatTouched = true
                startCords = Cords(event.x, event.y)
            }
        }
        return super.onTouchEvent(event)
    }
}