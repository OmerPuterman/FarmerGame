package com.example.project10

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.gms.maps.GoogleMap


class RecordsActivity : AppCompatActivity() {
    private lateinit var mMap: GoogleMap
    private lateinit var scoresLayout: LinearLayout
    private lateinit var backButton: MaterialButton
    private val sharedPreferences by lazy { getSharedPreferences("HIGH_SCORES", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        scoresLayout = findViewById(R.id.records_layout_scores)
        backButton = findViewById(R.id.records_BTN_back)

        displayScores()

        backButton.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayScores() {
        val scoreCount = sharedPreferences.getInt("SCORE_COUNT", 0)
        val scores = mutableListOf<Int>()
        for (i in 0 until scoreCount) {
            val score = sharedPreferences.getInt("SCORE_$i", 0)
            scores.add(score)
        }

        scores.forEachIndexed { index, score ->
            val scoreTextView = TextView(this).apply {
                text = "${index + 1}. Score  -  $score"
                textSize = 18f
                setPadding(10, 10, 10, 10)
                setTextColor(resources.getColor(R.color.white, null))
            }
            scoresLayout.addView(scoreTextView)
        }
    }

}







