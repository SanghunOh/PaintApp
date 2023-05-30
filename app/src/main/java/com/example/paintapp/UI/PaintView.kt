package com.example.paintapp.UI

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.paintapp.CustomEventListener
import com.example.paintapp.CustomPath
import com.example.paintapp.MainActivity.Companion.path
import com.example.paintapp.MainActivity.Companion.strokePaint
import com.example.paintapp.R
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min


class PaintView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet){
    var params : ViewGroup.LayoutParams? = null

    var pathList = ArrayList<CustomPath>()
    var currentBrush = Color.BLACK
    var selectMode = false
    var displaySelectBox = false
    var selectedStroke = ArrayList<Int>()
    var isSelect = false

    private var customEventListener: CustomEventListener? = null

    init {
        strokePaint.apply {
            isAntiAlias = true
            isDither = true
            color = currentBrush
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = convertDpToPixel(3F)
        }

        params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    private var tmpPath = ArrayList<Path>()
    private var minX = 0F
    private var minY = 0F
    private var maxX = 0F
    private var maxY = 0F

    private val ERASER_SIZE = 20F
    private val TOUCH_TOLERANCE = 2f
    private val POINTS_TOLERANCE = 10f

    private val SPEN_ACTION_DOWN = 211
    private val SPEN_ACTION_UP = 212
    private val SPEN_ACTION_MOVE = 213

    private val selectBrushPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.argb(255, 255, 241, 115)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = convertDpToPixel(5F)
    }

    private val selectedStrokePaint = Paint().apply {
        val dashPath = DashPathEffect(floatArrayOf(5F, 30F), 0F)
        isAntiAlias = true
        isDither = true
        color = Color.argb(255, 255, 241, 115)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.SQUARE
        strokeWidth = convertDpToPixel(4F)
        pathEffect = dashPath
    }

//    private val eraserCirclePaint = Paint().apply {
//        isAntiAlias = true
//        style = Paint.Style.STROKE
//        strokeWidth = convertDpToPixel(5F)
//    }
//
//    private val eraserPaint = Paint().apply {
//        isAntiAlias = true;
//        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
//    }

    private var strokePoint = PointF(0F, 0F)

    private var isMoving = false
    private var isErasing = false

    private var firstSelectPointX = 0F
    private var firstSelectPointY = 0F

    private var lastSelectPointX = 0F
    private var lastSelectPointY = 0F

    private var tmpPathPoints = ArrayList<PointF>()

    private var topLeft = PointF()
    private var bottomRight = PointF()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        if (event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS)
            return false

