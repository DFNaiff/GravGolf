package io.github.dfnaiff.gravgolf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.view.View
import android.util.Log

class BallView(context: Context, color: String) : View(context) {
    private val paint = Paint()

    init {
        paint.color = when (color) {
            "RED" -> Color.RED
            "BLUE" -> Color.BLUE
            "GREEN" -> Color.GREEN
            "GOLDEN" -> Color.YELLOW // Assuming GOLDEN refers to yellow
            else -> Color.GRAY // Default color
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2.0f, height / 2.0f, 25f, paint)
    }
}

class RectangleView(
    context: Context,
    color: String,
    private val cx: Float,
    private val cy: Float,
    private val lx: Float,
    private val ly: Float
) : View(context) {

    private val paint = Paint()

    init {
        paint.color = when (color) {
            "RED" -> Color.RED
            "BLUE" -> Color.BLUE
            "GREEN" -> Color.GREEN
            "GOLDEN" -> Color.YELLOW // Assuming GOLDEN refers to yellow
            else -> Color.GRAY // Default color
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = cx - lx / 2
        val top = cy - ly / 2
        val right = cx + lx / 2
        val bottom = cy + ly / 2
        canvas.drawRect(left, top, right, bottom, paint)
    }
}

class ArrowView(context: Context) : View(context) {
    private val paint = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 100f
    }

    var startX: Float = 0f
    var startY: Float = 0f
    var endX: Float = 0f
    var endY: Float = 0f

    fun setArrow(startX: Float, startY: Float, endX: Float, endY: Float) {
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        Log.d("ArrowDraw", "Here ${startX}, ${startY}, ${endX}, ${endY}")
        super.onDraw(canvas)
        drawArrow(canvas, startX, startY, endX, endY)
    }

    private fun drawArrow(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
        val dx = x2 - x1
        val dy = y2 - y1

        // Calculate angle of the arrow
        val angle = kotlin.math.atan2(dy, dx)

        // Calculate length of the arrow
        val length = kotlin.math.sqrt(dx * dx + dy * dy)

        // Define maximum length for the arrow
        val maxLength = 100.0

        // Calculate scaling factor
        val factor = kotlin.math.min(maxLength / length, 1.0)

        // Calculate new end coordinates based on the scaling factor
        val x2New = x1 + dx * factor
        val y2New = y1 + dy * factor

        // Draw the arrow line
        canvas.drawLine(0f, 0f, dx, dy, paint)

    }

    fun setArrowVisibility(isVisible: Boolean) {
        visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.INVISIBLE // or View.GONE based on your requirement
        }
    }
}