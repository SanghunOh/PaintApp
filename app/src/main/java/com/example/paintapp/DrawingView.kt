package com.example.paintapp

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import java.lang.Math.abs

class DrawingView(context: Context, attributeSet: AttributeSet) : View(context) {
    init {
        setLayerType(FrameLayout.LAYER_TYPE_HARDWARE, null)
    }

    companion object {
        private const val ERASER_SIZE = 20F
        private const val TOUCH_TOLERANCE = 2f

        private const val SPEN_ACTION_DOWN = 211
        private const val SPEN_ACTION_UP = 212
        private const val SPEN_ACTION_MOVE = 213
    }

    private var strokePoint = PointF(0F, 0F)
    private val strokePath = Path()

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = convertDpToPixel(5F)
    }

    private val eraserCirclePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = convertDpToPixel(5F)
    }

    private val eraserPaint = Paint().apply {
        isAntiAlias = true;
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var isMoving = false
    private var isErasing = false

    private var lastEraserPositionX = 0F
    private var lastEraserPositionY = 0F

    private var scribeCanvasBitmap: Bitmap? = null

    private lateinit var scribeCanvas: Canvas

    private var canvasWidth = -1
    private var canvasHeight = -1

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        canvasWidth = w;
        canvasHeight = h
        scribeCanvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)

        scribeCanvasBitmap?.let {
            scribeCanvas = Canvas(it)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        scribeCanvasBitmap?.let {
            canvas?.drawBitmap(it, 0F, 0F, strokePaint)

            if (isErasing && isMoving) {
                canvas?.drawCircle(lastEraserPositionX, lastEraserPositionY, ERASER_SIZE, eraserCirclePaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return false

        if (event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS)
                return false
        val touchX = event.x
        val touchY = event.y
        if (event.buttonState == MotionEvent.BUTTON_STYLUS_PRIMARY) {
            if (!isMoving) {
                isErasing = !isErasing
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN, SPEN_ACTION_DOWN -> {
                isMoving = true

                strokePath.reset()
                strokePath.moveTo(touchX, touchY)

                if (isErasing) {
                    scribeCanvas.drawCircle(touchX, touchY, ERASER_SIZE, eraserPaint)

                    lastEraserPositionX = touchX
                    lastEraserPositionY = touchY
                } else {
                    strokePoint = PointF(touchX, touchY)
                }

                invalidate()
            }

            MotionEvent.ACTION_MOVE, SPEN_ACTION_MOVE -> {
                if (isErasing) {
                    strokePath.addCircle(touchX, touchY, ERASER_SIZE, Path.Direction.CW)

                    lastEraserPositionY = touchY
                    lastEraserPositionX = touchX
                } else {
                    val dx = kotlin.math.abs(touchX - strokePoint.x)
                    val dy = abs(touchY - strokePoint.y)

                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        strokePath.quadTo(
                            strokePoint.x,
                            strokePoint.y,
                            (touchX + strokePoint.x) / 2,
                            (touchY + strokePoint.y) / 2
                        )

                        strokePoint = PointF(touchX, touchY)
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP, SPEN_ACTION_UP -> {
                if (isErasing) {
                    scribeCanvas.drawPath(strokePath, eraserPaint)
                } else {
                    scribeCanvas.drawPath(strokePath, strokePaint)
                }

                isMoving = false
                isErasing = false

                lastEraserPositionX = 0F
                lastEraserPositionY = 0F

                strokePath.reset()

                invalidate()
            }
        }
        return true
    }

    private fun convertDpToPixel(dp: Float): Float {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics

            dp* (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics

            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}