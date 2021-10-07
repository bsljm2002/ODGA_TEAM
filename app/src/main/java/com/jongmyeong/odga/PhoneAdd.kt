package com.jongmyeong.odga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jongmyeong.odga.databinding.ActivityPhoneAddBinding

class PhoneAdd : AppCompatActivity() {
    val binding by lazy { ActivityPhoneAddBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val btnSave = binding.btnSave

        val DB_NAME = "sqlite.sql"
        val DB_VERSION = 1
        val helper = SqliteHelper(this, DB_NAME, DB_VERSION)

        btnSave.setOnClickListener {
            var txtPhone = binding.editPhone.text.toString()
            var txtName = binding.editName.text.toString()

            if(txtName.isNotEmpty()){
                val phonebook = PhoneBook(null, txtName, txtPhone)
                helper?.insertPhoneBook(phonebook)
                binding.editName.setText("")
                binding.editPhone.setText("")

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}