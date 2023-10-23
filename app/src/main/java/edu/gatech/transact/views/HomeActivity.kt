package edu.gatech.transact.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import edu.gatech.transact.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_home.*

/*
    This class hosts the nav bar and is able to transition to other pages by the clicking on
    different pages in the nav bar
 */
class HomeActivity : AppCompatActivity() {
    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val tag = "Firebase Cloud Messaging"
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(tag, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(tag, msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })




        val updatePage = UpdatePage()
        val profilePage = ProfilePage()

        makeCurrentFragment(updatePage)

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.update_icon -> makeCurrentFragment(updatePage)
                R.id.profile_icon -> makeCurrentFragment(profilePage)
                R.id.location_icon -> {
                    startActivity(Intent(this@HomeActivity, GoogleMapActivity::class.java))
                }
            }
            true
        }
        // bottom navigation view

    }

    private fun makeCurrentFragment(currentPage: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.homePage, currentPage)
            commit()
        }
    }
}
