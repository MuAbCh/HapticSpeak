package com.example.hapticspeak

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.TextView
import android.widget.ToggleButton
import com.example.hapticspeak.databinding.ActivityMainBinding
import java.util.Locale
import android.graphics.Typeface
import android.graphics.Paint
import android.widget.Button
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isSpeechDetectionOn: Boolean = false
    private lateinit var tts: TextToSpeech

    private val DOT_THRESHOLD = 800 // Adjust as needed
    private val TIME_FRAME = 1000 // Adjust as needed
    private var tapCount = 0
    private var lastTapTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(recognitionListener)

        // Initialize TextToSpeech
        tts = TextToSpeech(this, OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
            }
        })

        // Request RECORD_AUDIO permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }

        // Setup toggle button click listener
        binding.speechToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                startListening()
            } else {
                stopListening()
            }
        }

        // Set title TextView properties
        val titleTextView: TextView = findViewById(R.id.titleTextView)
        titleTextView.text = "Haptic Speak"
        titleTextView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        titleTextView.textSize = 37f
        titleTextView.setTypeface(null, Typeface.BOLD)
        titleTextView.paintFlags = titleTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Setup Morse code button click listener
        binding.transmitMorseButton.setOnClickListener {
            handleTap()
        }
    }

    private fun startListening() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.startListening(recognizerIntent)
        isSpeechDetectionOn = true
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        isSpeechDetectionOn = false
    }

    private fun handleTap() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastTap = currentTime - lastTapTime
        if (timeSinceLastTap > TIME_FRAME) {
            // Reset tap count if it's been too long since the last tap
            tapCount = 0
        }
        tapCount++
        translateTapToMorse(tapCount)
        lastTapTime = currentTime
    }

    private var morseCodeBuffer = StringBuilder()

    private fun translateTapToMorse(tapCount: Int) {
        when (tapCount) {
            1 -> {
                // Handle dot (.) in Morse code
                // Example: Add dot to Morse code buffer
                morseCodeBuffer.append('.')
            }
            2 -> {
                // Handle dash (-) in Morse code
                // Example: Add dash to Morse code buffer
                morseCodeBuffer.append('-')
            }
            3 -> {
                // Handle space ( ) in Morse code
                // Example: Add space to Morse code buffer
                // Translate accumulated Morse code to English and speak it
                speakMorseCode(translateMorseToEnglish(morseCodeBuffer.toString()))
                // Clear Morse code buffer for next word
                morseCodeBuffer.clear()
            }
        }
    }

    // Function to speak Morse code translation
    private fun speakMorseCode(morseCode: String) {
        // Translate Morse code to English alphabets
        val englishText = translateMorseToEnglish(morseCode)
        // Check if text-to-speech is initialized
        if (::tts.isInitialized) {
            // Speak the translated English alphabets
            tts.speak(englishText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun translateMorseToEnglish(morseCode: String): String {
        val morseToEnglishMap = mapOf(
            ".-" to "A", "-..." to "B", "-.-." to "C", "-.." to "D", "." to "E",
            "..-." to "F", "--." to "G", "...." to "H", ".." to "I", ".---" to "J",
            "-.-" to "K", ".-.." to "L", "--" to "M", "-." to "N", "---" to "O",
            ".--." to "P", "--.-" to "Q", ".-." to "R", "..." to "S", "-" to "T",
            "..-" to "U", "...-" to "V", ".--" to "W", "-..-" to "X", "-.--" to "Y",
            "--.." to "Z",
            "-----" to "0", ".----" to "1", "..---" to "2", "...--" to "3", "....-" to "4",
            "....." to "5", "-...." to "6", "--..." to "7", "---.." to "8", "----." to "9",
            "/" to " " // Use '/' as a word separator in Morse code
        )

        val words = morseCode.split(" / ")
        val englishStringBuilder = StringBuilder()

        for (word in words) {
            val characters = word.split(" ")
            for (character in characters) {
                val englishChar = morseToEnglishMap[character]
                englishStringBuilder.append(englishChar ?: "")
            }
            englishStringBuilder.append(" ") // Add space between words
        }

        return englishStringBuilder.toString().trim()
    }


    private val recognitionListener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.isNotEmpty()) {
                val detectedSpeech = matches[0]
                // Display the detected speech in the TextView
                binding.speechTextView.text = "Detected Speech: $detectedSpeech"
                // Convert detectedSpeech to Morse code
                val morseCode = convertToMorse(detectedSpeech)
                // Display Morse code under the start button
                binding.morseCodeTextView.text = "Morse Code: $morseCode"
            }
        }

        private fun convertToMorse(detectedSpeech: String): String {
            val morseCodeMap = mapOf(
                'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
                'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
                'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
                'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
                'Y' to "-.--", 'Z' to "--..",
                '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
                '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
                ' ' to "/" // Use '/' as a word separator in Morse code
            )
            val morseCodeBuilder = StringBuilder()
            for (char in detectedSpeech.toUpperCase()) {
                val morse = morseCodeMap[char]
                morseCodeBuilder.append(morse ?: "")
                morseCodeBuilder.append(" ") // Add space between characters
            }
            return morseCodeBuilder.toString()
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release SpeechRecognizer resources
        speechRecognizer.destroy()
        // Release TextToSpeech resources
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 101
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
