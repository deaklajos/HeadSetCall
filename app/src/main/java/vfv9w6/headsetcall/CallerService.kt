package vfv9w6.headsetcall

import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.widget.Toast
import vfv9w6.headsetcall.model.PressCounter

//TODO this maybe gets closed after some time.
class CallerService : Service() {

    private lateinit var mediaSession: MediaSession
    private val pressCounter = PressCounter(500, Runnable{ callCounted()})

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun callCounted() {
        Toast.makeText(applicationContext, "Calling: " + pressCounter.getPressCount(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        super.onCreate()

        Toast.makeText(applicationContext, "imStartedYall", Toast.LENGTH_SHORT).show()
        val callback = object : MediaSession.Callback() {
            override fun onPlay() = pressCounter.press()
            override fun onPause() = pressCounter.press()
            override fun onStop() = pressCounter.press()
        }

        mediaSession = MediaSession(applicationContext, "MYMS")

        mediaSession.setCallback(callback)
        mediaSession.setPlaybackState(
                PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE)
                        .setState(PlaybackState.STATE_PLAYING,
                                0,
                                1f).build())
        mediaSession.isActive = true
        pressCounter.press()
    }

    override fun onDestroy() {
        mediaSession.release()
        Toast.makeText(applicationContext, "imDeadYall", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}
