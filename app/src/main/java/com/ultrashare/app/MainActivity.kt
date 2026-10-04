package com.ultrashare.app

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioTrack
import android.media.AudioManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    private val sampleRate = 44100
    private val frequency = 18000.0

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
            text = "Ultrasonic Offline Transfer"
            textSize = 18f
        }

        val sendButton = Button(this).apply {
            text = "SEND TEST SIGNAL"
        }

        val receiveButton = Button(this).apply {
            text = "LISTEN"
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
            Thread {
                sendUltrasonicSignal()
            }.start()
        }

        receiveButton.setOnClickListener {
            Thread {
                listenForSignal()
            }.start()
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

    private fun sendUltrasonicSignal() {

        runOnUiThread {
            statusText.text = "Status: Sending..."
        }

        val durationMs = 1000
        val sampleCount = sampleRate * durationMs / 1000

        val samples = ShortArray(sampleCount)

        for (i in samples.indices) {

            val time = i.toDouble() / sampleRate

            val value =
                sin(2.0 * Math.PI * frequency * time)

            samples[i] = (value * Short.MAX_VALUE * 0.5).toInt().toShort()
        }

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            samples.size * 2,
            AudioTrack.MODE_STATIC
        )

        audioTrack.write(samples, 0, samples.size)

        audioTrack.play()

        Thread.sleep(durationMs.toLong())

        audioTrack.stop()
        audioTrack.release()

        runOnUiThread {
            statusText.text = "Status: Signal sent"
        }
    }

    private fun listenForSignal() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            runOnUiThread {
                statusText.text = "Microphone permission required"
            }
            return
        }

        runOnUiThread {
            statusText.text = "Status: Listening..."
        }

        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val buffer = ShortArray(bufferSize)

        recorder.startRecording()

        val start = System.currentTimeMillis()
        var detected = false

        while (System.currentTimeMillis() - start < 5000) {

            val count = recorder.read(
                buffer,
                0,
                buffer.size
            )

            if (count > 0) {

                var energy = 0.0

                for (i in 0 until count) {
                    val value = buffer[i].toDouble()
                    energy += value * value
                }

                energy = sqrt(energy / count)

                if (energy > 1500) {
                    detected = true
                    break
                }
            }
        }

        recorder.stop()
        recorder.release()

        runOnUiThread {

            if (detected) {
                statusText.text =
                    "Status: Ultrasonic signal detected!"
            } else {
                statusText.text =
                    "Status: No signal detected"
            }
        }
    }
}
