package com.example.gruut_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Utils
import org.json.JSONObject
import java.io.IOException
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private lateinit var keyStore : KeyStore
    private lateinit var address : String
    private lateinit var userPubKeyPerm : String
    private lateinit var signPubKeyPerm : String
    private var userAlias : String = "userKey"
    private var signAlias : String = "signKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        // ========================================================================================
        // Generate key store
        keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        // ========================================================================================

        val inputEmail = findViewById<EditText>(R.id.input_email_signup)
        val emailCheck = findViewById<TextView>(R.id.email_check)
        val inputPasswd = findViewById<EditText>(R.id.input_passwd_signup)
        val inputConfirmPasswd = findViewById<EditText>(R.id.input_confirm_passwd)
        val passwdCheck = findViewById<TextView>(R.id.passwd_check)
        val accountAddr = findViewById<TextView>(R.id.account_address)
        val btnGenAccount = findViewById<Button>(R.id.btnGenAccount)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        val emailPattern : Pattern = android.util.Patterns.EMAIL_ADDRESS

        // Generate key store
        keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }



        inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(emailPattern.matcher(p0.toString()).matches()){
                    emailCheck.text = resources.getString(R.string.email_valid)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

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

        inputConfirmPasswd.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
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

        inputConfirmPasswd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(inputPasswd.text.toString() == inputConfirmPasswd.text.toString()){
                    passwdCheck.text = resources.getString(R.string.passwd_match)
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        btnGenAccount.setOnClickListener {
            // Set Key spec for user key
            val userKeyParamSpec = KeyGenParameterSpec.Builder(
                userAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                setDigests(
                    KeyProperties.DIGEST_SHA512,
                    KeyProperties.DIGEST_SHA256
                )
                setUserAuthenticationRequired(false)
                build()
            }

            // Set Key spec for sign key(Permanent)
            val signKeyParamSpec = KeyGenParameterSpec.Builder(
                signAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                setDigests(
                    KeyProperties.DIGEST_SHA512,
                    KeyProperties.DIGEST_SHA256
                )
                setUserAuthenticationRequired(false)
                build()
            }

            val userKeyKpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )

            val signKeyKpg : KeyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )

            userKeyKpg.initialize(userKeyParamSpec)
            val userPK : KeyPair = userKeyKpg.genKeyPair()
            val publicKey = keyStore.getCertificate(userAlias).publicKey
            userPubKeyPerm = formatPemPublicKey(Base64.encodeToString(publicKey.encoded, Base64.DEFAULT))
            address = Base58.encode(Utils.sha256hash160(Sha256Hash.hash(publicKey.encoded)))

            signKeyKpg.initialize(signKeyParamSpec)
            val signPK : KeyPair = signKeyKpg.genKeyPair()
            val signPublicKey = keyStore.getCertificate(signAlias).publicKey
            signPubKeyPerm = formatPemPublicKey(Base64.encodeToString(signPublicKey.encoded, Base64.DEFAULT))

            val accountAddress = ": $address"
            accountAddr.text = accountAddress
        }

        btnSignup.setOnClickListener {
            val signupIntent : Intent = Intent(this@SignupActivity, UtxoListActivity::class.java)
            if ((inputEmail.text == null) or (inputPasswd.text == null) or (inputConfirmPasswd.text == null)) {
                Toast.makeText(this.applicationContext, "You should fill all inputs!", Toast.LENGTH_SHORT).show()
            } else {
                // Send user information to server
                val signupUrl : String = "http://192.168.129.134:5000/signup"
                val jsonObject : JSONObject = JSONObject()
                jsonObject.put("email", inputEmail.text)
                jsonObject.put("passwd", inputPasswd.text)
                jsonObject.put("address", address)
                jsonObject.put("userPubKey", userPubKeyPerm)
                jsonObject.put("signPubKey", signPubKeyPerm)
                getServer(signupUrl, jsonObject)

                // move activity
                startActivity(signupIntent)
            }
        }

    }

    private fun formatPemPublicKey(base64PublicKey: String): String {
        val pemHeader = "-----BEGIN PUBLIC KEY-----"
        val pemFooter = "-----END PUBLIC KEY-----"

        val formattedKey = StringBuilder()
        formattedKey.append(pemHeader)
        formattedKey.append("\n")

        val base64Length = base64PublicKey.length
        var currentIndex = 0
        while (currentIndex < base64Length) {
            val endIndex = if (currentIndex + 64 < base64Length) currentIndex + 64 else base64Length
            formattedKey.append(base64PublicKey.substring(currentIndex, endIndex))
            formattedKey.append("\n")
            currentIndex += 64
        }

        formattedKey.append(pemFooter)
        formattedKey.append("\n")

        return formattedKey.toString()
    }


    // function for connecting server&application
    fun getServer(serverPath : String, jsonMessage : JSONObject) {
        try {
            val client = OkHttpClient()
            val reqBody = jsonMessage.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .post(reqBody)
                .url(serverPath)
                .build()
            val response =  client.newCall(request).execute()
            Log.v("connect_flask", "Send user information to server : " + (response).toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

}