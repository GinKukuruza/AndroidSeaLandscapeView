package com.gintasan.android.sealandscapeanimation.views.background

import android.graphics.Canvas
import android.graphics.Paint
import com.gintasan.android.sealandscapeanimation.model.Item
import com.gintasan.android.sealandscapeanimation.model.StarWrapper
import java.util.*
import kotlin.collections.ArrayList

class Stars(
    private val starsTime: Long,
    private val starCount: Int,
    private var starSize: Float,
    private var starColor: Int,
    private val random: Random,
    paint: Paint,
) : Item(paint) {
    private var stars = ArrayList<StarWrapper>()

    override fun onSizeChanged() {
        stars.clear()
        for (i in 0 until starCount) {
            val z = random.nextFloat()
            stars.add(StarWrapper(random.nextFloat() * width, random.nextFloat() * height, z))
        }
    }

    override fun onDraw(currentTime: Long, canvas: Canvas) {
        paint.color = starColor
        val percent = if (currentTime > starsTime * 0.5f) {
            1 - (currentTime * 100 / starsTime) / 100f
        } else {
            (currentTime * 100 / starsTime) / 100f
        }
        for (s in stars) {
            paint.alpha = (255f * percent).toInt()
            canvas.drawCircle(s.x, s.y, s.z * starSize, paint)
        }
    }
}