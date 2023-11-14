package com.example.gruut_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val inputEmail = findViewById<EditText>(R.id.input_email_login)
        val emailPattern : Pattern = android.util.Patterns.EMAIL_ADDRESS
        val inputPasswd = findViewById<EditText>(R.id.input_passwd_login)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val loginToSignup = findViewById<TextView>(R.id.login_to_signup)

        inputPasswd.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            val pwRegex = """^[0-9a-zA-Z!@_#$%^+\-=]*$"""
            val pwPattern = Pattern.compile(pwRegex)
            if (source.isNullOrBlank() || pwPattern.matcher(source).matches()) {
                return@InputFilter source
            }
            val myToast = Toast.makeText(this.applicationContext,
                "This character cannot be entered", Toast.LENGTH_SHORT)
            myToast.show()
            ""
        })

        btnLogin.setOnClickListener {
            if(emailPattern.matcher(inputEmail.text).matches()) {
                val id = inputEmail.text
                val pw = inputPasswd.text
                // send message to server
                val jsonObject : JSONObject = JSONObject()
                jsonObject.put("email", id)
                jsonObject.put("passwd", pw)
                val result : String = loginServer(jsonObject)
                if(result == "success"){
                    val loginToMainIntent = Intent(this@LoginActivity, UtxoListActivity::class.java)
                    startActivity(loginToMainIntent)
                } else{
                    Toast.makeText(this.applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            } else{
                Toast.makeText(this.applicationContext, "The email format is not appropriate.", Toast.LENGTH_SHORT).show()
            }
        }
        loginToSignup.setOnClickListener {
            val loginToSignIntent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(loginToSignIntent)
        }
    }

    // function for connecting server&application
    private fun loginServer(jsonMessage : JSONObject) : String {
        try {
            val client = OkHttpClient()
            val reqBody = jsonMessage.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .post(reqBody)
                .url("http://192.168.129.134:5000/login")
                .build()
            val response =  client.newCall(request).execute()
            Log.v("connect_flask", "Send login information to server : " + (response).toString())
            return response.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            return "Failure"
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return "Failure"
        }
    }

}