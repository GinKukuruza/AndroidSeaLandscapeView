package com.gintasan.android.sealandscapeanimation.views.background

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.gintasan.android.sealandscapeanimation.model.Item
import com.gintasan.android.sealandscapeanimation.model.SeaWaveStats
import com.google.android.material.math.MathUtils
import java.util.Random

class Sea(
    paint: Paint,
    fogSecondColor: Int,
    fogMainColor: Int,
    _fluctuation: Float,
    private val random: Random,
) : Item(paint) {
    private var shader: LinearGradient? = null
    private val path = Path()
    private var seaWaveScales: HashMap<Int, SeaWaveStats>? = null

    var fogMainColor: Int = fogMainColor
        set(value) {
            field = value
            shader = null
        }

    var fogSecondColor: Int = fogSecondColor
        set(value) {
            field = value
            shader = null
        }

    var fluctuation: Float = _fluctuation
        set(value) {
            field = value
            path.reset()
            shader = null
        }

    override fun onDraw(currentTime: Long, canvas: Canvas) {
        init()

        paint.color = fogMainColor
        paint.shader = shader
        canvas.drawPath(path, paint)
        paint.shader = null
    }

    override fun onSizeChanged() {
        path.reset()
        shader = null
    }

    private fun init() {
        if (seaWaveScales == null) {
            seaWaveScales = HashMap()
            path.moveTo(0f, height)
            path.lineTo(width, height)
            var prevX: Float = width
            var prevY = (Math.random() * fluctuation).toFloat()
            path.lineTo(prevX, prevY)

            val segments = (width / 20).toInt()
            for (i in 0..segments) {
                val x: Float = (width * (segments - i) / segments)
                val y = (Math.random() * fluctuation * 0.5f).toFloat()
                val x33 = MathUtils.lerp(prevX, x, 0.33f)
                val x67 = MathUtils.lerp(prevX, x, 0.67f)
                path.cubicTo(x33, prevY, x67, y, x, y)
                prevX = x
                prevY = y
                seaWaveScales?.put(i, SeaWaveStats(y, 0, random.nextBoolean()))
            }
            path.close()
        } else {
            path.reset()
            path.moveTo(0f, height)
            path.lineTo(width, height)
            var prevX = width
            var prevY = (Math.random() * fluctuation).toFloat()
            path.lineTo(prevX, prevY)

            val segments = (width / 120).toInt()
            for (i in 0..segments) {
                var currentDirection = seaWaveScales?.get(i)?.direction ?: true
                val inc = if (currentDirection) {
                    1
                } else {
                    -1
                }
                val x: Float = (width * (segments - i) / segments)
                val supportY = (Math.random() * fluctuation).toFloat()
                val prevInc = seaWaveScales?.get(i)?.inc?.toFloat() ?: 0f
                val newY = seaWaveScales?.get(i)?.y ?: supportY
                val y = newY + prevInc
                val x33 = MathUtils.lerp(prevX, x, 0.33f)
                val x67 = MathUtils.lerp(prevX, x, 0.67f)
                path.cubicTo(x33, prevY, x67, y, x, y)
                prevX = x
                prevY = y
                val totalInc = (prevInc + inc)
                val newInc = when {
                    (totalInc).toInt() >= MAX_WAVE_HEIGHT -> {
                        currentDirection = false
                        prevInc.toInt()
                    }
                    (totalInc).toInt() <= MIN_WAVE_HEIGHT -> {
                        currentDirection = true
                        prevInc.toInt()
                    }
                    else -> {
                        totalInc.toInt()
                    }
                }
                seaWaveScales?.put(i, SeaWaveStats(newY, newInc, currentDirection))
            }
            path.close()
        }

        if (shader == null)
            shader = LinearGradient(0f, height, 0f, 0f, fogSecondColor, fogMainColor, Shader.TileMode.CLAMP)
    }

    private companion object {
        private const val MAX_WAVE_HEIGHT = 10
        private const val MIN_WAVE_HEIGHT = -10
    }
}