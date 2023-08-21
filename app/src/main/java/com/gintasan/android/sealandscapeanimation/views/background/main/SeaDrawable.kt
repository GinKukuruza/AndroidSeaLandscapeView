package com.gintasan.android.sealandscapeanimation.views.background.main

import android.animation.ArgbEvaluator
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.gintasan.android.sealandscapeanimation.model.Item
import com.gintasan.android.sealandscapeanimation.model.RGB
import com.gintasan.android.sealandscapeanimation.utils.ColorChanger
import com.gintasan.android.sealandscapeanimation.views.background.ActionStar
import com.gintasan.android.sealandscapeanimation.views.background.Moon
import com.gintasan.android.sealandscapeanimation.views.background.Sea
import com.gintasan.android.sealandscapeanimation.views.background.Stars
import com.gintasan.android.sealandscapeanimation.views.background.Sun
import com.gintasan.android.sealandscapeanimation.R
import java.util.Random
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SeaDrawable(
    private val resources: Resources,
    private val seaColor: Int = resources.getColor(R.color.seaColor),
    val seaHeight: Float = resources.getDimension(R.dimen.seaHeight),
    private val seaPlanesCount: Int = 8,
    private val sunColor: Int = ResourcesCompat.getColor(resources, R.color.sunColor, null),
    private val moonColor: Int = ResourcesCompat.getColor(resources, R.color.moonColor, null),
    private val starsColor: Int = ResourcesCompat.getColor(resources, R.color.starColor, null),
    private val starsCount: Int = 30,
    private val starSize: Float = 5f,
    private val sunSize: Float = 45f,
    private val moonSize: Float = 30f,
    private val maxActionStarSize: Float = 50f,
) : Drawable() {

    /**
     * Sea elements
     */
    private lateinit var sun: Sun
    private lateinit var moon: Moon
    private lateinit var stars: Stars
    private var actionStar: ActionStar? = null

    private var dayTimer: Job? = null
    private var actionStarTimer: Job? = null
    private var dolphinTimer: Job? = null
    private var waveTimer: Job? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random()
    private val argbEvaluator = ArgbEvaluator()
    private var waves = ArrayList<Sea>()

    /**
     * Dynamic generation of the colors of the main screen
     * This should be dynamically because the color of each element below
     * depends of certain time of day/night lifecycle
     */
    private val fogDayColor: Int get() = fogDayColorChanger.getColor(currentTime)
    private val fogNightColor: Int get() = fogNightColorChanger.getColor(currentTime - SUN_TIME)
    private val skyDayColor: Int get() = skyDayColorChanger.getColor(currentTime)
    private val skyNightColor: Int get() = skyNightColorChanger.getColor(currentTime - SUN_TIME)

    private fun init() {
        initWaves()
        initMoon()
        initSun()
        initStars()
    }

    private fun initWaves() {
        waves.clear()
        for (i in 0 until seaPlanesCount) {
            val height = seaHeight * (i + 1) / (seaPlanesCount)
            val fluctuation = height / 2

            val fogMainColor = argbEvaluator.evaluate(i.toFloat() / seaPlanesCount, seaColor, fogDayColor) as Int
            val fogSecondColor = argbEvaluator.evaluate((i + 1).toFloat() / seaPlanesCount, seaColor, fogDayColor) as Int
            Sea(paint, fogSecondColor, fogMainColor, fluctuation, random).apply {
                y = bounds.height() - height
                setSize(bounds.width().toFloat(), height)
            }.let { wave ->
                waves.add(0, wave)
            }
        }
    }

    private fun initMoon() {
        moon = Moon(paint, MOON_TIME, moonSize, moonColor).apply {
            y = bounds.height() - seaHeight * 1.6f
            setSize(bounds.width().toFloat(), seaHeight)
        }
    }

    private fun initSun() {
        sun = Sun(paint, SUN_TIME, sunSize).apply {
            this.color = sunColor
        }.apply {
            y = bounds.height() - seaHeight * 1.6f
            setSize(bounds.width().toFloat(), seaHeight)
        }
    }

    private fun initStars() {
        stars = Stars(MOON_TIME, starsCount, starSize, starsColor, random, paint).apply {
            setSize(bounds.width().toFloat(), seaHeight)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        init()
    }

    override fun draw(canvas: Canvas) {
        if (dayTimer == null) dayTimer = startDayTimer()
        if (waveTimer == null) waveTimer = startWavesTimer()

        drawBackground(canvas)
        drawWaves(canvas)
        drawStars(canvas)
        drawActionStar(canvas)
        drawSunOrMoon(canvas)

        if (isVisible) {
            Thread.sleep(sleeperTime)
            invalidateSelf()
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity() = PixelFormat.OPAQUE

    fun getStarsItem(): Item {
        return stars
    }

    fun getWaveItem(): Item {
        return waves.first()
    }

    fun startActionStar(x: Float, y: Float) {
        if (currentTime > SUN_TIME && this.actionStar == null) {
            actionStar = ActionStar(maxActionStarSize, ACTION_STAR_TIME, starsColor, x, y, Paint())
            actionStarTimer?.cancel()
            actionStarTimer = startActionStarTimer()
        }
    }

    fun startDolphin(onDolphinGoes: (isGoes: Boolean) -> Unit) {
        if (dolphinTimer == null) {
            goDolphin(onDolphinGoes)
            return
        }
        if (!dolphinTimer!!.isActive) {
            goDolphin(onDolphinGoes)
            return
        }
    }

    private fun goDolphin(onDolphinGoes: (isGoes: Boolean) -> Unit) {
        dolphinTimer?.cancel()
        onDolphinGoes(true)
        dolphinTimer = startDolphinTimer(onDolphinGoes)
    }

    private fun drawActionStar(canvas: Canvas) {
        actionStar?.draw(currentActionStarTime, canvas)
    }

    private fun drawStars(canvas: Canvas) {
        if (currentTime > SUN_TIME) {
            stars.draw(currentTime - SUN_TIME, canvas)
        }
    }

    private fun drawWaves(canvas: Canvas) {
        for (i in 0 until waves.size) {
            if (currentTime > SUN_TIME) {
                waves[i].apply {
                    this.fogSecondColor = argbEvaluator.evaluate((waves.size - 1 - i).toFloat() / seaPlanesCount, seaColor, fogNightColor) as Int
                    this.fogMainColor = argbEvaluator.evaluate((waves.size - i).toFloat() / seaPlanesCount, seaColor, fogNightColor) as Int
                }
            } else {
                waves[i].apply {
                    this.fogSecondColor = argbEvaluator.evaluate((waves.size - 1 - i).toFloat() / seaPlanesCount, seaColor, fogDayColor) as Int
                    this.fogMainColor = argbEvaluator.evaluate((waves.size - i).toFloat() / seaPlanesCount, seaColor, fogDayColor) as Int
                }
            }
            waves[i].draw(currentTime, canvas)
        }
    }

    private fun drawSunOrMoon(canvas: Canvas) {
        if (currentTime > SUN_TIME) {
            moon.draw((currentTime - SUN_TIME), canvas)
        } else {
            sun.draw(currentTime, canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        if (currentTime > SUN_TIME) {
            paint.shader = LinearGradient(0f, bounds.height() - waves[0].height - waves[0].fluctuation, 0f, 0f, fogNightColor, skyNightColor, Shader.TileMode.CLAMP)
            canvas.drawPaint(paint)
            paint.shader = null
        } else {
            paint.shader = LinearGradient(0f, bounds.height() - waves[0].height - waves[0].fluctuation, 0f, 0f, fogDayColor, skyDayColor, Shader.TileMode.CLAMP)
            canvas.drawPaint(paint)
            paint.shader = null
        }
    }

    private fun startDayTimer(): Job {
        return GlobalScope.launch {
            while (true) {
                delay(DAY_TIME_DELAY)
                currentTime += DAY_TIME_DELAY
                if (currentTime > DAY_TIME) {
                    currentTime = 0
                }
            }
        }
    }

    private fun startActionStarTimer(): Job {
        return GlobalScope.launch {
            while (true) {
                delay(ACTION_STAR_DELAY)
                currentActionStarTime += ACTION_STAR_DELAY
                if (currentActionStarTime > ACTION_STAR_TIME) {
                    currentActionStarTime = 0
                    actionStar = null
                    actionStarTimer?.cancel()
                    actionStarTimer = null
                }
            }
        }
    }

    private fun startDolphinTimer(onDolphinGoes: (isGoes: Boolean) -> Unit): Job {
        return GlobalScope.launch {
            while (true) {
                delay(ACTION_DOLPHIN_DELAY)
                currentDolphinTime += ACTION_DOLPHIN_DELAY
                if (currentDolphinTime > ACTION_DOLPHIN_TIME) {
                    currentDolphinTime = 0
                    onDolphinGoes(false)
                    dolphinTimer?.cancel()
                    dolphinTimer = null
                }
            }
        }
    }

    private fun startWavesTimer(): Job {
        return GlobalScope.launch {
            while (true) {
                delay(WAVE_DELAY)
                currentWaveTime += WAVE_DELAY
                if (currentWaveTime > WAVE_TIME) {
                    currentWaveTime = 0
                    sleeperTime = DEF_SLEEPER
                }
                if (currentWaveTime < WAVE_TIME && currentWaveTime >= WAVE_TIME - WAVE_WIND_TIME) {
                    sleeperTime = WAVE_SLEEPER_TIME
                }
            }
        }
    }

    private companion object {
        private const val DAY_TIME = 20000L // in ms
        private const val SUN_TIME = 10000L // in ms
        private const val MOON_TIME = 10000L // in ms
        private const val DAY_TIME_DELAY = 10L // in ms

        private const val ACTION_STAR_TIME = 500L // in ms
        private const val ACTION_STAR_DELAY = 10L // in ms

        private const val WAVE_TIME = 4000L // in ms
        private const val WAVE_WIND_TIME = 1500L // in ms
        private const val WAVE_DELAY = 10L // in ms

        private const val DEF_SLEEPER = 70L // in ms
        private const val WAVE_SLEEPER_TIME = 40L

        private var sleeperTime = DEF_SLEEPER

        private const val ACTION_DOLPHIN_TIME = 1500L // in ms
        private const val ACTION_DOLPHIN_DELAY = 10L // in ms

        private var currentTime = 0L
        private var currentActionStarTime = 0L
        private var currentDolphinTime = 0L
        private var currentWaveTime = 0L

        private val fogDayColorChanger = ColorChanger(
            SUN_TIME, listOf(
                RGB(168, 168, 169),
                RGB(198, 189, 196),
                RGB(227, 209, 197),
                RGB(227, 209, 197),
                RGB(227, 209, 197),
                RGB(198, 189, 196),
                RGB(168, 168, 169)
            )
        )

        private val skyDayColorChanger = ColorChanger(
            SUN_TIME, listOf(
                RGB(168, 168, 169),
                RGB(198, 189, 196),
                RGB(231, 212, 199),
                RGB(231, 212, 199),
                RGB(231, 212, 199),
                RGB(198, 189, 196),
                RGB(168, 168, 169)
            )
        )

        private val fogNightColorChanger = ColorChanger(
            MOON_TIME, listOf(
                RGB(168, 168, 169),
                RGB(139, 148, 196),
                RGB(129, 142, 195),
                RGB(139, 148, 196),
                RGB(168, 168, 169)
            )
        )

        private val skyNightColorChanger = ColorChanger(
            MOON_TIME, listOf(
                RGB(168, 168, 169),
                RGB(125, 139, 193),
                RGB(120, 136, 190),
                RGB(125, 139, 193),
                RGB(168, 168, 169)
            )
        )
    }
}