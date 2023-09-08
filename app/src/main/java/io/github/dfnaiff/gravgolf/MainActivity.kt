package io.github.dfnaiff.gravgolf

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewTreeObserver
import android.view.MotionEvent
import android.util.Log
import android.graphics.Color
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    private var ballLaunched = false
    lateinit var container: FrameLayout
    lateinit var ballView: BallView
    lateinit var arrowView: ArrowView
    lateinit var targetView: RectangleView
    lateinit var counterTextView: TextView  // Add this line to your class-level properties
    lateinit var animator: Animator
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var currentTouchX = 0f
    private var currentTouchY = 0f
    private var initialBallViewX: Float = 0f
    private var initialBallViewY: Float = 0f
    private var launchStrength: Float = 0.8f
    private var targetWidth: Float = 0.1f
    private var targetLeftX: Float = 0.0f
    private var targetRightX: Float = 0.0f
    private var n: Int = 1
    private var m: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.container)

        container.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    container.setBackgroundColor(Color.BLACK)

                    initialBallViewX = container.width / 2f
                    initialBallViewY = 9f / 10f * container.height

                    targetLeftX = container.width*(1f - targetWidth)/2f
                    targetRightX = container.width*(1f + targetWidth)/2f

                    initializeGame()
                }
            })
    }

    private fun initializeGame() {
        // Increment n (if not the first run)
        if (n > 1) {
            // Clear the existing views
            container.removeAllViews()
        }

        // Initialize ball, level, and other views
        initializeBall()
        initializeLevel()

        // Initialize TextView
        counterTextView = TextView(this)
        counterTextView.setTextColor(Color.GRAY)
        counterTextView.textSize = 24f  // Set your desired size
        counterTextView.alpha = 0.5f  // Set to fade (0 fully transparent, 1 fully opaque)

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 16, 0, 0)  // left, top, right, bottom
        counterTextView.layoutParams = params

        container.addView(counterTextView)

        // Update counter TextView
        updateCounter()
    }

    private fun initializeBall() {
        ballView = BallView(this, "GOLDEN")
        val params = FrameLayout.LayoutParams(50, 50)
        ballView.layoutParams = params
        container.addView(ballView)
        arrowView = ArrowView(this)
        arrowView.setArrowVisibility(false)
        val arrowparams = FrameLayout.LayoutParams(50, 50)
        arrowView.layoutParams = arrowparams
        container.addView(arrowView)

        targetView = RectangleView(this, "GOLDEN",
            container.width/2f,
            targetWidth/10f*container.width,
            container.width*targetWidth,
            targetWidth/5f*container.width)
        container.addView(targetView)
    }

    private fun initializeLevel() {
        val objectsList = mutableListOf<Triple<Float, Float, Float>>()
        for (i in 1..n) {
            val x = kotlin.random.Random.nextFloat() * (4 * container.width / 5f - container.width / 5f) + container.width / 5f
            val y = kotlin.random.Random.nextFloat() * (4 * container.height / 5f - container.height / 5f) + container.height / 5f
            val force = if (i % 2 == 0) 1f else -1f
            objectsList.add(Triple(x, y, force))
        }

        createLevel(objectsList)
        initializeIntegratorAndAnimator(objectsList)
    }

    private fun createLevel(objectsList: List<Triple<Float, Float, Float>>) {
        for (obj in objectsList) {
            val color = if (obj.third < 0) "RED" else "BLUE"
            val forcePointView = BallView(this, color)
            val params = FrameLayout.LayoutParams(50, 50)
            params.leftMargin = obj.first.toInt()
            params.topMargin = container.height - obj.second.toInt() // Cartesian to Android coords
            forcePointView.layoutParams = params
            container.addView(forcePointView)
        }
    }

    private fun initializeIntegratorAndAnimator(objectsList: List<Triple<Float, Float, Float>>) {
        val field = ForceField(objectsList)

        val integrator = LeapfrogIntegrator(
            x = initialBallViewX,
            y = container.height - initialBallViewY,
            vx = 0f,
            vy = 0f,
            dt = 0.016f,
            field = field
        )

        animator = Animator(ballView, container, integrator,
            ::resetGame,
            ::losingCondition,
            ::winningCondition)

        resetGame(integrator)

        container.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    handleActionDown(event)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    handleActionMove(event)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    handleActionUp(event, integrator)
                    true
                }
                else -> {
                    ballView.performClick()
                    false
                }
            }
        }
    }

    private fun updateCounter() {
        counterTextView.text = "$n/$m"
    }

    private fun handleActionDown(event: MotionEvent) {
        // Do things when ACTION_DOWN occurs
        arrowView.setArrowVisibility(true)
        initialTouchX = event.x
        initialTouchY = event.y
        arrowView.startX = ballView.x
        arrowView.startY = ballView.y
        arrowView.endX = ballView.x
        arrowView.endY = ballView.y
    }

    private fun handleActionMove(event: MotionEvent) {
        // Do things when ACTION_MOVE occurs
        currentTouchX = event.x
        currentTouchX = event.y
        val dx = currentTouchX - initialTouchX
        val dy = currentTouchY - initialTouchY
        arrowView.endX = arrowView.startX + dx
        arrowView.endY = arrowView.startY + dy
    }

    private fun handleActionUp(event: MotionEvent, integrator: LeapfrogIntegrator) {
        val vx = (event.x - ballView.x) * launchStrength
        val vy = -(event.y - ballView.y) * launchStrength
        integrator.vx = vx
        integrator.vy = vy
        ballLaunched = true
        animator.startAnimation(ballLaunched)

    }

    private fun resetGame(integrator: LeapfrogIntegrator) {

        // Reset integrator's state to initial conditions
        integrator.x = initialBallViewX
        integrator.y = container.height - initialBallViewY
        integrator.vx = 0f
        integrator.vy = 0f

        // Reset ballView's position
        ballView.x = integrator.x // Adjusting for center
        ballView.y = container.height - integrator.y // Adjusting for center

        // Reset touch coordinates and ball launch state
        initialTouchX = 0f
        initialTouchY = 0f
        currentTouchX = 0f
        currentTouchY = 0f
        ballLaunched = false
    }

    private fun losingCondition(integrator: LeapfrogIntegrator): Boolean {
        val condition = (integrator.x < 0 || integrator.x > container.width ||
                integrator.y < 0 || integrator.y > container.height)
        if (condition) {
            m++
            updateCounter()
        }
        return condition
    }

    private fun winningCondition(integrator: LeapfrogIntegrator): Boolean {
        val condition = (integrator.y > container.height &&
                integrator.x >= targetLeftX &&
                integrator.x <= targetRightX)
        if (condition) {
            n++  // Increment n upon winning
            m++
            initializeGame()  // Reinitialize the game
        }
        return condition
    }
}

class Animator(
    private val ballView: BallView,
    private val container: FrameLayout,
    private val integrator: LeapfrogIntegrator,
    private val resetGame: (LeapfrogIntegrator) -> Unit,
    private val losingCondition: (LeapfrogIntegrator) -> Boolean,
    private val winningCondition: (LeapfrogIntegrator) -> Boolean
) {
    private val handler = Handler(Looper.getMainLooper())

    fun startAnimation(ballLaunched: Boolean) {
        if (!ballLaunched) return

        val runnable = object : Runnable {
            override fun run() {
                integrator.step()
                ballView.x = integrator.x
                ballView.y = container.height - integrator.y

                if(winningCondition(integrator)){
                    handler.removeCallbacksAndMessages(null)
                    Log.d("Winning", "Won")
                    resetGame(integrator)
                    return

                }
                if(losingCondition(integrator)){
                    handler.removeCallbacksAndMessages(null)

                    resetGame(integrator)
                    return
                }

                handler.postDelayed(this, 16)
            }
        }

        handler.post(runnable)
    }
}
