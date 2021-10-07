package com.jongmyeong.odga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.jongmyeong.odga.databinding.ActivityPhonebookUpdateRemoveBinding

class PhonebookUpdateRemove : AppCompatActivity() {

    val binding by lazy { ActivityPhonebookUpdateRemoveBinding.inflate(layoutInflater) }

    private var id : Int? = null
    private var name : String? = null
    private var phone : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val btnUpdate = binding.btnUpdate
        val btnRemove = binding.btnRemove

        val DB_NAME = "sqlite.sql"
        val DB_VERSION = 1
        val helper = SqliteHelper(this, DB_NAME, DB_VERSION)
        if(intent.hasExtra("id") && intent.hasExtra("name") && intent.hasExtra("phone")){
            // 현재 정보를 할당해줌
            id = Integer.parseInt(intent.getStringExtra("id"))
            name = intent.getStringExtra("name").toString()
            phone = intent.getStringExtra("phone").toString()

            // 에디트뷰에 수정하기 전 값을 보여줌
            binding.editName.setText(name)
            binding.editPhone.setText(phone)

            Log.d("dbdata","${id},${name},${phone}")
        }
        else{
            Toast.makeText(this,"불러오기 실패", Toast.LENGTH_SHORT).show()
        }

        btnUpdate.setOnClickListener {

            var txtPhone = binding.editPhone.text.toString()
            var txtName = binding.editName.text.toString()

            if(txtName.isNotEmpty()){

                val phonebook = PhoneBook(id, txtName, txtPhone)
                helper?.updatePhoneBook(phonebook)
                binding.editName.setText("")
                binding.editPhone.setText("")

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }

        }

        btnRemove.setOnClickListener {

            var txtPhone = binding.editPhone.text.toString()
            var txtName = binding.editName.text.toString()
            if(txtName.isNotEmpty()){

                val phonebook = PhoneBook(id, txtName, txtPhone)
                helper?.deletePhoneBook(phonebook)
                binding.editName.setText("")
                binding.editPhone.setText("")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            }

        }
    }
}