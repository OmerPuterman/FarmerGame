package com.example.project10

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var matrixLayout: LinearLayout
    private lateinit var farmers: Array<ImageView>
    private lateinit var hearts: Array<ImageView>
    private lateinit var leftButton: MaterialButton
    private lateinit var rightButton: MaterialButton

    private val matrixSize = 5
    private var currentHearts = 3
    private var currentPosition = 1
    private lateinit var obstacles: Array<Array<ImageView>>

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var gameActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeHearts()
        initializeFarmers()
        initializeMatrix()
        setupControls()
        startObstacleMovement()
    }

    private fun initializeHearts() {
        hearts = arrayOf(
            findViewById(R.id.main_IMG_heart1),
            findViewById(R.id.main_IMG_heart2),
            findViewById(R.id.main_IMG_heart3)
        )
    }

    private fun initializeFarmers() {
        farmers = arrayOf(
            findViewById(R.id.main_IMG_farmer1),
            findViewById(R.id.main_IMG_farmer2),
            findViewById(R.id.main_IMG_farmer3)
        )
        updatePlayerPosition()
    }

    private fun initializeMatrix() {
        obstacles = Array(matrixSize) { Array(3) { ImageView(this) } }
        val matrixContainer = findViewById<LinearLayout>(R.id.obstaclesLayout)
        for (i in 0 until matrixSize) {
            val row = matrixContainer.getChildAt(i) as LinearLayout
            for (j in 0 until 3) {
                obstacles[i][j] = row.getChildAt(j) as ImageView
                obstacles[i][j].visibility = View.INVISIBLE
            }
        }
    }

    private fun setupControls() {
        leftButton = findViewById(R.id.button1)
        rightButton = findViewById(R.id.button2)

        leftButton.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                updatePlayerPosition()
            }
        }

        rightButton.setOnClickListener {
            if (currentPosition < 2) {
                currentPosition++
                updatePlayerPosition()
            }
        }
    }

    private fun updatePlayerPosition() {
        for (i in farmers.indices) {
            farmers[i].visibility = View.INVISIBLE
        }
        farmers[currentPosition].visibility = View.VISIBLE
    }

    private fun checkCollision() {
        if (obstacles[matrixSize - 1][currentPosition].visibility == View.VISIBLE) {
            // Collision detected
            if (currentHearts > 0) {
                hearts[--currentHearts].visibility = View.INVISIBLE
            }
            triggerCrashEffects()
        }
    }

    @SuppressLint("ServiceCast")
    private fun triggerCrashEffects() {
        // Trigger vibration (already fixed earlier)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))

        // Show crash toast
        Toast.makeText(this, "ewww carrot, blah!", Toast.LENGTH_SHORT).show()
    }

    private fun startObstacleMovement() = scope.launch {
        while (gameActive) {
            moveObstaclesDown()
            spawnNewObstacle()
            checkCollision()
            delay(500)
        }
    }

    private fun moveObstaclesDown() {
        for (i in matrixSize - 1 downTo 1) {
            for (j in 0 until 3) {
                obstacles[i][j].visibility = obstacles[i - 1][j].visibility
            }
        }
        for (j in 0 until 3) {
            obstacles[0][j].visibility = View.INVISIBLE
        }
    }

    private fun spawnNewObstacle() {
        val newPattern = generateNewPattern()
        for (j in 0 until 3) {
            obstacles[0][j].visibility = if (j == newPattern) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun generateNewPattern(): Int {
        return (0..2).random()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
