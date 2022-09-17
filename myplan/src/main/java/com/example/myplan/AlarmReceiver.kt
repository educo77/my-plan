package com.example.myplan

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, MainActivity::class.java)

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingI = PendingIntent.getActivity(
            context, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, "default")

        //mPendingIntent 해당 알림 클릭시 메인 엑티비티로 이동

        //Intent Get the data
        val bundle = intent.extras
        var alarmItem: String = bundle?.getString("AlarmItem", "AlarmItem") ?: ""
        var diarlyTrue: String = bundle?.getString("DiarlyTrue", "flase") ?: ""
        var numberInt: Int = bundle?.getInt("NumberInt", -1) ?: -2


//        var alarmList: String = bundle?.getString("AlarmList", "AlarmList") ?: ""
        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(android.R.drawable.stat_notify_chat) //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            val channelName = "매일 알람 채널"
            val description = "매일 정해진 시간에 알람합니다."
            val importance = NotificationManager.IMPORTANCE_HIGH //소리와 알림메시지를 같이 보여줌
            val channel = NotificationChannel("default", channelName, importance)
                .apply {
                    //채널에 다양한 정보 설정
                    setShowBadge(true)
                    val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(uri, audioAttributes)
                    enableVibration(true)
                }
            channel.description = description
            notificationManager?.createNotificationChannel(channel)
        } else builder.setSmallIcon(android.R.drawable.ic_menu_my_calendar) // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        builder.setAutoCancel(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setTicker("{Time to watch some cool stuff!}")
            .setContentTitle("${alarmItem}")
            .setContentText("Check the Do It List Please")
            .setContentInfo("INFO")
            .setContentIntent(pendingI)

        // 노티피케이션 동작시킴
        notificationManager.notify(numberInt, builder.build())
        // notify(id 를 변경해야 notification 아이콘이 별도로 생성된다

        Log.d("Call the Broad...", alarmItem)
        Log.d("notify(Id) : ", numberInt.toString())

        //checkBox 값이 참이었을 경우 데일리 알람이 셋팅된다
        if (diarlyTrue == "true") {

            val nextNotifyTime = Calendar.getInstance()

            // 내일 같은 시간으로 알람시간 결정
            nextNotifyTime.add(Calendar.DATE, 1)

            //  Preference에 설정한 값 저장
            val editor =
                context.getSharedPreferences("daily alarm${numberInt}", Context.MODE_PRIVATE).edit()
            editor.putLong("nextNotifyTime", nextNotifyTime.timeInMillis)
            editor.apply()
            val currentDateTime = nextNotifyTime.time
            val nextAlarm =
                SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(
                    currentDateTime
                )
            Toast.makeText(
                context.applicationContext,
                "다음 알람은 " + nextAlarm + "으로 알람이 설정되었습니다!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
