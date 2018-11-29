package vfv9w6.headsetcall

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.orm.SugarRecord
import com.orm.util.NamingHelper
import vfv9w6.headsetcall.data.Contact
import vfv9w6.headsetcall.model.PressCounter
import java.util.*
import com.intentfilter.androidpermissions.PermissionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.Collections.singleton


class CallerService : Service() {

    private var mediaSession: MediaSession? = null
    private val pressCounter = PressCounter(1000, Runnable{ callCounted()})
    private var textToSpeech: TextToSpeech? = null
    private var contact: Contact? = null

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun callContact()
    {
        if(contact == null)
            return

        if(ContextCompat.checkSelfPermission( this, android.Manifest.permission.CALL_PHONE ) != PackageManager.PERMISSION_GRANTED )
        {
            val permissionManager = PermissionManager.getInstance(applicationContext)
            permissionManager.checkPermissions(singleton(Manifest.permission.CALL_PHONE), object : PermissionManager.PermissionRequestListener {
                override fun onPermissionGranted() {}
                override fun onPermissionDenied() {}
            })

            return // Next time we can call
        }
        val intent = Intent(Intent.ACTION_CALL)

        intent.data = Uri.parse("tel:" + contact!!.phoneNumber)
        applicationContext.startActivity(intent)
    }

    private fun callCounted() {
        doAsync {
            val list = SugarRecord.find(Contact::class.java,
                    NamingHelper.toSQLNameDefault("pressCount") + " = ? ",
                    pressCounter.getPressCount().toString())

            uiThread {
                afterFoundInDb(list)
            }
        }
    }

    private fun afterFoundInDb(list: List<Contact>)
    {
        if(list.isNotEmpty())
        {
            contact = list[0]
            textToSpeech?.speak(getString(R.string.speech_calling) + contact!!.name,
                    TextToSpeech.QUEUE_FLUSH, null, "")
        }
        else
        {
            contact = null
            textToSpeech?.speak(getString(R.string.speech_error),
                    TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onCreate() {
        super.onCreate()

        startForeground()

        textToSpeech = TextToSpeech(applicationContext, OnInitListener {status ->
            if (status == TextToSpeech.SUCCESS)
            {
                textToSpeech?.language = Locale.UK
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                    override fun onDone(utteranceId: String?) = callContact()
                    override fun onError(utteranceId: String?) {}
                    override fun onStart(utteranceId: String?) {}
                })
            }

        })

        Toast.makeText(applicationContext, getString(R.string.service_created), Toast.LENGTH_SHORT).show()

        val callback = object : MediaSession.Callback() {
            override fun onPlay() = pressCounter.press()
            override fun onPause() = pressCounter.press()
            override fun onStop() = pressCounter.press()
        }

        mediaSession = MediaSession(applicationContext, "MYMS")

        mediaSession?.apply {
            setCallback(callback)
            setPlaybackState(
                    PlaybackState.Builder().setActions(
                            PlaybackState.ACTION_PLAY or
                                    PlaybackState.ACTION_PAUSE or
                                    PlaybackState.ACTION_PLAY_PAUSE)
                            .setState(PlaybackState.STATE_PLAYING,
                                    0,
                                    1f).build())
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            isActive = true
        }
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
        textToSpeech?.apply {
            stop()
            shutdown()}

        mediaSession?.release()
        Toast.makeText(applicationContext, getString(R.string.service_destroyed), Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}
