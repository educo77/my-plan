package com.example.myplan

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myplan.MainActivity.Companion.calendar
import com.example.myplan.MainActivity.Companion.mContext
import com.example.myplan.databinding.ItemListBinding
import java.util.*


// 리사이클러뷰 어댑터
class MyRecyclerViewAdapter() : RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>() {
    var listData = mutableListOf<Memo>()
    var helper: SqliteHelper? = null
    // 뷰 레이아웃 (item_list.xml) 연결 후 뷰 홀더 만들어서 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder (
        ItemListBinding.inflate (
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    // 전달받은 위치의 아이템 연결
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.MyViewHolder, position: Int) {
        holder.bind(
            listData[position].content, listData[position].checkbox,
            listData[position].checkbox2, listData[position].dateTime!!
        )
        // 리스트 추가후 recyclerview로 돌아왔을때 전에 삭제된 아이템뷰의 서브 아이템뷰 감추기
        getReturnView(holder).animate().translationX(0f).setDuration(100L).start()
    }


    // 아이템 갯수 리턴
    override fun getItemCount() = listData.size


    fun getReturnView(viewHolder: RecyclerView.ViewHolder) : View = viewHolder.itemView.findViewById(
        R.id.swipe_view)
    // -----------------데이터 조작함수 추가-----------------

    // position 위치의 데이터를 삭제 후 어댑터 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    fun removeData(position: Int) {
        helper?.deleteMemo(listData[position])
        listData.removeAt(position)


        notifyItemRemoved(position)
        (mContext as MainActivity).diaryNotification(calendar)
        }

    // 현재 선택된 데이터와 드래그한 위치에 있는 데이터를 교환
    fun swapData(fromPos: Int, toPos: Int) {
        Collections.swap(listData, fromPos, toPos)
        for (i in 0 until listData.size) {
            helper!!.deleteMemo(listData[i])
            listData.size
        }
        for (i in 0 until listData.size) {
            helper!!.insertMemo(listData[i])
            listData.size
        }
        notifyItemMoved(fromPos, toPos)

    }

    // 뷰 홀더 설정
    inner class MyViewHolder(private val binding : ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(title : String, diarly : String, checkbox2 : String, dateTime : String) {

            // 제목 달기
            binding.title = title

            binding.dateText = dateTime

            if (diarly == "true") {
                binding.diarly = "Diarly"
                binding.diarly


            } else {
                binding.diarly = "Once"

            }
            //item_list.xml 의 binding 을 잡아오기
            if (checkbox2 == "true") {
                binding.checked = "Check"
                binding.checkBox2 = true
            } else {
                binding.checked = "Yet"
                binding.checkBox2 = false
            }

            binding.isCheck.setOnCheckedChangeListener{_, isChecked ->
                if (isChecked) {
                    listData[adapterPosition].checkbox2 = "true"
                    helper?.updateMemo(listData[adapterPosition])
                    binding.checked = "Check"

                } else {
                    listData[adapterPosition].checkbox2 = "false"
                    helper?.updateMemo(listData[adapterPosition])
                    binding.checked = "Yet"
                }

            }

            // 서브 메뉴 달기(...모양)
            binding.textViewOptions.setOnClickListener {

                val popup = PopupMenu(binding.textViewOptions.context, binding.textViewOptions)
                popup.inflate(R.menu.recyclerview_item_menu)

                popup.setOnMenuItemClickListener { item ->
                    val str =  when (item.itemId) {
                        R.id.editTitle -> {
                            val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(mContext)
                            builder.setTitle("Title")

                            val input = EditText(mContext)
                            input.setHint("Enter Text")
                            input.inputType = InputType.TYPE_CLASS_TEXT
                            builder.setView(input)
                            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                var m_Text = input.text.toString()
                                listData[layoutPosition].content = m_Text
                                notifyDataSetChanged()
                                helper?.updateMemo(listData[layoutPosition])
                                (mContext as MainActivity).diaryNotification(calendar)
                            })
                            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                            builder.show()
                        }


                        R.id.editContent -> {
                            val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(mContext)
                            builder.setTitle("Content")

                            val input = EditText(mContext)
                            input.setHint("Enter Text")
                            input.inputType = InputType.TYPE_CLASS_TEXT
                            builder.setView(input)
                            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                // Here you get get input text from the Edittext
                                var m_Text = input.text.toString()
                                listData[layoutPosition].detail = m_Text
                                notifyDataSetChanged()
                                helper?.updateMemo(listData[layoutPosition])
                                (mContext as MainActivity).diaryNotification(calendar)
                            })
                            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                            builder.show()
                        }



                        R.id.viewContent -> {
                            val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(mContext)
                            builder.setTitle("View Content")
                            builder.setMessage(listData[layoutPosition].detail)
                            builder.show()
                        }



                        R.id.changeDiarly -> {
                            if (listData[layoutPosition].checkbox == "true") {
                                listData[layoutPosition].checkbox = "false"
                                helper?.updateMemo(listData[layoutPosition])
                                notifyItemChanged(layoutPosition)
                                (mContext as MainActivity).diaryNotification(calendar)
                            } else {
                                listData[layoutPosition].checkbox = "true"
                                helper?.updateMemo(listData[layoutPosition])
                                notifyItemChanged(layoutPosition)
                                (mContext as MainActivity).diaryNotification(calendar)
                            }
                        }

                        else -> "Error"
                    }
                    true
                }
                popup.show()
            }

            // 삭제 텍스트뷰 클릭시 토스트 표시
            binding.tvRemove.setOnClickListener {
                removeData(this.layoutPosition)
            }
        }


    }




    //RecyclerView 꾸미기
    class MyDecoration(val context: Context) : RecyclerView.ItemDecoration() {
        //모든 항목이 출력된후 호출
        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDrawOver(c, parent, state)
            //뷰 사이즈 계산
            val width = parent.width
            val height = parent.height
            //이미지 사이즈 계산
            val dr: Drawable? = ResourcesCompat.getDrawable(
                context.getResources(),
                R.drawable.ic_launcher_foreground, null
            )
            val drWidth = dr?.intrinsicWidth
            val drHeight = dr?.intrinsicHeight
            //이미지가 그려질 위치 계산
            val left = width / 2 - drWidth?.div(2) as Int  //크기 수정 들어감 2 -> 1
            val top = height / 2 - drHeight?.div(2) as Int
            //이미지 출력
//            c.drawBitmap(
//                BitmapFactory.decodeResource(context.getResources(),
//                    R.drawable.kbo
//                ),
//                left.toFloat(),
//                top.toFloat(),
//                null
//            )
        }

        //각 항목을 꾸미기 위해서 호출
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent.getChildAdapterPosition(view) + 1

            if (index % 3 == 0) //left, top, right, bottom
                outRect.set(10, 10, 10, 10)
            else
                outRect.set(10, 10, 10, 0)

            view.setBackgroundColor(Color.parseColor("#ffffff"))// FF 앞 66 넣어 투명도 성정했65%
            ViewCompat.setElevation(view, 3.0f)

        }
    }
}