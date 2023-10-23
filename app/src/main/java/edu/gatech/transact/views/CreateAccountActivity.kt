package edu.gatech.transact.views

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.gatech.transact.R
import edu.gatech.transact.extensions.Extensions.toast
import edu.gatech.transact.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_account.*


class CreateAccountActivity : AppCompatActivity() {
    lateinit var userEmail: String
    lateinit var userPassword: String
    lateinit var userName: String
    lateinit var createAccountInputsArray: Array<EditText>
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
//    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase database
        database = FirebaseDatabase.getInstance().reference
        firestore = Firebase.firestore
        setContentView(R.layout.activity_create_account)
        createAccountInputsArray = arrayOf(etEmail, etPassword, etConfirmPassword)
        btnCreateAccount.setOnClickListener {
            if (validPassword()) {
                signIn()
                writeNewUser()
            }
        }
        setSpannableText()
    }

    private fun setSpannableText() {
        val tv = findViewById<TextView>(R.id.log_in)
        val string = SpannableString(tv.text)
        string.setSpan(
            StyleSpan(Typeface.BOLD),
            string.indexOf("?") + 2,
            string.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@CreateAccountActivity, SignInActivity::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }

        string.setSpan(
            clickableSpan,
            string.indexOf("?") + 2,
            string.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.highlightColor = Color.TRANSPARENT
        tv.text = string
    }


    /*
        This function write new user to the database
     */
    private fun writeNewUser() {
        //val user = Users(userEmail, userPassword, userName)
       // database.child(userName).setValue(user)

        val userMap = hashMapOf(
            "Email" to userEmail,
//            "Password" to userPassword, probably shouldn't record the password
            "Username" to userName,
            "Accepting agreement" to true
        )
        firestore.collection("users").document(userName).set(userMap)
    }

    /* check if there's a signed-in user*/
    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.let {
            startActivity(Intent(this, HomeActivity::class.java))
            toast("welcome back")
        }
    }
    /*
    If you create your own keys, they must be UTF-8 encoded, can be a maximum of 768 bytes,
    and cannot contain ., $, #, [, ], /, or ASCII control characters 0-31 or 127.
    You cannot use ASCII control characters in the values themselves, either.
     */
    private fun invalidEmail(email: String): Boolean {
        val end = email.indexOf('@')
        if (end == -1) {
            return true
        }
        val name = email.substring(0, end)
        return name.contains('.') || name.contains('$') || name.contains('#')
                || name.contains('[') || name.contains(']') || name.contains('/')
    }

    private fun notEmpty(): Boolean = etEmail.text.toString().trim().isNotEmpty() &&
            etPassword.text.toString().trim().isNotEmpty() &&
            etConfirmPassword.text.toString().trim().isNotEmpty()


    /*
        Note that the sequence of conditional checking is important because it decides which
        error message to show up
     */
    private fun validPassword(): Boolean {
        var valid = true
        val emailString = etEmail.text.toString().trim()
        if (notEmpty() && !invalidEmail(emailString) &&
            etPassword.text.toString().trim() == etConfirmPassword.text.toString().trim() &&
            etPassword.length() >= 8
        ) {
        } else if (!notEmpty()) {
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
            valid = false
        } else if (invalidEmail(emailString)) {
            etEmail.error = "email is not valid and it cannot contain any symbols from .$#[]/"
            valid = false
        } else if (etPassword.length() < 8){
            etPassword.error = "Password should have at least 8 characters!"
            valid = false
        } else if (!etPassword.equals(etConfirmPassword)) {
            etPassword.error = "passwords are not matching!"
            etConfirmPassword.error = "passwords are not matching!"
            toast("passwords are not matching!")
            valid = false
        }
        return valid

    }

    private fun signIn() {
//        if (identicalPassword()) {
            // identicalPassword() returns true only  when inputs are not empty and passwords are identical
            userEmail = etEmail.text.toString().trim()
            userPassword = etPassword.text.toString().trim()
            val end = userEmail.indexOf('@')
            userName = userEmail.substring(0, end)
//            database.child(userEmail).setValue(Users(userEmail, userPassword))

            /*create a user*/
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, WarningPage::class.java))
                    } else {
                        toast("failed to Authenticate!")
                        Log.v("authentication error", "failed to authenticate")
                    }
                }
//        }
    }

}