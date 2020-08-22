package NWUP.com.Alarm

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer


class RescheduleAlarmsService : LifecycleService() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val alarmRepository = AlarmRepository(application)
        alarmRepository.alarmsLiveData.observe(
            this,
            Observer<List<Alarm>> { alarms ->
                for (a in alarms) {
                    if (a.started) {
                        a.schedule(applicationContext)
                    }
                }
            })
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}