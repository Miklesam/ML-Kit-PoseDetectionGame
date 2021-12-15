package com.onelinegaming.posedetectiondemo.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.onelinegaming.posedetectiondemo.R
import com.onelinegaming.posedetectiondemo.fragments.GameFragment.Companion.RESULT
import kotlinx.android.synthetic.main.fragment_results.*

class ResultsFragment : Fragment(R.layout.fragment_results) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_bttn.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_menuFragment)
        }
        time_result.text = requireArguments().getString(RESULT)
    }

}