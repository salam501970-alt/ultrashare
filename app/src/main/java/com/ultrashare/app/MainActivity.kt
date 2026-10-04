package com.ultrashare.app

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "UltraShare"
            textSize = 32f
        }

        val subtitle = TextView(this).apply {
            text = "Ultrasonic Offline File Transfer"
            textSize = 18f
        }

        val sendButton = Button(this).apply {
            text = "SEND"
        }

        val receiveButton = Button(this).apply {
            text = "RECEIVE"
        }

        statusText = TextView(this).apply {
            text = "Status: Ready"
            textSize = 18f
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(sendButton)
        layout.addView(receiveButton)
        layout.addView(statusText)

        setContentView(layout)

        requestMicrophonePermission()

        sendButton.setOnClickListener {
            statusText.text = "Status: Send mode"
        }

        receiveButton.setOnClickListener {
            statusText.text = "Status: Receive mode"
        }
    }

    private fun requestMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }
}
