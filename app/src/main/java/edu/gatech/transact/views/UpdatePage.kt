package edu.gatech.transact.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.gatech.transact.R
import kotlinx.android.synthetic.main.fragment_update_page.view.*

class UpdatePage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater!!.inflate(
            R.layout.fragment_update_page,
            container, false
        )

        view.survey1.setOnClickListener {
            activity?.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(
                "https://gatech.co1.qualtrics.com/jfe/form/SV_6PuW0r9cQ3XyavI")))
        }
//        val scrollView = R.id.scrollView
//        getActionbar
//        view.setBackgroundColor(Color.argb(1, 18, 155, 76))
        return view
    }
}