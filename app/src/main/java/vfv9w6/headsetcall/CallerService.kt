package vfv9w6.headsetcall

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.widget.Toast
import vfv9w6.headsetcall.model.PressCounter
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat


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

        startForeground()

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


        //pressCounter.press()
    }

    private fun startForeground() {
        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("my_service", "My Background Service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("HeadSetCall")
                .setContentText("Ready for directions...")
                .setContentIntent(pendingIntent).build()
        startForeground(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        mediaSession.release()
        Toast.makeText(applicationContext, "imDeadYall", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}
