package com.jongmyeong.odga

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqliteHelper(context: Context, name:String, version:Int): SQLiteOpenHelper(context, name, null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        val create = "create table phonebook (id integer primary key, fname text, fphone text)"
        db?.execSQL(create)
    }
    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        // 테이블에 변경사항이 있을 경우 호출됨
        // SqliteHelper()의 생성자를 호출할 떼 기존 데이터베이스와 version을 비교해서 높으면
        // 호출된다.
    }
    // 데이터 입력 함수
    fun insertPhoneBook(phonebook:PhoneBook){
        // db 가져오기
        val wd = writableDatabase
        // 입력타입으로 변환

        val values = ContentValues()
        values.put("fname", phonebook.fname)
        values.put("fphone", phonebook.fphone)


        // db 넣기
        wd.insert("phonebook",null, values)
        // db 닫기
        wd.close()
    }
    // 데이터 조회함수
    @SuppressLint("Range")
    fun selectPhoneBook(): MutableList<PhoneBook>{
        val list = mutableListOf<PhoneBook>()

        val select = "select * from phonebook"

        val rd = readableDatabase
        val cursor = rd.rawQuery(select, null)

        while(cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val fname = cursor.getString(cursor.getColumnIndex("fname"))
            val fphone = cursor.getString(cursor.getColumnIndex("fphone"))
            val phonebook = PhoneBook(id, fname, fphone)
            list.add(phonebook)
        }
        cursor.close()
        rd.close()

        return list
    }
    // 데이터 수정함수
    fun updatePhoneBook(phonebook: PhoneBook){
        val wd = writableDatabase
        val values = ContentValues()
        values.put("fname", phonebook.fname)
        values.put("fphone", phonebook.fphone)
        wd.update("phonebook", values, "id = ${phonebook.id}",null)
        wd.close()
    }
    // 데이터 삭제함수
    fun deletePhoneBook(phonebook: PhoneBook){
//        val delete = "delete from phonebook where id = ${phonebook.id}"
//        wd.execSQL(delete)

        val wd = writableDatabase
        wd.delete("phonebook", "id=${phonebook.id}",null)
        wd.close()
    }
}

data class PhoneBook(var id: Int?, var fname: String, var fphone: String)