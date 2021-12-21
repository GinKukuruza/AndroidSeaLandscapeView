package com.gintasan.android.sealandscapeanimation.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.view.contains
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.gintasan.android.sealandscapeanimation.views.bubbles.BubblesView
import com.gintasan.android.sealandscapeanimation.views.background.main.SeaDrawable
import com.gintasan.android.sealandscapeanimation.views.dolphin.DolphinView
import com.gintasan.android.sealandscapeanimation.views.sea_float.FloatView
import com.gintasan.android.sealandscapeanimation.model.Cords
import java.util.LinkedList
import java.util.Queue

class SeaView(context: Context, private val attrs: AttributeSet) : FrameLayout(context, attrs) {
    private lateinit var seaDrawable: SeaDrawable
    private var bubblesHeight = 10
    private var floatWidth = 120
    private var maxDolphinHeight = 350

    private var layoutHeight = 0
    private var layoutWidth = 0

    private val dolphinView = DolphinView(context, attrs)
    private val floatView = FloatView(context, attrs, floatWidth)
    private val bubbles: BubblesView get() = BubblesView(context, attrs, bubblesHeight)

    private var isFloatDrew = false
    private var floatDirection = false

    private var lastCordsSetTime = System.currentTimeMillis()

    private var lastCordsAccessTime = System.currentTimeMillis()
    private val prevCords: Cords?
        get() {
            return if (System.currentTimeMillis() - lastCordsAccessTime > PREV_FLOAT_CORDS_DELAY) {
                lastCordsAccessTime = System.currentTimeMillis()
                cordsQueue.poll()
            } else null
        }

    private val cordsQueue: Queue<Cords> = LinkedList()

