package com.example.project10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class GameOverActivity : AppCompatActivity() {

    private lateinit var scoreText: MaterialTextView
    private lateinit var restartButton: MaterialButton
    private lateinit var menuButton: MaterialButton
    private lateinit var sharedPreferences: android.content.SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)

        sharedPreferences = getSharedPreferences("HIGH_SCORES", Context.MODE_PRIVATE)

        val finalScore = intent.getIntExtra("FINAL_SCORE", 0)

        findViewById<MaterialTextView>(R.id.gameover_TXT_score).text = "Score: $finalScore"

        saveScore(finalScore)

        findViewById<MaterialButton>(R.id.gameover_BTN_restart).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.gameover_BTN_menu).setOnClickListener {
            startActivity(Intent(this, OpeningActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.gameover_BTN_records).setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
            finish()
        }
    }

    private fun saveScore(score: Int) {
        val scores = mutableListOf<Int>()
        val scoreCount = sharedPreferences.getInt("SCORE_COUNT", 0)


        for (i in 0 until scoreCount) {
            scores.add(sharedPreferences.getInt("SCORE_$i", 0))
        }

        scores.add(score)

        scores.sortDescending()
        val topScores = scores.take(10)

        val editor = sharedPreferences.edit()
        editor.putInt("SCORE_COUNT", topScores.size)
        topScores.forEachIndexed { index, score ->
            editor.putInt("SCORE_$index", score)
        }
        editor.apply()
    }
}
