package com.example.gruut_application.jsons

import kotlinx.serialization.Serializable

class Signup {

    @Serializable
    data class Signup(val email : String, val passwd : String, val address : String,
                      val publicKey : String, val signPublicKey : String)

    fun SendSignupData(
        email: String,
        passwd: String,
        address: String,
        publicKey: String,
        signPublicKey: String
    ): Signup {
        return Signup(email, passwd, address, publicKey, signPublicKey)
    }

}