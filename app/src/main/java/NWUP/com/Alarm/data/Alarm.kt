package NWUP.com.Alarm.data

import NWUP.com.Alarm.receiver.AlertReceiver
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.FRIDAY
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.MONDAY
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.RECURRING
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.SATURDAY
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.SUNDAY
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.THURSDAY
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.TITLE
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.TUESDAY
import NWUP.com.Alarm.receiver.AlertReceiver.Companion.WEDNESDAY
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "alarm_table")
class Alarm(
    @PrimaryKey
    var alarmId:Int,
    var hour:Int,
    var minute:Int,
    var title:String,
    var started: Boolean,
    var recurring:Boolean,
    var monday:Boolean,
    var tuesday:Boolean,
    var wednesday:Boolean,
    var thursday:Boolean,
    var friday:Boolean,
    var saturday:Boolean,
    var sunday:Boolean
) {


    fun schedule(context:Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlertReceiver::class.java)
        intent.putExtra(RECURRING, recurring)
        intent.putExtra(MONDAY, monday)
        intent.putExtra(TUESDAY, tuesday)
        intent.putExtra(WEDNESDAY, wednesday)
        intent.putExtra(THURSDAY, thursday)
        intent.putExtra(FRIDAY, friday)
        intent.putExtra(SATURDAY, saturday)
        intent.putExtra(SUNDAY, sunday)
        intent.putExtra(TITLE, title)

        val alarmPendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0)

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // if alarm time has already passed, increment day by 1
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }

        if (!recurring) {
            var toastText: String? = null
            try {
                toastText = String.format("Einmaliger Alarm \"%s\" erstellt um %02d:%02d", title, hour, minute)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmPendingIntent)
        } else {
            if(getRecurringDaysText() == "") {
                var toastText: String? = null
                try {
                    toastText = String.format("Einmaliger Alarm \"%s\" erstellt um %02d:%02d", title, hour, minute)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmPendingIntent)
            }
            else {
                val toastText = String.format("Wiederholender Alarm \"%s\" erstellt für %s um %02d:%02d", title, getRecurringDaysText(), hour, minute)
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
                val RUN_DAILY = 24 * 60 * 60 * 1000.toLong()
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, RUN_DAILY, alarmPendingIntent)
            }
        }

        started = true
    }

    //sends intent to cancel pending alarm
    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0)
        alarmManager.cancel(alarmPendingIntent)
        started = false
        val toastText =
            String.format("Alarm abgebrochen für %02d:%02d", hour, minute)
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        Log.i("cancel", toastText)
    }

    // without the line "started = false" to prevent the observer from using onToggle
    fun cancelAlarmDelete(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0)
        alarmManager.cancel(alarmPendingIntent)
        val toastText =
            String.format("Alarm abgebrochen für %02d:%02d", hour, minute)
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        Log.i("cancel", toastText)
    }

    //gets String to put in Toast in schedule
    fun getRecurringDaysText(): Any? {
        if (!recurring) {
            return null
        }

        var days = ""
        if (monday) days += "Mo "
        if (tuesday) days += "Tu "
        if (wednesday) days += "We "
        if (thursday) days += "Th "
        if (friday) days += "Fr "
        if (saturday) days += "Sa "
        if (sunday) days += "Su "

        return days
    }

}