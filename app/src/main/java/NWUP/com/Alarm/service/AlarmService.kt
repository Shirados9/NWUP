package NWUP.com.Alarm.service

import NWUP.com.Alarm.application.AlarmApplication.Companion.CHANNEL_ID
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.TITLE
import NWUP.com.Alarm.activities.AlarmRingActivity
import NWUP.com.R
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Vibrator
import androidx.core.app.NotificationCompat


class AlarmService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer.isLooping = true

        vibrator = (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, AlarmRingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)


        val bundle = intent!!.extras
        val alarmTitle = String.format("%s Alarm", bundle!!.getString(TITLE))

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(alarmTitle)
                .setContentText("Ring Ring .. Ring Ring")
                .setSmallIcon(R.drawable.ic_replay)
                .setContentIntent(pendingIntent)
                .build()


        mediaPlayer.start()
        mediaPlayer.start()

        val pattern = longArrayOf(0, 100, 1000)
        vibrator.vibrate(pattern, 0)

        startForeground(1, notification)

        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        vibrator.cancel()
    }
}
