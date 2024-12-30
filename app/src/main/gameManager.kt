class gameManager {

    private val matrixSize: Int = 5,
    private val lanes: Int = 3,
    private val updateDelay: Long = 1000L
    ) {
        private var gameActive = true
        private var score = 0
        private lateinit var obstacleCallbacks: ObstacleCallbacks
        private val scope = CoroutineScope(Dispatchers.Main + Job())
        private var currentMatrix = Array(matrixSize) { Array(lanes) { false } }

        interface ObstacleCallbacks {
            fun onObstacleMove(row: Int, col: Int, isVisible: Boolean)
            fun onGameOver()
            fun onScoreUpdate(score: Int)
        }

        fun setCallbacks(callbacks: ObstacleCallbacks) {
            obstacleCallbacks = callbacks
        }

        fun startGame() = scope.launch {
            var currentPattern = generatePattern()

            while (gameActive) {
                moveDown()
                addNewRow(currentPattern)
                currentPattern = generatePattern()
                delay(updateDelay)
            }
        }

        private fun moveDown() {
            for (i in matrixSize - 1 downTo 1) {
                for (j in 0 until lanes) {
                    currentMatrix[i][j] = currentMatrix[i-1][j]
                    obstacleCallbacks.onObstacleMove(i, j, currentMatrix[i][j])
                }
            }
        }

        private fun addNewRow(pattern: Int) {
            for (j in 0 until lanes) {
                currentMatrix[0][j] = j == pattern
                obstacleCallbacks.onObstacleMove(0, j, currentMatrix[0][j])
            }
        }

        private fun generatePattern() = Random.nextInt(0, lanes)

        fun checkCollision(playerPosition: Int): Boolean {
            return currentMatrix[matrixSize-1][playerPosition]
        }

        fun stopGame() {
            gameActive = false
            scope.cancel()
        }

        fun increaseScore() {
            score++
            obstacleCallbacks.onScoreUpdate(score)
        }
    }
}