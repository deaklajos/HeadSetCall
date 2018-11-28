package vfv9w6.headsetcall

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.orm.SugarRecord
import com.orm.util.NamingHelper
import vfv9w6.headsetcall.data.Contact
import vfv9w6.headsetcall.model.PressCounter
import java.util.*

class CallerService : Service() {

    private lateinit var mediaSession: MediaSession
    private val pressCounter = PressCounter(1000, Runnable{ callCounted()})
    private var textToSpeech: TextToSpeech? = null

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun callCounted() {

        val list = SugarRecord.find(Contact::class.java,
                NamingHelper.toSQLNameDefault("pressCount") + " = ? ",
                pressCounter.getPressCount().toString())
        if(list.size > 0)
        {
            val contact = list[0]
            textToSpeech?.speak(getString(R.string.speech_calling) + contact.name,
                    TextToSpeech.QUEUE_FLUSH, null, "")
        }
        else
            textToSpeech?.speak(getString(R.string.speech_error),
                    TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onCreate() {
        super.onCreate()

        startForeground()

        textToSpeech = TextToSpeech(applicationContext, OnInitListener {status ->
            if (status != TextToSpeech.ERROR)
                textToSpeech?.language = Locale.UK
        })

        Toast.makeText(applicationContext, getString(R.string.service_created), Toast.LENGTH_SHORT).show()

        val callback = object : MediaSession.Callback() {
            override fun onPlay() = pressCounter.press()
            override fun onPause() = pressCounter.press()
            override fun onStop() = pressCounter.press()
        }

        mediaSession = MediaSession(applicationContext, "MYMS")

        mediaSession.setCallback(callback)
        mediaSession.setPlaybackState(
                PlaybackState.Builder().setActions(
                        PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE)
                        .setState(PlaybackState.STATE_PLAYING,
                                0,
                                1f).build())

        //Although deprecated my phone still needs it.
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.isActive = true
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
                .setContentText(getString(R.string.notification_text))
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

        textToSpeech?.stop()
        textToSpeech?.shutdown()

        mediaSession.release()
        Toast.makeText(applicationContext, getString(R.string.service_destroyed), Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}
