package NWUP.com.Weltuhr

import NWUP.com.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_weltuhr.*
import kotlinx.android.synthetic.main.fragment_weltuhr.view.*
import java.text.DateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */

//TODO: Variablen-Namen in XML einheitlicher machen

class WeltuhrFragment : Fragment() {

    private fun setCurrentDate() {
        val calendar = Calendar.getInstance()
        val currentDate =  DateFormat.getDateInstance().format(calendar.getTime())

        val tz: TimeZone = TimeZone.getDefault()

        CurrentTimeZone.text = tz.getDisplayName()
        CurrentDate.text = currentDate
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_weltuhr, container, false)

        //val boris = TextView("Blah")
        //binding.ViewLayout.addView(boris)

        binding.gettopicktimezone.setOnClickListener {
            val intent = Intent(context, PickTimeZone::class.java)
            startActivity((intent))
        }





        return binding
    }

    override fun onStart() {
        super.onStart()
        setCurrentDate()
    }


}
