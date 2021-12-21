package com.gintasan.android.sealandscapeanimation.views.background

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.gintasan.android.sealandscapeanimation.model.Item
import com.gintasan.android.sealandscapeanimation.model.SunWrapper

class Sun(
    paint: Paint,
    private val sunTime: Long,
    private val sunSize: Float,
) : Item(paint) {
    private var sun: SunWrapper? = null

    private var _color = 0
    var color: Int
        get() = _color
        set(value) {
            _color = value
            sun = null
        }

    override fun onSizeChanged() {
        sun = null
    }

    override fun onDraw(currentTime: Long, canvas: Canvas) {
        init(currentTime)

        sun?.let {
            paint.shader = sun?.shader
            canvas.drawCircle(it.x, it.y, it.size, paint)
        }
    }

    private fun init(currentTime: Long) {
        val x = ((width / 85f) * (currentTime * 100 / sunTime.toDouble())).toLong()
        val halfWidth = width * 0.5f
        val cord: Float
        val y = if (x >= halfWidth) {
            cord = -((-(halfWidth - x) * 100) / halfWidth)
            (cord * cord / sunSize * 0.5f)
        } else {
            cord = (((halfWidth - x) * 100) / halfWidth)
            (cord * cord / sunSize * 0.5f)
        }
        val shader = RadialGradient(x + cord * 0.1f, y - sunSize * 0.1f, sunSize, intArrayOf(-0x1, color), floatArrayOf(0.95f, 1f), Shader.TileMode.CLAMP)
        sun = SunWrapper(x.toFloat(), y, sunSize, shader)
    }
}