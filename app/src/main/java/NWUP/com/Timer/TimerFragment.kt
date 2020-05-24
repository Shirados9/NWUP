package NWUP.com.Timer

import NWUP.com.R
import NWUP.com.Timer.util.PrefUtil
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.content_timer.*
import kotlinx.android.synthetic.main.fragment_timer.*
import java.util.*

class TimerFragment : Fragment() {

    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState{
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var timerState = TimerState.Stopped
    private var secondsRemaining: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabStart.setOnClickListener{view ->
            startTimer()
            timerState =  TimerState.Running
            updateButtons()
        }

        fabPause.setOnClickListener { view ->
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }

        fabStop.setOnClickListener { view ->
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume() {
        super.onResume()

        initTimer()

        context?.let { removeAlarm(it) }

        //TODO: remove background timer, hide notification
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running){
            timer.cancel()
            val wakeUpTime = context?.let { setAlarm(it, nowSeconds, secondsRemaining) }
        }
        else if (timerState == TimerState.Paused){
            //TODO: show notification
        }

        context?.let { PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, it) }
        context?.let { PrefUtil.setSecondsRemaining(secondsRemaining, it) }
        context?.let { PrefUtil.setTimerState(timerState, it) }
    }

    private fun initTimer(){
        timerState = context?.let { PrefUtil.getTimerState(it) }!!

        //we don't want to change the length of the timer which is already running
        //if the length was changed in settings while it was backgrounded
        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = (if (timerState == TimerState.Running || timerState == TimerState.Paused)
            context?.let { PrefUtil.getSecondsRemaining(it) }
        else
            timerLengthSeconds)!!

        //TODO: change secondsRemaining according to where the background timer stopped

        val alarmSetTime = PrefUtil.getAlarmSetTime(requireContext())
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        //resume where we left off
        else if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished(){
        timerState = TimerState.Stopped

        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength()

        progress_countdown.progress = 0

        context?.let { PrefUtil.setSecondsRemaining(timerLengthSeconds, it) }
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer(){
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength(){
        val lengthInMinutes = context?.let { PrefUtil.getTimerLength(it) }
        if (lengthInMinutes != null) {
            timerLengthSeconds = (lengthInMinutes * 60L)
        }
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength(){
        timerLengthSeconds = context?.let { PrefUtil.getPreviousTimerLengthSeconds(it) }!!
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        progress_countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons(){
        when (timerState) {
            TimerState.Running ->{
                fabStart.isEnabled = false
                fabPause.isEnabled = true
                fabStop.isEnabled = true
            }
            TimerState.Stopped -> {
                fabStart.isEnabled = true
                fabPause.isEnabled = false
                fabStop.isEnabled = false
            }
            TimerState.Paused -> {
                fabStart.isEnabled = true
                fabPause.isEnabled = false
                fabStop.isEnabled = true
            }
        }
    }
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_timer, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