        when(event.action) {
            MotionEvent.ACTION_DOWN, SPEN_ACTION_DOWN -> {
                if (selectMode) {
                    isSelect = false
                    selectedStroke.clear()
                    displaySelectBox = true
                    firstSelectPointX = x
                    firstSelectPointY = y
                }
                else {
                    path.moveTo(x, y)
                    minX = x
                    maxX = x
                    minY = y
                    maxY = y
                    tmpPathPoints.add(PointF(x, y))

                    strokePoint = PointF(x, y)
                }
                return true
            }
            MotionEvent.ACTION_MOVE, SPEN_ACTION_MOVE -> {
                if (selectMode) {
                    lastSelectPointX = x
                    lastSelectPointY = y
                }
                else {
                    minX = min(minX, x)
                    minY = min(minY, y)
                    maxX = max(maxX, x)
                    maxY = max(maxY, y)

                    var dx = kotlin.math.abs((x - tmpPathPoints.last().x))
                    var dy = kotlin.math.abs((y - tmpPathPoints.last().y))

                    if (dx >= POINTS_TOLERANCE || dy >= POINTS_TOLERANCE) {
                        tmpPathPoints.add(PointF(x, y))
                    }

                    dx = kotlin.math.abs((x - strokePoint.x))
                    dy = kotlin.math.abs((y - strokePoint.y))

                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        path.quadTo(
                            strokePoint.x,
                            strokePoint.y,
                            (x + strokePoint.x) / 2,
                            (y + strokePoint.y) / 2
                        )

                        strokePoint = PointF(x, y)
                    }
                    tmpPath.add(path)
                }
            }
            MotionEvent.ACTION_UP, SPEN_ACTION_UP -> {
                if (selectMode) {
                    lastSelectPointX = x
                    lastSelectPointY = y
                    if (lastSelectPointX < firstSelectPointX) {
                        firstSelectPointX = lastSelectPointX.also {
                            lastSelectPointX = firstSelectPointX
                        }
                    }
                    if (lastSelectPointY < firstSelectPointY) {
                        firstSelectPointY = lastSelectPointY.also {
                            lastSelectPointY = firstSelectPointY
                        }
                    }

                    for (i in pathList.indices) {
                        if (lastSelectPointX < pathList[i].minX || firstSelectPointX > pathList[i].maxX)
                            continue
                        if (lastSelectPointY < pathList[i].minY || firstSelectPointY > pathList[i].maxY)
                            continue
                        for(j in pathList[i].points.indices) {
                            if (firstSelectPointX < pathList[i].points[j].x && pathList[i].points[j].x < lastSelectPointX &&
                                firstSelectPointY < pathList[i].points[j].y && pathList[i].points[j].y < lastSelectPointY) {
                                selectedStroke.add(i)
                                displaySelectBox = false
                                isSelect = true
                                break
                            }
                        }
                    }
                    if (selectedStroke.size > 0) {
                        var idx: Int = selectedStroke[0]
                        if (idx < pathList.size) {
                            topLeft = PointF(pathList[idx].minX, pathList[idx].minY)
                            bottomRight = PointF(pathList[idx].maxX, pathList[idx].maxY)

                            for (i in 1 until selectedStroke.size) {
                                idx = selectedStroke[i]
                                topLeft.x = min(topLeft.x, pathList[idx].minX)
                                topLeft.y = min(topLeft.y, pathList[idx].minY)
                                bottomRight.x = max(bottomRight.x, pathList[idx].maxX)
                                bottomRight.y = max(bottomRight.y, pathList[idx].maxY)
                            }
                            isSelect = true
                        }
                        // TODO : create popup for query chatgpt
                        triggerOnStrokeSelected(PointF(topLeft.x, bottomRight.y))
                    }
                }
                else {
                    tmpPathPoints.add(PointF(x, y))
                    pathList.add(
                        CustomPath(
                            currentBrush, path, minX, minY,
                        maxX, maxY, ArrayList(tmpPathPoints))
                    )


                    triggerOnPathAdded(
                        CustomPath(
                            currentBrush, path, minX, minY,
                        maxX, maxY, tmpPathPoints.toList())
                    )
                    tmpPath.clear()
                    tmpPathPoints.clear()
                }
            }
            else -> return false
        }

        invalidate() // important inform non-ui thread that some changes have been done
        return false;
    }

    override fun onDraw(canvas: Canvas) {
        if (selectMode) {
            for(i: Int in 0 until pathList.size) {
                strokePaint.color = pathList[i].color
                canvas.drawPath(pathList[i].path, strokePaint)
            }
            if (displaySelectBox) {
                canvas.drawRect(
                    firstSelectPointX, firstSelectPointY,
                    lastSelectPointX, lastSelectPointY, selectBrushPaint
                )
            }
            if (isSelect) {
                canvas.drawRect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y, selectedStrokePaint)
            }
        }
        else {
            for(i in pathList.indices){
                strokePaint.color = pathList[i].color
                canvas.drawPath(pathList[i].path, strokePaint)
            }
            for (i in tmpPath.indices) {
                strokePaint.color = currentBrush
                canvas.drawPath(tmpPath[i], strokePaint)
            }
        }
        invalidate()
    }

    fun setCustomEventListener(listener: CustomEventListener) {
        customEventListener = listener
    }

    // Custom Event를 발생시키는 함수
    private fun triggerOnPathAdded(path: CustomPath) {
        customEventListener?.onPathAdded(path)
    }

    private fun triggerOnStrokeSelected(pos: PointF) {
        customEventListener?.onStrokeSelected(pos);
    }

    fun saveToPNG(): Bitmap {
        val min = PointF(999999F, 999999F)
        val max = PointF(0F, 0F)
        val display = context.resources.displayMetrics
        val deviceWidth = display.widthPixels
        val deviceHeight = display.heightPixels


        val view = View(context)
        Log.d("IMAGE", "$deviceHeight, $deviceWidth")
        val bitmap = Bitmap.createBitmap(deviceWidth, deviceHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val paint = Paint(strokePaint)
        canvas.drawColor(Color.WHITE)

        paint.color = Color.BLACK
        paint.strokeWidth = 1F

        for(i: Int in 0 until selectedStroke.size) {
            min.x = min(pathList[selectedStroke[i]].minX, min.x)
            min.y = min(pathList[selectedStroke[i]].minY, min.y)

            max.x = max(pathList[selectedStroke[i]].maxX, max.x)
            max.y = max(pathList[selectedStroke[i]].maxY, max.y)
            canvas.drawPath(pathList[selectedStroke[i]].path, paint)
        }

        view.draw(canvas)

        return Bitmap.createBitmap(bitmap, (min.x - 100).toInt(), (min.y - 100).toInt(), (max.x - min.x + 200).toInt(), (max.y - min.y + 200).toInt())
    }

    fun saveBitmapToJPG(bitmap: Bitmap): File {
        val pictureFileDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(
            R.string.app_name))
        val pictureFile = File(pictureFileDir.path + System.currentTimeMillis() + ".jpg")

        if (!pictureFileDir.exists()) {
            pictureFileDir.mkdirs()
        }
        var fos: FileOutputStream? = null
        fos = FileOutputStream(pictureFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        return pictureFile
    }
    private fun getSelectedCanvas(bitmap: Bitmap): Canvas {
        val canvas = Canvas(bitmap)
        val paint = strokePaint
        canvas.drawColor(Color.WHITE)

        paint.color = Color.BLACK

        for(i: Int in 0 until selectedStroke.size) {
            canvas.drawPath(pathList[selectedStroke[i]].path, paint)
        }
        return canvas
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