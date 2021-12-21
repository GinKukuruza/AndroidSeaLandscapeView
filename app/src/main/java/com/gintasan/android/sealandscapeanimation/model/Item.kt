package com.gintasan.android.sealandscapeanimation.model

import android.graphics.Canvas
import android.graphics.Paint

abstract class Item(val paint: Paint) {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
        onSizeChanged()
    }

    open fun onSizeChanged() {}

    fun draw(currentTime: Long, canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)
        onDraw(currentTime, canvas)
        canvas.restore()
    }

    abstract fun onDraw(currentTime: Long, canvas: Canvas)
}