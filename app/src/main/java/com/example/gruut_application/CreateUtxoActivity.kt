package com.example.gruut_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class CreateUtxoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_utxo)

        val coinInput = findViewById<EditText>(R.id.input_coin)
        val btnGenNonBase = findViewById<Button>(R.id.btnGenNonBase)
        val btnGoMainPage = findViewById<Button>(R.id.btnGoMain)

        btnGenNonBase.setOnClickListener {

        }

        btnGoMainPage.setOnClickListener {

        }

    }
}