    constructor(
        context: Context,
        attrs: AttributeSet,
        seaColor: Int,
        seaHeight: Float,
        seaPlanesCount: Int,
        sunColor: Int,
        moonColor: Int,
        starsColor: Int,
        starsCount: Int,
        starSize: Float,
        sunSize: Float,
        moonSize: Float,
        maxActionStarSize: Float,
        bubblesHeight: Int,
        floatWidth: Int,
        maxDolphinHeight: Int,
    ) : this(context, attrs) {
        seaDrawable = SeaDrawable(
            resources,
            seaColor,
            seaHeight,
            seaPlanesCount,
            sunColor,
            moonColor,
            starsColor,
            starsCount,
            starSize,
            sunSize,
            moonSize,
            maxActionStarSize
        )
        this.bubblesHeight = bubblesHeight
        this.floatWidth = floatWidth
        this.maxDolphinHeight = maxDolphinHeight
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        seaDrawable = SeaDrawable(resources)
        background = seaDrawable
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layoutHeight = height
        layoutWidth = width

        if (!isFloatDrew) {
            val floatCurrentTop = ((height - seaDrawable.seaHeight) + seaDrawable.seaHeight * 0.2).toInt()
            val floatCurrentStart = width / 4

            floatView.apply {
                val heightRange = (layoutHeight - floatView.floatHeight) - (seaDrawable.seaHeight - floatView.floatHeight * 0.8) // TODO
                val position = floatCurrentTop - seaDrawable.seaHeight
                var percent = position * 100 / heightRange / 100
                if (percent <= 0.7) {
                    percent = 0.7
                }
                floatWidth = (defaultFloatWidth * percent).toInt()
                RelativeLayout.LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let {
                    it.setMargins(floatCurrentStart, floatCurrentTop, 0, 0)
                    this.layoutParams = it
                }
            }

            showFloat()
            floatView.startIdleAnimation()
            isFloatDrew = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val stars = seaDrawable.getStarsItem() ?: return false
        val wave = seaDrawable.getWaveItem()
        val currentEvent = event ?: return false


        when (currentEvent.action) {
            MotionEvent.ACTION_UP -> {
                if (floatView.isFloatTouched) {
                    floatView.isFloatTouched = false
                    floatView.startCords = null
                    cordsQueue.poll()?.let {
                        if (floatDirection) {
                            floatView.startIdleReverseAnimation()
                        } else {
                            floatView.startIdleAnimation()
                        }
                    }
                    cordsQueue.clear()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (floatView.isFloatTouched) {
                    prevCords?.let {
                        val xDiff = (it.x - (floatView.startCords?.x ?: 0f)).toInt()
                        val yDiff = (it.y - (floatView.startCords?.y ?: 0f)).toInt()

                        floatView.apply {
                            val heightRange = (layoutHeight - floatView.floatHeight) - (seaDrawable.seaHeight - floatView.floatHeight * 0.8) // TODO
                            if (currentEvent.y < layoutHeight - floatView.floatHeight * 0.1
                                && currentEvent.y > seaDrawable.seaHeight
                            ) {
                                val position = currentEvent.y - seaDrawable.seaHeight
                                var percent = position * 100 / heightRange / 100
                                if (percent <= 0.7) {
                                    percent = 0.7
                                }
                                floatWidth = (defaultFloatWidth * percent).toInt()
                            }
                        }

                        if (xDiff < (layoutWidth - floatView.width)
                            && xDiff > floatView.width * 0.1f
                            && yDiff < layoutHeight - floatView.height
                            && yDiff > seaDrawable.seaHeight - floatView.floatHeight * 0.8 // TODO
                        ) {
                            checkDirection(it.x, currentEvent.x)
                            startBubbles()
                            floatView.apply {
                                LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                    params.setMargins(xDiff, yDiff, 0, 0)
                                    this.layoutParams = params
                                }
                                checkDirection(it.x, currentEvent.x)
                            }
                        } else {
                            floatView.apply {
                                if (xDiff < floatView.width * 0.1f) {
                                    when {
                                        yDiff < seaDrawable.seaHeight - floatView.floatHeight * 0.5 -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins((floatView.width * 0.1f).toInt(), (seaDrawable.seaHeight - floatView.floatHeight * 0.5).toInt(), 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                        yDiff > layoutHeight - floatView.height -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins((floatView.width * 0.1f).toInt(), layoutHeight - floatView.height, 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                        else -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins((floatView.width * 0.1f).toInt(), yDiff, 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                    }
                                    startBubbles()
                                    return@apply
                                }

                                if (xDiff > layoutWidth - floatView.width) {
                                    when {
                                        yDiff < seaDrawable.seaHeight - floatView.floatHeight * 0.5 -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins((layoutWidth - floatView.width), (seaDrawable.seaHeight - floatView.floatHeight * 0.5).toInt(), 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                        yDiff > layoutHeight - floatView.height -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins((layoutWidth - floatView.width), layoutHeight - floatView.height, 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                        else -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins((layoutWidth - floatView.width), yDiff, 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                    }
                                    startBubbles()
                                    return@apply
                                }

                                if (yDiff > layoutHeight - floatView.height) {
                                    when {
                                        xDiff < floatView.width * 0.1f -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins((floatView.width * 0.1f).toInt(), layoutHeight - floatView.height, 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                        xDiff > layoutWidth - floatView.width -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins(layoutWidth - floatView.width, layoutHeight - floatView.height, 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                        else -> {
                                            LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                                params.setMargins(xDiff, layoutHeight - floatView.height, 0, 0)
                                                this.layoutParams = params
                                            }
                                        }
                                    }
                                    checkDirection(it.x, currentEvent.x)
                                    startBubbles()
                                    return@apply
                                }

                                if (yDiff < seaDrawable.seaHeight - floatView.floatHeight * 0.5) {
                                    LayoutParams(floatView.floatWidth, floatView.floatHeight.toInt()).let { params ->
                                        params.setMargins(xDiff, (seaDrawable.seaHeight - floatView.floatHeight * 0.5).toInt(), 0, 0)
                                        this.layoutParams = params
                                    }
                                    checkDirection(it.x, currentEvent.x)
                                    startBubbles()
                                    return@apply
                                }
                            }
                        }
                        showFloat()
                    }
                    setPrevCords(currentEvent.x, currentEvent.y)
                    return true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if (floatView.isFloatTouched) {
                    return true
                }
                if (currentEvent.y < stars.height) {
                    seaDrawable.startActionStar(currentEvent.x, currentEvent.y)
                    setPrevCords(currentEvent.x, currentEvent.y)
                    return true
                }
                if (currentEvent.y > (layoutHeight - wave.height) * 1.4f) {
                    seaDrawable.startDolphin { isGoes ->
                        if (isGoes) {
                            var percent = ((currentEvent.y - seaDrawable.seaHeight) * 100) / (layoutHeight - seaDrawable.seaHeight) / 100
                            if (percent < 0.5) {
                                percent = 0.5f
                            }
                            val newHeight = (percent * maxDolphinHeight).toInt()
                            val newWidth = (newHeight * 1.6f).toInt()
                            val topP = layoutHeight - newHeight - (layoutHeight - currentEvent.y)
                            val startP = currentEvent.x.toInt() - newWidth * 0.5f
                            dolphinView.apply {
                                RelativeLayout.LayoutParams(newWidth, newHeight).let {
                                    it.setMargins(startP.toInt(), topP.toInt(), 0, 0)
                                    layoutParams = it
                                }
                            }
                            showDolphin()
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun checkDirection(cordX: Float, currentX: Float) {
        val currentFloatDirection = if (cordX <= currentX) {
            floatView.startPullRightAnimation()
            true
        } else {
            floatView.startPullLeftAnimation()
            false
        }

        if (currentFloatDirection != floatDirection) {
            cordsQueue.clear()
        }

        floatDirection = currentFloatDirection
    }

    private fun startBubbles() {
        bubbles.let { newBubbles ->
            if (floatDirection) {
                newBubbles.apply {
                    LayoutParams(bubblesWidth.toInt(), bubblesHeight).let { params ->
                        params.setMargins((floatView.marginLeft + floatView.floatWidth * 0.2).toInt(), (floatView.marginTop + floatView.floatHeight.toInt() * 0.95f).toInt(), 0, 0)
                        this.layoutParams = params
                    }
                    onBubblesListener = {
                        removeBubbles()
                    }
                    showBubbles()
                }
            } else {
                newBubbles.apply {
                    LayoutParams(bubblesWidth.toInt(), bubblesHeight).let { params ->
                        params.setMargins((floatView.marginLeft + floatView.floatWidth * 0.8).toInt(), (floatView.marginTop + floatView.floatHeight.toInt() * 0.95f).toInt(), 0, 0)
                        this.layoutParams = params
                    }
                    onBubblesListener = {
                        removeBubbles()
                    }
                    showBubbles()
                }
            }
        }
    }

    private fun setPrevCords(x: Float, y: Float) {
        if (System.currentTimeMillis() - lastCordsSetTime > SET_CORDS_DELAY) {
            cordsQueue.offer(Cords(x, y))
            lastCordsSetTime = System.currentTimeMillis()
        }
    }

    private fun showDolphin() {
        if (contains(dolphinView)) {
            removeView(dolphinView)
        }
        addView(dolphinView)
    }

    private fun BubblesView.showBubbles() {
        addView(this)
    }

    private fun BubblesView.removeBubbles() {
        if (contains(this)) {
            removeView(this)
        }
    }

    private fun showFloat() {
        if (contains(floatView)) {
            removeView(floatView)
        }
        addView(floatView)
    }

    private companion object {
        private const val PREV_FLOAT_CORDS_DELAY = 40L // in ms
        private const val SET_CORDS_DELAY = 20L // in ms
    }
}