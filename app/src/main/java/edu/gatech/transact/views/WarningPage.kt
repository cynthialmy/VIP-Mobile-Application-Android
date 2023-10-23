package edu.gatech.transact.views

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import edu.gatech.transact.R
import edu.gatech.transact.extensions.Extensions.toast
import edu.gatech.transact.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_warning_page.*
import kotlin.system.exitProcess

class WarningPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_page)
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        if (userEmail != null) {
            Log.i("user", userEmail)
        } else {
            Log.i("user", "email is empty")
        }
        acceptButton.setOnClickListener{
            user!!.sendEmailVerification().addOnSuccessListener {
                // record acceptance on firebase

                // proceed on to email verification
                toast("Verification email is sent to $userEmail")
                firebaseAuth.signOut()
            }.addOnFailureListener {
                Toast.makeText(this@WarningPage, it.message, Toast.LENGTH_LONG).show()
                Log.e(this.toString(), "Exception in sending verification email")
            }
            startActivity(Intent(this, SignInActivity::class.java))
        }
        declineButton.setOnClickListener {
            firebaseAuth.signOut()
            user?.delete()?.addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User account deleted.")
                }
                exitProcess(0)
                //triggerRestart(this)
            }
        }
    }

    private fun triggerRestart(context: Activity) {
        val intent = Intent(context, CreateAccountActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            (context as Activity).finish()
        }
        Runtime.getRuntime().exit(0)
    }
}

/*
user!!.sendEmailVerification().addOnSuccessListener {
                            toast("Verification email is sent to $userEmail")
                            firebaseAuth.signOut()
                        }.addOnFailureListener {
                            Toast.makeText(this@CreateAccountActivity, it.message, Toast.LENGTH_LONG).show()
                            Log.e(this.toString(), "Exception in sending verification email")
                        }
 */