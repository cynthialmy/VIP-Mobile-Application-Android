package edu.gatech.transact.views

import android.app.AlertDialog
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
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.gatech.transact.R
import edu.gatech.transact.data.Users
import edu.gatech.transact.extensions.Extensions.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.fragment_profile_page.*


class SignInActivity : AppCompatActivity() {
    private lateinit var signInEmail: String
    private lateinit var signInPassword: String
    private lateinit var signInInputsArray: Array<EditText>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        firestore = Firebase.firestore
        signInInputsArray = arrayOf(etSignInEmail, etSignInPassword)
        val tv = findViewById<TextView>(R.id.sign_up)
        val string = SpannableString(tv.text)
        val start = string.indexOf("?") + 2
        val end = string.length
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@SignInActivity, CreateAccountActivity::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        setSpannableText(tv, string, start, end, clickableSpan)
        btnSignIn.setOnClickListener {
            signInUser()
        }
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//           it shows up in red but it is runnable
//           it is in the auto generated file
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInButton = findViewById<Button>(R.id.google_sign_in)
        googleSignInButton.setOnClickListener{
            signIn()
        }

        val forgotPassword = findViewById<TextView>(R.id.forgot_password)
        val string2 = SpannableString(forgotPassword.text)
        val start2 = 0
        val end2 = string2.length
        val clickableSpan2 = object : ClickableSpan() {
            override fun onClick(view: View) {
                startActivity(Intent(this@SignInActivity, ResetPasswordActivity::class.java))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        setSpannableText(forgotPassword, string2, start2, end2, clickableSpan2)

        forgotPassword.setOnClickListener{
            startActivity(Intent(this@SignInActivity,ResetPasswordActivity::class.java))
        }

    }
    private fun writeNewUser() {
        var userInstance = FirebaseAuth.getInstance().currentUser
        var email = userInstance?.email!!
        val end = userInstance.email?.indexOf('@')!!
        val userName = email.substring(0, end)
        // note that if a user uses google service to log in
        // we do not know their password
        val userData = Users(email, password = null, userName)
        database.child(userName).setValue(userData)

        val userMap = hashMapOf(
            "Email" to signInEmail,
            "Password" to signInPassword,
            "Username" to userName
        )
        firestore.collection("users").add(userMap)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        toast("Google Sign In Successful")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                toast("failed")
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    //val user = auth.currentUser
                    writeNewUser()
                    startActivity(Intent(this, HomeActivity::class.java))

                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //updateUI(null)
                }
            }
    }

    private fun setSpannableText(tv: TextView, string: SpannableString,
                                 start: Int, end: Int, clickableSpan: ClickableSpan) {
        string.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        string.setSpan(
            clickableSpan,
            start,
            end,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.highlightColor = Color.TRANSPARENT
        tv.text = string
    }

    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signInUser() {
        signInEmail = etSignInEmail.text.toString().trim()
        signInPassword = etSignInPassword.text.toString().trim()

        if (notEmpty()) {

            auth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        val user = signIn.result.user
                        if (user!!.isEmailVerified) {
                       //     writeNewUser()
                            startActivity(Intent(this, HomeActivity::class.java))
                            toast("Signed in successfully!")
                            finish()
                        } else {
                            resendVerificationEmail(user)
                            auth.signOut()

                            toast("Email is not verified yet!")
                            Log.w(null, "Email is not verified")
                        }

                    } else {
                        toast("Sign in failed!")
                    }
                }
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        }
    }

    private fun resendVerificationEmail(user: FirebaseUser) {
        val builder = AlertDialog.Builder(this@SignInActivity)
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout:View = inflater.inflate(R.layout.resend_verification_email_layout,
            null)
        with(builder) {
            setTitle("Do you want us to resend the verification email?")
            setPositiveButton("Resend"){ _, _ ->
                user.sendEmailVerification().addOnSuccessListener {
                    toast("Verification email is sent to $signInEmail")
                }.addOnFailureListener {
                    Toast.makeText(this@SignInActivity, it.message, Toast.LENGTH_LONG).show()
                    Log.e(this.toString(), "Exception in sending verification email")
                }
            }
            setNegativeButton("Back"){ _, _ ->
                Log.d("Main","Negative Button clicked")
            }
            setView(dialogLayout)
            show()
        }
    }
}