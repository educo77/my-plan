package com.example.myplan

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.content.Context
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.widget.*
import com.example.myplan.MainActivity.Companion.calendar
import com.example.myplan.MainActivity.Companion.date_text
import com.example.myplan.databinding.ActivityAddBinding
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAddBinding.inflate(layoutInflater) }

    var cal_Year: Int = 0
    var cal_Month: Int = 0
    var cal_DayOfMonth: Int = 0
    var cal_Hour: Int = 0
    var cal_Minute: Int = 0
    var detail_text: String = ""
    var checked: Boolean = false
    val helper = SqliteHelper(this, "myplan", 1)
    val adapter = MyRecyclerViewAdapter()
    lateinit var memo: Memo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)	//툴바 사용 설정
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)	//왼쪽 버튼 사용설정(기본은 뒤로가기)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_launcher_foreground)	//왼쪽 버튼 메뉴로 아이콘 변경
        supportActionBar!!.setDisplayShowTitleEnabled(true)		//타이틀 보이게 설정

        val timePicker = findViewById<View>(R.id.timePicker) as TimePicker
        val datePicker = findViewById<View>(R.id.datePicker) as DatePicker
        timePicker.setIs24HourView(true)


        binding.lookCal.setOnClickListener {
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.look_cal, null)

            val popupWindow = PopupWindow(
                view,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            popupWindow.elevation = 10.0F

            val slideIn = Slide()
            slideIn.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideIn

            val slideOut = Slide()
            slideOut.slideEdge = Gravity.RIGHT
            popupWindow.exitTransition = slideOut

            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(binding.root)
            popupWindow.showAtLocation(
                binding.root, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )
            val dismissBtn = view.findViewById<Button>(R.id.dismiss)

            dismissBtn.setOnClickListener {
                popupWindow.dismiss()
            }
        }



        binding.checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checked = true
            } else checked = false
        }

        binding.addSetBtn.setOnClickListener {
            if (binding.editMemo.text.toString().isNotEmpty()) {

                calendar.timeInMillis = System.currentTimeMillis()

                cal_Year = datePicker.year
                cal_Month = datePicker.month
                cal_DayOfMonth = datePicker.dayOfMonth
                cal_Hour = timePicker.currentHour
                cal_Minute = timePicker.currentMinute

                calendar[Calendar.HOUR_OF_DAY] = cal_Hour
                calendar[Calendar.MINUTE] = cal_Minute
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.YEAR] = datePicker.year
                calendar[Calendar.MONTH] = datePicker.month
                calendar[Calendar.DAY_OF_MONTH] = datePicker.dayOfMonth
            }
            saveAddAlarm()
        }
    }

    fun saveAddAlarm(){

        val currentDateTime = calendar.time
        date_text = SimpleDateFormat(
            "yyyy/ MM/ dd/ EE  a hh : mm",
            Locale.getDefault()
        ).format(currentDateTime)


        memo = Memo(
            null,
            binding.editMemo.text.toString(),
            detail_text,
            date_text,
            cal_Year,
            cal_Month,
            cal_DayOfMonth,
            cal_Hour,
            cal_Minute,
            checked.toString(),
            "false"
        )

        adapter.helper = helper
        helper.insertMemo(memo)

        binding.editMemo.setText("")
        binding.editMemo.endBatchEdit()

        val intent = intent
        val sdf = SimpleDateFormat("yyyy/mm/dd hh:mm")
        intent.putExtra("Content", binding.editMemo.text.toString())
        intent.putExtra("DateTime", sdf.format(System.currentTimeMillis()))
        binding.editMemo.setText("")
        Log.d("데이트타임", sdf.format(System.currentTimeMillis()))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

