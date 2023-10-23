package edu.gatech.transact.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import edu.gatech.transact.R
import edu.gatech.transact.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val actionBar: ActionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        val firestore = Firebase.firestore

        btnSignIn.setOnClickListener {
            val email: String = etSignInEmail.text.toString().trim{ it <= ' '}
            if (email.isEmpty()) {
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Please enter email address.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener {task ->
                        if (task.isSuccessful){
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                "Email sent successfully to reset your password!",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                task.exception!!.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }
}