package com.example.myplan

import android.app.Activity
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myplan.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        var calendar = Calendar.getInstance()
        var pendingIntent : PendingIntent? = null
        var mContext : Context? = null
        var date_text = ""
    }
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val helper = SqliteHelper(this, "myplan", 1)
    val adapter = MyRecyclerViewAdapter()
    var numberInt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)	//툴바 사용 설정
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)	//왼쪽 버튼 사용설정(기본은 뒤로가기)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_launcher_foreground)	//왼쪽 버튼 메뉴로 아이콘 변경
        supportActionBar!!.setDisplayShowTitleEnabled(true) //타이틀 보이게 설정

        mContext = this
        adapter.helper = helper

        // 리사이클러뷰 아이템 생성


        adapter.listData.addAll(helper.selectMemo())

        // 리사이클러뷰 어댑터 달기

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.addItemDecoration(MyRecyclerViewAdapter.MyDecoration(this as Context))
        adapter.notifyDataSetChanged()
        // 리사이클러뷰에 스와이프, 드래그 기능 달기
        val swipeHelperCallback = SwipeHelperCallback(adapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat() / 4)    // 1080 / 4 = 270
        }
        ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(binding.recyclerView)

        // 구분선 추가
//        binding.recyclerView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        // 데코레이션


        // 다른 곳 터치 시 기존 선택했던 뷰 닫기
        binding.recyclerView.setOnTouchListener { _, _ ->
            swipeHelperCallback.removePreviousClamp(binding.recyclerView)
            false
        }



        binding.addPlanButton.setOnClickListener {

            val intent = Intent(this, AddActivity::class.java)
            startActivityForResult(intent, 10)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {


//            data!!.getStringExtra("Content")?.let {
//                content = it
//                Log.d("김샘",it)
//            }
//            data!!.getStringExtra("DateTime")?.let {
//                dateTime = it
//            }
//
//            helper.insertMemo(memo)
            //insertMemo 메서드에 Memo를 전달하여 데이터베이스에 저
            Log.d("doit sql list", adapter.listData.toString())

            adapter.listData.clear()
            adapter.listData.addAll(helper.selectMemo())
            adapter.notifyDataSetChanged()

            diaryNotification(calendar)

            //데이터베이스에 새로운 목록을 읽ㅇ어와 어댑터에 세팅하고 갱신
            // = 번호를 갱신하기 위해서 새로운 데이터를 세팅
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_main, menu) //작성한 메뉴파일 설정
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId) {
//            android.R.id.home -> {
//                var baselayout = View.inflate(this@MainActivity, R.layout.manul_img, null) as View
//                val dlg = AlertDialog.Builder(this@MainActivity)
//                dlg.setView(baselayout)
//                dlg.setNegativeButton("Cancel", null)
//                dlg.show()
//            }

        //      메뉴에 텍스트가 출력 안될시 메인레이아웃의 엡바 테그의 팝업테마 설정을 바꾸어야 한다 흰색바탕 흰색글짜일경우 안나옴
        when (item.itemId) {
            R.id.uncheck -> {
                for (i in 0 until adapter.listData.size) {
                    adapter.listData[i].checkbox2 = "false"
                    val memo = adapter.listData[i]
                    helper.updateMemo(memo)
                }
                adapter.notifyDataSetChanged()
            }

            R.id.realarm -> {
                this.diaryNotification(calendar)
            }

            R.id.alldelete -> {
                for (i in 0 until adapter.listData.size) {
                    helper.deleteMemo(adapter.listData[i])
                }
                adapter.listData.clear()
                adapter.notifyDataSetChanged()
            }

            R.id.explain -> {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.manul_img)
                dialog.setTitle("Manul")

                val iv: ImageView = dialog.findViewById(R.id.manul_img) as ImageView
                iv.setImageResource(R.drawable.manul_myplan)

                dialog.show()
            }

        }

        return super.onOptionsItemSelected(item)
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun diaryNotification(calendar: Calendar)  {
        val pm = this.packageManager
        val receiver = ComponentName(this, DeviceBootReceiver::class.java)
//            val receiver = ComponentName(context, DeviceBootReceiver::class.java)

        val alarmManagers = arrayOfNulls<AlarmManager>(adapter.listData.size)
        val intents = arrayOfNulls<Intent>(alarmManagers.size)

        pendingIntent?.cancel() // 알람 전체취소 방법


        for (i in 0 until alarmManagers.size) {
            intents[i] = Intent(applicationContext, AlarmReceiver::class.java)

            Log.d("알람 인텐트[${i}]", intents[i].toString())

            intents[i]?.putExtra("AlarmItem", adapter.listData[i].content)
            intents[i]?.putExtra("DiarlyTrue", adapter.listData[i].checkbox)
            adapter.listData[i].no?.let { intents[i]?.putExtra("NumberInt", it.toInt()) }


            pendingIntent = PendingIntent.getBroadcast(
                this, i, intents[i]!!,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            //            알람 리시버 작동시 넥스트 데이터 타임 값으로 아래 사항을 환결설정Preferences 해주고, 날짜를 증가 시켜주는게 필요하다.
            // notification 반복은 알람메니저가 아닌 노티피케이션메니저가 알아서 하므로 노티피케이션 반복을
            //삭제해주고 재설정 하는 게 맞다 그리고 반복문이 참일경우만 노티피케이션 반복을실행시켜주는게 맞는것 같다
            val editor =
                getSharedPreferences("daily alarm${numberInt}", MODE_PRIVATE).edit()
            editor.clear()
            editor.putLong("nextNotifyTime", calendar.timeInMillis)
            editor.apply()

            calendar.set(
                adapter.listData[i].year,
                adapter.listData[i].month,
                adapter.listData[i].day,
                adapter.listData[i].hour,
                adapter.listData[i].minute,
            )
            val cureDateTime: LocalDateTime = LocalDateTime.now()

            //LocalDateTime 초기화는 다른 초기화의 영향을 받을수 있으므로 쓰기 직전에 사용

            when {

                (calendar.before(Calendar.getInstance()) && adapter.listData[i].checkbox == "false") -> {
                    alarmManagers[i]?.cancel(pendingIntent)

                    Log.d(
                        "Delete alarmmanagers${adapter.listData[i].dateTime}",
                        adapter.listData[i].content
                    )
                }
                (calendar.after(Calendar.getInstance()) && adapter.listData[i].checkbox == "false") -> {

                    Log.d(
                        "Set SingleAlarm${adapter.listData[i].dateTime}",
                        adapter.listData[i].content
                    )

                    alarmManagers[i] = getSystemService(ALARM_SERVICE) as AlarmManager
                    //this.getSystemService 로 MainActivity 에서는 적용해야하고 다른 엑티비티 에서는 context.getSystemService
                    alarmManagers[i]!!.setAlarmClock(
                        AlarmManager.AlarmClockInfo(
                            calendar.timeInMillis,
                            pendingIntent
                        ), pendingIntent
                    )
                }

                (calendar.after(Calendar.getInstance()) && adapter.listData[i].checkbox == "true") -> {
                    Log.d(
                        "Set After Interverl${adapter.listData[i].dateTime}",
                        adapter.listData[i].content
                    )

                    alarmManagers[i] = getSystemService(ALARM_SERVICE) as AlarmManager

                    if (alarmManagers[i] != null) {
                        alarmManagers[i]!!.setRepeating(
                            AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                            AlarmManager.INTERVAL_DAY, pendingIntent
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManagers[i]!!.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                    }
                    //부팅 후 실행되는 리시버 사용가능하게 설정
                    pm.setComponentEnabledSetting(
                        receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }

                (calendar.before(Calendar.getInstance()) && adapter.listData[i].checkbox == "true") -> {
                    calendar.timeInMillis = System.currentTimeMillis()

                    when {
                        cureDateTime.hour == adapter.listData[i].hour && cureDateTime.minute >= adapter.listData[i].minute -> {

                            calendar.set(
                                cureDateTime.year,
                                cureDateTime.monthValue.minus(1),
                                cureDateTime.dayOfMonth.plus(1),
                                adapter.listData[i].hour,
                                adapter.listData[i].minute
                            )
                            Log.d("Before Case0 :${calendar.time}", adapter.listData[i].content)

                            alarmManagers[i] = getSystemService(ALARM_SERVICE) as AlarmManager

                            if (alarmManagers[i] != null) {
                                alarmManagers[i]!!.setRepeating(
                                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                                    AlarmManager.INTERVAL_DAY, pendingIntent
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManagers[i]!!.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        calendar.timeInMillis,
                                        pendingIntent
                                    )
                                }
                            }
                            //부팅 후 실행되는 리시버 사용가능하게 설정
                            pm.setComponentEnabledSetting(
                                receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                            )

                        }

                        (cureDateTime.hour > adapter.listData[i].hour && cureDateTime.minute < adapter.listData[i].minute) -> {

                            calendar.set(
                                cureDateTime.year,
                                cureDateTime.monthValue.minus(1),
                                cureDateTime.dayOfMonth.plus(1),
                                adapter.listData[i].hour,
                                adapter.listData[i].minute
                            )
                            Log.d("Before Case1 :${calendar.time}", adapter.listData[i].content)

                            alarmManagers[i] = getSystemService(ALARM_SERVICE) as AlarmManager

                            if (alarmManagers[i] != null) {
                                alarmManagers[i]!!.setRepeating(
                                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                                    AlarmManager.INTERVAL_DAY, pendingIntent
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManagers[i]!!.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        calendar.timeInMillis,
                                        pendingIntent
                                    )
                                }
                            }
                            //부팅 후 실행되는 리시버 사용가능하게 설정
                            pm.setComponentEnabledSetting(
                                receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                            )
                        }

                        (cureDateTime.hour > adapter.listData[i].hour && cureDateTime.minute >= adapter.listData[i].minute) -> {

                            calendar.set(
                                cureDateTime.year,
                                cureDateTime.monthValue.minus(1),
                                cureDateTime.dayOfMonth.plus(1),
                                adapter.listData[i].hour,
                                adapter.listData[i].minute
                            )
                            Log.d("Before Case2 :${calendar.time}", adapter.listData[i].content)

                            alarmManagers[i] = getSystemService(ALARM_SERVICE) as AlarmManager

                            if (alarmManagers[i] != null) {
                                alarmManagers[i]!!.setRepeating(
                                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                                    AlarmManager.INTERVAL_DAY, pendingIntent
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManagers[i]!!.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        calendar.timeInMillis,
                                        pendingIntent
                                    )
                                }
                            }
                            //부팅 후 실행되는 리시버 사용가능하게 설정
                            pm.setComponentEnabledSetting(
                                receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                            )
                        }

                        else -> {
                            calendar.set(
                                cureDateTime.year,
                                cureDateTime.monthValue.minus(1),
                                //monthValue 값은 실제 월수를 나타내므로 카렌다 셋 해줄때는 minus(1)을 해줘야 정확한 값이 등록된다
                                cureDateTime.dayOfMonth,
                                adapter.listData[i].hour,
                                adapter.listData[i].minute,
                                0
                            )
                            Log.d("Before Else :${calendar.time}", adapter.listData[i].content)

                            alarmManagers[i] = getSystemService(ALARM_SERVICE) as AlarmManager

                            if (alarmManagers[i] != null) {
                                alarmManagers[i]!!.setRepeating(
                                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                                    AlarmManager.INTERVAL_DAY, pendingIntent
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManagers[i]!!.setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        calendar.timeInMillis,
                                        pendingIntent
                                    )
                                }
                            }
                            //부팅 후 실행되는 리시버 사용가능하게 설정
                            pm.setComponentEnabledSetting(
                                receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                            )
                        }
                    }
                }
            }
        }
    }

}