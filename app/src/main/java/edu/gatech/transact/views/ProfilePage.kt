package edu.gatech.transact.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.gatech.transact.R
import edu.gatech.transact.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile_page.*
import kotlinx.android.synthetic.main.fragment_profile_page.view.*
import edu.gatech.transact.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sign_in.*

class ProfilePage : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(
            R.layout.fragment_profile_page,
            container, false
        )

        view.btnSignOut.setOnClickListener {
            firebaseAuth.signOut()
            activity?.startActivity(Intent(activity, SignInActivity::class.java))
            Toast.makeText(activity, "Signed Out", Toast.LENGTH_SHORT).show()
        }


        val user = FirebaseAuth.getInstance().currentUser
        val emailText = view.findViewById<TextView>(R.id.et_email)
        emailText.setText(user?.email)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val email = user?.email!!
        val end = user.email?.indexOf('@')!!
        val userName = email.substring(0, end)
        val ref: DatabaseReference = database.getReference(userName)

        //val textFirstName = view.findViewById<TextView>(R.id.tv_first_name)
        //val textLastName = view.findViewById<TextView>(R.id.tv_last_name)
        val textPhoneNum = view.findViewById<TextView>(R.id.tv_phone_no)

        val textFullName = view.findViewById<TextView>(R.id.fullname)

        ref.child("profile").get().addOnSuccessListener {
            if (it.exists()) {
                val firstname = it.child("First Name").value
                val lastname = it.child("Last Name").value
                if (firstname != null && lastname != null) {
                    textFullName.text = "$firstname $lastname"
                }
                val phoneNum = it.child("Phone Number").value
                if (phoneNum != null) {
                    textPhoneNum.text = phoneNum.toString()
                }
            } else {
                Toast.makeText(activity, "profile does not exist",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(activity, "Failed",Toast.LENGTH_SHORT).show()
        }

        showEditFullNameDialog(textFullName)
        //showEditLastNameDialog(textLastName)
        showEditPhoneNumDialog(textPhoneNum)


        /*-----------------Email --------------------------------------*/
        ref.child("profile").child("Email").setValue(emailText.text.toString())
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun showEditFullNameDialog(textView: TextView) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        var user = FirebaseAuth.getInstance().currentUser
        var email = user?.email!!
        val end = user.email?.indexOf('@')!!
        val userName = email.substring(0, end)
        val ref: DatabaseReference = database.getReference(userName)
        /*textView.setOnLongClickListener(View.OnLongClickListener {
            val builder = AlertDialog.Builder(activity)
            val inflater:LayoutInflater = layoutInflater
            val dialogLayout:View = inflater.inflate(R.layout.layout_dialog_profile_first_name,
                null)
            val editText = dialogLayout.findViewById<EditText>(R.id.et_first_name)
            with(builder) {
                setTitle("Enter First Name:")
                setPositiveButton("SAVE"){dialog, which ->
                    tv_first_name.text = editText.text.toString()
                    ref.child("profile").child("First Name").setValue(editText.text.toString().trim())
                }
                setNegativeButton("CANCEL"){dialog, which ->
                    Log.d("Main","Negative Button clicked")
                }
                setView(dialogLayout)
                show()
            }
            return@OnLongClickListener true
        })*/
        textView.setOnClickListener{
            val builder = AlertDialog.Builder(activity)
            val inflater:LayoutInflater = layoutInflater
            val dialogLayout:View = inflater.inflate(R.layout.layout_dialog_profile_first_name,
                null)
            val editFname = dialogLayout.findViewById<EditText>(R.id.et_first_name)
            val editLname = dialogLayout.findViewById<EditText>(R.id.et_last_name)
            with(builder) {
                setTitle("Enter Full Name: ")
                setPositiveButton("SAVE"){dialog, which ->
                    ref.child("profile").child("First Name").setValue(editFname.text.toString().trim())
                    ref.child("profile").child("Last Name").setValue(editLname.text.toString().trim())
                    fullname.text = editFname.text.toString() + " " + editLname.text.toString()
                }
                setNegativeButton("CANCEL"){dialog, which ->
                    Log.d("Main","Negative Button clicked")
                }
                setView(dialogLayout)
                show()
            }
        }
    }

    /*private fun showEditLastNameDialog(textView: TextView) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        var user = FirebaseAuth.getInstance().currentUser
        var email = user?.email!!
        val end = user.email?.indexOf('@')!!
        val userName = email.substring(0, end)
        val ref: DatabaseReference = database.getReference(userName)
        /*textView.setOnLongClickListener(View.OnLongClickListener {
            val builder = AlertDialog.Builder(activity)
            val inflater:LayoutInflater = layoutInflater
            val dialogLayout:View = inflater.inflate(R.layout.layout_dialog_profile_last_name,
                null)
            val editText = dialogLayout.findViewById<EditText>(R.id.et_last_name)
            with(builder) {
                setTitle("Enter Last Name:")
                setPositiveButton("SAVE"){dialog, which ->
                    tv_last_name.text = editText.text.toString()
                    ref.child("profile").child("Last Name").setValue(editText.text.toString().trim())
                }
                setNegativeButton("CANCEL"){dialog, which ->
                    Log.d("Main","Negative Button clicked")
                }
                setView(dialogLayout)
                show()
            }
            return@OnLongClickListener true
        })*/
        textView.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            val inflater:LayoutInflater = layoutInflater
            val dialogLayout:View = inflater.inflate(R.layout.layout_dialog_profile_last_name,
                null)
            val editText = dialogLayout.findViewById<EditText>(R.id.et_last_name)
            with(builder) {
                setTitle("Enter Last Name: ")
                setPositiveButton("SAVE"){dialog, which ->
                    tv_last_name.text = editText.text.toString()
                    ref.child("profile").child("Last Name").setValue(editText.text.toString().trim())
                }
                setNegativeButton("CANCEL"){dialog, which ->
                    Log.d("Main","Negative Button clicked")
                }
                setView(dialogLayout)
                show()
            }
        }
    }*/

    private fun showEditPhoneNumDialog(textView: TextView){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        var user = FirebaseAuth.getInstance().currentUser
        var email = user?.email!!
        val end = user.email?.indexOf('@')!!
        val userName = email.substring(0, end)
        val ref: DatabaseReference = database.getReference(userName)
        /*textView.setOnLongClickListener(View.OnLongClickListener {
            val builder = AlertDialog.Builder(activity)
            val inflater:LayoutInflater = layoutInflater
            val dialogLayout:View = inflater.inflate(R.layout.layout_dialog_profile_phone,
                null)
            val editText = dialogLayout.findViewById<EditText>(R.id.et_phone_num)
            with(builder) {
                setTitle("Enter Phone Number:")
                setPositiveButton("SAVE"){dialog, which ->
                    tv_phone_no.text = editText.text.toString()
                    ref.child("profile").child("Phone Number").setValue(editText.text.toString())
                }
                setNegativeButton("CANCEL"){dialog, which ->
                    Log.d("Main","Negative Button clicked")
                }
                setView(dialogLayout)
                show()
            }
            return@OnLongClickListener true
        })*/
        textView.setOnClickListener{
            val builder = AlertDialog.Builder(activity)
            val inflater:LayoutInflater = layoutInflater
            val dialogLayout:View = inflater.inflate(R.layout.layout_dialog_profile_phone,
                null)
            val editText = dialogLayout.findViewById<EditText>(R.id.et_phone_num)
            with(builder) {
                setTitle("Enter Phone Number: ")
                setPositiveButton("SAVE"){dialog, which ->
                    tv_phone_no.text = editText.text.toString()
                    ref.child("profile").child("Phone Number").setValue(editText.text.toString())
                }
                setNegativeButton("CANCEL"){dialog, which ->
                    Log.d("Main","Negative Button clicked")
                }
                setView(dialogLayout)
                show()
            }
        }
    }
}