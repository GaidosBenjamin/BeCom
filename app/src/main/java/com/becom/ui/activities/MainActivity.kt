package com.becom.ui.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.becom.R
import com.becom.utils.views.BCTextView
import com.becom.utils.Constants

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences(Constants.BECOM_PREFERENCES, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "")!!

        val textView: BCTextView = findViewById(R.id.tv_main)
        textView.text = username
    }
}