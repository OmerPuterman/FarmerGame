package com.example.project10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var farmers: Array<ImageView>
    private lateinit var hearts: Array<ImageView>
    private lateinit var leftButton: MaterialButton
    private lateinit var rightButton: MaterialButton
    private var currentScore = 0
    private lateinit var scoreTextView: MaterialTextView
    private val matrixSize = 8
    private var currentHearts = 3
    private var currentPosition = 1

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var gameActive = true

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var tiltThreshold = 2.0f
    private var lastTiltTime: Long = 0
    private val tiltDelay = 300
    private val tiltDelayS = 1000


    private lateinit var carrotObstacles: Array<Array<ImageView>>
    private lateinit var dollarObstacles: Array<Array<ImageView>>
    private lateinit var hitSound: MediaPlayer
    private lateinit var coinSound: MediaPlayer

    private lateinit var delayTextView: MaterialTextView
    private var currentDelay = 500L
    private var minDelay = 100L
    private var maxDelay = 1000L
    private var delayStep = 50L

    private var lastTiltAdjustmentTime: Long = 0
    private val tiltAdjustmentInterval = 500

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val useTiltControls = intent.getBooleanExtra("USE_TILT_CONTROLS", true)
        scoreTextView = findViewById(R.id.textView_score)
        scoreTextView.text = "0"
        hitSound = MediaPlayer.create(this, R.raw.hit1)
        coinSound = MediaPlayer.create(this, R.raw.coin)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        delayTextView = findViewById(R.id.textView_delay)
        delayTextView.text = "Delay: $currentDelay ms"

        initializeHearts()
        initializeFarmers()
        initializeMatrix()
        hideAllObstacles()
        setupControls()
        startObstacleMovement()
        startObstacleMovement()

        if (useTiltControls) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            leftButton.visibility = View.GONE
            rightButton.visibility = View.GONE
        } else {
            sensorManager.unregisterListener(this)
            leftButton.visibility = View.VISIBLE
            rightButton.visibility = View.VISIBLE
        }


    }

    private fun playCoinSound() {
        coinSound.seekTo(0)
        coinSound.start()
    }

    private fun playHitSound() {
        hitSound.seekTo(0)
        hitSound.start()
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
            findViewById(R.id.main_IMG_farmer3),
            findViewById(R.id.main_IMG_farmer4),
            findViewById(R.id.main_IMG_farmer5)
        )
        updatePlayerPosition()
    }

    private fun initializeMatrix() {
        carrotObstacles = Array(matrixSize) { row ->
            Array(5) { col ->
                val id = resources.getIdentifier("obstacle_${row}_${col}", "id", packageName)
                findViewById<ImageView>(id)
            }
        }

        dollarObstacles = Array(matrixSize) { row ->
            Array(5) { col ->
                val id = resources.getIdentifier("obstacle_dollar_${row}_${col}", "id", packageName)
                findViewById<ImageView>(id)
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
            if (currentPosition < 4) {
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
        val bottomRow = matrixSize - 1
        val currentPosition = currentPosition

        if (carrotObstacles[bottomRow][currentPosition].visibility == View.VISIBLE) {
            if (currentHearts > 0) {
                hearts[--currentHearts].visibility = View.INVISIBLE
                playHitSound()
                triggerCrashEffects()
                carrotObstacles[bottomRow][currentPosition].visibility = View.INVISIBLE

                if (currentHearts == 0) {
                    gameActive = false
                    scope.launch(Dispatchers.Main) {
                        showGameOver()
                    }
                    return
                }
            }
        }

        if (dollarObstacles[bottomRow][currentPosition].visibility == View.VISIBLE) {
            playCoinSound()
            updateScore(5)
            Toast.makeText(this, "+5 points!", Toast.LENGTH_SHORT).show()
            dollarObstacles[bottomRow][currentPosition].visibility = View.INVISIBLE
        }
    }

    @SuppressLint("ServiceCast")
    private fun triggerCrashEffects() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        Toast.makeText(this, "ewww carrot, blah!", Toast.LENGTH_SHORT).show()

    }
    private fun showGameOver() {
        if (!gameActive) {
            val intent = Intent(this, GameOverActivity::class.java).apply {
                putExtra("FINAL_SCORE", currentScore)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun startObstacleMovement() = scope.launch {
        while (gameActive) {
            moveObstaclesDown()
            spawnNewObstacle()
            checkCollision()
            delay(currentDelay)
        }
    }

    private fun moveObstaclesDown() {
        for (i in matrixSize - 1 downTo 1) {
            for (j in 0 until 5) {
                if (i == matrixSize - 1) {
                    if (carrotObstacles[i][j].visibility == View.VISIBLE && j != currentPosition) {
                        updateScore(1)
                    }
                }

                carrotObstacles[i][j].visibility = carrotObstacles[i - 1][j].visibility
                carrotObstacles[i][j].setImageDrawable(carrotObstacles[i - 1][j].drawable)

                dollarObstacles[i][j].visibility = dollarObstacles[i - 1][j].visibility
                dollarObstacles[i][j].setImageDrawable(dollarObstacles[i - 1][j].drawable)
            }
        }

        for (j in 0 until 5) {
            carrotObstacles[0][j].visibility = View.INVISIBLE
            dollarObstacles[0][j].visibility = View.INVISIBLE
        }
    }

    private fun spawnNewObstacle() {
        val newPattern = generateNewPattern()
        val shouldSpawnDollar = (1..5).random() == 4  // 20% chance to spawn dollar

        for (j in 0 until 5) {
            carrotObstacles[0][j].visibility = View.INVISIBLE
            dollarObstacles[0][j].visibility = View.INVISIBLE
        }

        if (newPattern in 0..4) {
            carrotObstacles[0][newPattern].apply {
                visibility = View.VISIBLE
                setImageResource(R.drawable.carrot)
            }
        }

        if (shouldSpawnDollar) {
            val dollarPosition = generateDollarPosition(newPattern)
            if (dollarPosition != -1) {
                dollarObstacles[0][dollarPosition].apply {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.dollar)
                }
            }
        }
    }

    private fun generateDollarPosition(carrotPosition: Int): Int {

        val availablePositions = (0..4).filter { it != carrotPosition }.toList()
        return if (availablePositions.isNotEmpty()) {
            availablePositions.random()
        } else {
            -1
        }
    }
    private fun generateNewPattern(): Int {
        return (0..4).random()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameActive = false
        scope.cancel()
        hitSound.release()
        coinSound.release()
    }

    @SuppressLint("DefaultLocale")
    private fun updateScore(points: Int) {
        currentScore += points
        scoreTextView.text = currentScore.toString()
    }


    private fun hideAllObstacles() {
        for (i in 0 until matrixSize) {
            for (j in 0 until 5) {
                carrotObstacles[i][j].visibility = View.INVISIBLE
                dollarObstacles[i][j].visibility = View.INVISIBLE
            }
        }
    }


    private fun adjustDelay(amount: Long) {
        currentDelay = (currentDelay + amount).coerceIn(minDelay, maxDelay)
        delayTextView.text = "Delay: $currentDelay ms"
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        if (intent.getBooleanExtra("USE_TILT_CONTROLS", false)) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val tiltX = event.values[0]
            val tiltY = event.values[1]
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastTiltAdjustmentTime >= tiltDelayS) {
                when {
                    tiltY > 2.0f -> {
                        adjustDelay(-delayStep)
                    }
                    tiltY < -2.0f -> {
                        adjustDelay(delayStep)
                    }
                }
                lastTiltAdjustmentTime = currentTime
            }

            if (currentTime - lastTiltTime >= tiltDelay) {
                when {
                    tiltX < -tiltThreshold -> {
                        if (currentPosition < 4) {
                            currentPosition++
                            updatePlayerPosition()
                        }
                    }
                    tiltX > tiltThreshold -> {
                        if (currentPosition > 0) {
                            currentPosition--
                            updatePlayerPosition()
                        }
                    }
                }
                lastTiltTime = currentTime
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}



