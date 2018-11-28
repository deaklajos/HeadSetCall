package vfv9w6.headsetcall.model

import java.util.*
import kotlin.concurrent.schedule

class PressCounter(private val interval: Long,
                   private val doAfterInterval: Runnable)
{
    private val timer = Timer()
    private var lastTime: Long = 0
    private var pressCount: Int = 0
    private var lastTimerTask: TimerTask? = null

    fun getPressCount() = pressCount

    fun press()
    {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTime < interval)
            pressCount++
        else
            pressCount = 1

        //TODO this not works!!!
        timer.cancel()
        timer.schedule(interval){ doAfterInterval.run() }
        lastTime = currentTime
    }
}