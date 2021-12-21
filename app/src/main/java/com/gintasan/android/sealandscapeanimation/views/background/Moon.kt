package com.gintasan.android.sealandscapeanimation.views.background

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.gintasan.android.sealandscapeanimation.model.Item
import com.gintasan.android.sealandscapeanimation.model.MoonWrapper

class Moon(
    paint: Paint,
    private val moonTime: Long,
    private val size: Float,
    private val color: Int,
) : Item(paint) {
    private var moon: MoonWrapper? = null

    override fun onDraw(currentTime: Long, canvas: Canvas) {
        init(currentTime)

        moon?.let {
            paint.shader = moon?.shader
            paint.alpha = 255
            canvas.drawCircle(it.x, it.y, it.size, paint)
        }
    }

    private fun init(currentTime: Long) {
        val x = ((width / 100f) * (currentTime * 100 / moonTime.toDouble())).toLong()
        val halfWidth = width * 0.5f
        val cord: Float
        val y = if (x >= halfWidth) {
            cord = -((-(halfWidth - x) * 100) / halfWidth)
            (cord * cord / size * 0.5f)
        } else {
            cord = (((halfWidth - x) * 100) / halfWidth)
            (cord * cord / size * 0.5f)
        }
        val shader = RadialGradient(x + cord * 0.1f, y - size * 0.1f, size, intArrayOf(color, -0x1), floatArrayOf(0.95f, 1f), Shader.TileMode.CLAMP)
        moon = MoonWrapper(x.toFloat(), y, size, shader)
    }
}