package com.gintasan.android.sealandscapeanimation.views.background

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.gintasan.android.sealandscapeanimation.model.Item

class ActionStar(
    private val maxStarSize: Float,
    private val starTime: Long,
    private val starColor: Int,
    private val starX: Float,
    private val starY: Float,
    paint: Paint,
) : Item(paint) {
    private val path = Path()

    private var starSize = 0f

    override fun onDraw(currentTime: Long, canvas: Canvas) {
        init(currentTime)

        canvas.drawPath(path, paint)
    }

    private fun init(currentTime: Long) {
        paint.color = starColor

        val percent = currentTime * 100 / starTime / 100f
        starSize = if (percent > 0.5f) {
            (1 - percent) * maxStarSize * 2
        } else {
            percent * maxStarSize * 2
        }

        path.reset()
        path.moveTo(starX + starSize, starY)
        path.lineTo(starX + starSize, starY)

        var x = starX
        var y = starY - starSize
        path.cubicTo(starX, starY, starX, starY, x, y)

        x = starX - starSize
        y = starY
        path.cubicTo(starX, starY, starX, starY, x, y)

        x = starX
        y = starY + starSize
        path.cubicTo(starX, starY, starX, starY, x, y)

        x = starX + starSize
        y = starY
        path.cubicTo(starX, starY, starX, starY, x, y)
    }
}