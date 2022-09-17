package com.example.myplan

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Memo(
    var no: Long?, var content: String, var detail: String, var dateTime: String?
    , var year: Int, var month: Int, var day: Int, var hour: Int, var minute: Int,
    var checkbox: String, var checkbox2: String
)

class SqliteHelper(context: Context, name: String, version: Int): SQLiteOpenHelper(context, name, null, version) {
    // SQLiteOpenHelper(컨텍스트, 데이터베이스명, 팩토리, 버전 정보)
    override fun onCreate(db: SQLiteDatabase?) {
//         super.onConfigure(db)
//        db?.disableWriteAheadLogging()
        val create = "create table myplan (no integer primary key, content text, detail text, dateTime text, " +
                "dateYear integer, dateMonth integer, dateDay integer, dateHour integer, dateMinute integer," +
                "checkBox text, checkBox2 text)"
//        val create = "create table memo (no integer primary key, content text, dateTime text, minute integer)"

        db?.execSQL(create)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    } // SqliteHelper에 전달되는 버전 정보가 변경되었을 때 현재 생성되어 있는 데이터베이스의 버전과 비교해서 더 높으면 호출

    // 데이터 삽입 메서드 INSERT를 구현
    fun insertMemo(memo: Memo) {
        val values = ContentValues(0)
        values.put("content", memo.content)
        values.put("detail", memo.detail)
        values.put("dateTime", memo.dateTime)
        values.put("dateYear", memo.year)
        values.put("dateMonth", memo.month)
        values.put("dateDay", memo.day)
        values.put("dateHour", memo.hour)
        values.put("dateMinute", memo.minute)
        values.put("checkBox", memo.checkbox)
        values.put("checkBox2", memo.checkbox2)

        //put("컬럼명", 값)

        //구현되어이 있는 writableDatabase에 테이블명과 함께 앞에서 작성한 값을 전달(INSERT)
        val wd = writableDatabase
//       wd.insert("memo", null, values )
        wd.insert("myplan", null, values)
        wd.close()
        //사용한 후에는 반드시 close()
    }
    //데이터 조회 메서드 SELECT를 구현
    @SuppressLint("Range")
    fun selectMemo(): MutableList<Memo> {
        val list = mutableListOf<Memo>()
        //가장 윗줄에 반환할 값을 변수로 선언하고 최하단 코드에서 반환(return)

        val select = "select * from myplan"
        //memo의 전체 데이터를 조회하는 쿼리를 작성
        val rd = readableDatabase
        //읽기전영 데이터베이스를 변수에 담음

        val cursor = rd.rawQuery(select, null)
        //rawQuery() 메서드에 앞에서 작성해둔 쿼리를 담아서 실행하면 커서의 형태로 값이 반환
        //커서(Cursor): 데이터셋을 처리할 때 현재 위치를 포함하는 데이터 요소
        while (cursor.moveToNext()) {
            /* 다음 줄에 사용할 수 있는 레코드가 있는지 여부를 반환하고,해당 커서를 다음 위치로 이동
            레코드가 없으면 반복문 탈출하며 모든 레코드를 읽을때까지 반복  */
            val no = cursor.getLong(cursor.getColumnIndex("no"))
            val content = cursor.getString(cursor.getColumnIndex("content"))
            val detail = cursor.getString(cursor.getColumnIndex("detail"))
            val dateTime = cursor.getString(cursor.getColumnIndex("dateTime"))
            val year = cursor.getInt(cursor.getColumnIndex("dateYear"))
            val month = cursor.getInt(cursor.getColumnIndex("dateMonth"))
            val day = cursor.getInt(cursor.getColumnIndex("dateDay"))
            val hour = cursor.getInt(cursor.getColumnIndex("dateHour"))
            val minute = cursor.getInt(cursor.getColumnIndex("dateMinute"))
            val checkbox = cursor.getString(cursor.getColumnIndex("checkBox"))
            val checkbox2 = cursor.getString(cursor.getColumnIndex("checkBox2"))
            list.add(Memo(no, content, detail, dateTime, year, month, day, hour, minute, checkbox, checkbox2))

        }
        cursor.close()
        rd.close()
        return list
    }
    fun updateMemo(memo: Memo) {
        val values = ContentValues(0)
        values.put("content", memo.content)
        values.put("detail", memo.detail)
        values.put("dateTime", memo.dateTime)
        values.put("dateYear", memo.year)
        values.put("dateMonth", memo.month)
        values.put("dateDay", memo.day)
        values.put("dateHour", memo.hour)
        values.put("dateMinute", memo.minute)
        values.put("checkBox", memo.checkbox)
        values.put("checkBox2", memo.checkbox2)


        val wd =writableDatabase
        wd.update("myplan", values, "no = ${memo.no}", null)

        wd.close()

    }

    fun deleteMemo(memo: Memo){
        val delete = "delete from myplan where no = ${memo.no}"
        //delete from 테이블명 where 조건식(컬럼명 = 값)
        //삭제 쿼리를 작성하고 변수에 저장
        val db = writableDatabase
        db.execSQL(delete)
        db.close()
    }

}