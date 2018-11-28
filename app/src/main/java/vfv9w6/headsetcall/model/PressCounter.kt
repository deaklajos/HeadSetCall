package vfv9w6.headsetcall.model

import android.os.Handler
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask

class PressCounter(private val interval: Long,
                   private val doAfterInterval: Runnable)
{
    private val handler = Handler()
    private var lastTime: Long = 0
    private var pressCount: Int = 0

    fun getPressCount() = pressCount

    fun press()
    {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTime < interval)
            pressCount++
        else
            pressCount = 1

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(doAfterInterval, interval)

        lastTime = currentTime
    }
}