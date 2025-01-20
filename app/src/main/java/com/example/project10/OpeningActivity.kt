package com.example.project10

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OpeningActivity : AppCompatActivity() {

    private lateinit var startButton: MaterialButton
    private lateinit var quitButton: MaterialButton
    private lateinit var controlGroup: RadioGroup

    private var useTiltControls: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)

        startButton = findViewById(R.id.opening_BTN_start)
        quitButton = findViewById(R.id.opening_BTN_quit)
        controlGroup = findViewById(R.id.opening_RADIO_group)


        controlGroup.setOnCheckedChangeListener { _, checkedId ->
            useTiltControls = checkedId == R.id.opening_RADIO_tilt
        }

        startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USE_TILT_CONTROLS", useTiltControls)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.opening_BTN_scores).setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
        }

        quitButton.setOnClickListener {
            finish()
        }
    }
}
