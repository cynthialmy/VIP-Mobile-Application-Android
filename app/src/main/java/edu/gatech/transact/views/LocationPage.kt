package edu.gatech.transact.views

import android.content.Intent
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.gatech.transact.R

class LocationPage : Fragment() {
    // deprecated class
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val intent = Intent(activity, GoogleMapActivity::class.java)
        startActivity(intent)
        return inflater.inflate(R.layout.fragment_location_page, container, false)
    }
}