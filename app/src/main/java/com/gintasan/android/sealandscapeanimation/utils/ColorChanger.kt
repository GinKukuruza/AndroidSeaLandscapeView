package com.gintasan.android.sealandscapeanimation.utils

import android.graphics.Color
import com.gintasan.android.sealandscapeanimation.model.RGB
import kotlin.math.floor

class ColorChanger(
    private val time: Long,
    private val rgb: List<RGB>,
) {
    private val timePerColor = time / rgb.size

    private var prevColor: Int = Color.rgb(rgb[0].r, rgb[0].g, rgb[0].b)

    fun getColor(currentTime: Long): Int {
        if (currentTime > time) return prevColor
        val currentTimePoint = floor(currentTime.div(timePerColor).toDouble()).toInt()
        if (currentTimePoint >= rgb.size - 1) return prevColor
        val prevColor = rgb[currentTimePoint]
        val nextColor = rgb[currentTimePoint + 1]
        return manageColors(nextColor, prevColor, currentTime)
    }

    private fun manageColors(nextColor: RGB, prevColor: RGB, currentTime: Long): Int {
        val timePercent = (currentTime % timePerColor) * 100 / timePerColor
        val newR = if (prevColor.r > nextColor.r) {
            val range = prevColor.r - nextColor.r
            val inc = timePercent * range / 100
            prevColor.r - inc
        } else {
            val range = nextColor.r - prevColor.r
            val inc = timePercent * range / 100
            prevColor.r + inc
        }
        val newG = if (prevColor.g > nextColor.g) {
            val range = prevColor.g - nextColor.g
            val inc = timePercent * range / 100
            prevColor.g - inc
        } else {
            val range = nextColor.g - prevColor.g
            val inc = timePercent * range / 100
            prevColor.g + inc
        }
        val newB = if (prevColor.b > nextColor.b) {
            val range = prevColor.b - nextColor.b
            val inc = timePercent * range / 100
            prevColor.b - inc
        } else {
            val range = nextColor.b - prevColor.b
            val inc = timePercent * range / 100
            prevColor.b + inc
        }
        this.prevColor = Color.rgb(newR.toInt(), newG.toInt(), newB.toInt())
        return this.prevColor
    }
}