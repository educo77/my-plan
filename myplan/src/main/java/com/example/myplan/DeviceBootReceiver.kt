package com.example.myplan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            // on device boot complete, reset the alarm
            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            //
            val sharedPreferences =
                context.getSharedPreferences("daily alarm", Context.MODE_PRIVATE)
            val millis =
                sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().timeInMillis)
            val current_calendar = Calendar.getInstance()
            val nextNotifyTime: Calendar = GregorianCalendar()
            nextNotifyTime.timeInMillis = sharedPreferences.getLong("nextNotifyTime", millis)
            if (current_calendar.after(nextNotifyTime)) {
                nextNotifyTime.add(Calendar.DATE, 1)
            }
            val currentDateTime = nextNotifyTime.time
            val date_text =
                SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(
                    currentDateTime
                )
            Toast.makeText(
                context.applicationContext,
                "[재부팅후] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!",
                Toast.LENGTH_SHORT
            ).show()
            manager?.setRepeating(
                AlarmManager.RTC_WAKEUP, nextNotifyTime.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
        }
    }
}