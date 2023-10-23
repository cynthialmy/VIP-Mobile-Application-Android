package edu.gatech.transact.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object FirebaseUtils {
    val firebaseAuth: FirebaseAuth = Firebase.auth
    val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
}