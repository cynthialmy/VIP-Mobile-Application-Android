package edu.gatech.transact.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Users(val email: String? = null,
                 val password: String? = null,
                 val userName: String? = null) {

}