package com.example.paintapp

import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.paintapp.UI.PaintView
import com.example.paintapp.data.PaintCanvas
import kotlinx.coroutines.delay


class PaintFragment : Fragment(), CustomEventListener {
    private lateinit var mainActivity: MainActivity
    private lateinit var paintView: PaintView
    private lateinit var viewModel: PaintViewModel
    private lateinit var paintViewContainer: FrameLayout
    private lateinit var paintFragmentView: View
    private var isStrokeSelected = false
    private var strokePosition: PointF = PointF(0F, 0F)

    companion object {
        fun newInstance() = PaintFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_paint, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paintFragmentView = view

        viewModel = ViewModelProvider(this)[PaintViewModel::class.java]
        viewModel.pathList.observe(viewLifecycleOwner, Observer<ArrayList<CustomPath>> {paths ->
            paintView.pathList = paths
        })

        viewModel.addFile(PaintCanvas("Blank"))

        val selectBrush = mainActivity.findViewById<ImageView>(R.id.selectBrush)
        val brushBtnGroup = mainActivity.findViewById<LinearLayout>(R.id.brushGroup)
        val redBtn = mainActivity.findViewById<ImageButton>(R.id.redColor)
        val blueBtn = mainActivity.findViewById<ImageButton>(R.id.blueColor)
        val blackBtn = mainActivity.findViewById<ImageButton>(R.id.blackColor)
        val clearBtn = mainActivity.findViewById<ImageView>(R.id.clear)
        val gptRequest = mainActivity.findViewById<ImageButton>(R.id.answerBtn)
        paintViewContainer = mainActivity.findViewById(R.id.paint_view_container)

        selectBrush.setOnClickListener {
            paintView.displaySelectBox = false
            paintView.isSelect = false
            paintView.selectedStroke.clear()
            if (paintView.selectMode) {
                paintView.selectMode = false
                brushBtnGroup.visibility = View.VISIBLE
            }
            else {
                paintView.selectMode = true
                brushBtnGroup.visibility = View.GONE
            }
        }
        redBtn.setOnClickListener {
            if (!paintView.selectMode) {
                paintView.currentBrush = Color.RED
                currentColor(paintView.currentBrush)
            }
        }
        blueBtn.setOnClickListener {
            if (!paintView.selectMode) {
                paintView.currentBrush = Color.BLUE
                currentColor(paintView.currentBrush)
            }
        }
        blackBtn.setOnClickListener {
            if (!paintView.selectMode) {
                paintView.currentBrush = Color.BLACK
                currentColor(paintView.currentBrush)
            }
        }
        clearBtn.setOnClickListener {
            if (!paintView.selectMode) {
                viewModel.clearPath()
                paintView.pathList.clear()
                MainActivity.path.reset()
            }
        }

        val paintViewInclude = view.findViewById<FrameLayout>(R.id.paint_view_include)
        paintView = paintViewInclude.findViewById(R.id.paint_view)
        paintView = view.findViewById(R.id.paint_view)
        paintView.setCustomEventListener(this)

        gptRequest.setOnClickListener {
            onGptRequest(paintView)
        }

        val observer = Observer<String> { m ->
//                val modelAnswer: LinearLayout = layoutInflater.inflate(R.layout.model_answer_view, paintViewContainer,false) as LinearLayout
            val modelAnswer = ModelAnswer(mainActivity)
            modelAnswer.visibility = LinearLayout.VISIBLE

            val layoutParams = LinearLayout.LayoutParams(800, 500)
            layoutParams.leftMargin = strokePosition.x.toInt()
            layoutParams.topMargin = strokePosition.y.toInt()

            modelAnswer.layoutParams = layoutParams

            val closeBtn = modelAnswer.findViewById<ImageView>(R.id.close)
            val minimizeBtn = modelAnswer.findViewById<ImageView>(R.id.minimize)
            val questionBarTextView = modelAnswer.findViewById<TextView>(R.id.question_bar)
            val questionTextView = modelAnswer.findViewById<TextView>(R.id.question)
            val answerTextView = modelAnswer.findViewById<TextView>(R.id.answer)
            val modelAnswerTopBar = modelAnswer.findViewById<LinearLayout>(R.id.model_answer_top_bar)
            val scrollView = modelAnswer.findViewById<ScrollView>(R.id.answer_field)

            questionTextView.text = context?.getString(R.string.app_gpt_question, "What is YOLO") ?: ""
            questionBarTextView.text = context?.getString(R.string.app_gpt_question, "What is YOLO") ?: ""
            answerTextView.text = context?.getString(R.string.app_gpt_answer, m)

            closeBtn.setOnClickListener {
                paintViewContainer.removeView(modelAnswer)
            }

            minimizeBtn.setOnClickListener {
                if (scrollView.visibility == VISIBLE)
                    scrollView.visibility = GONE
                else if(scrollView.visibility == GONE)
                    scrollView.visibility = VISIBLE
            }

            var moveX = strokePosition.x
            var moveY = strokePosition.y
            modelAnswerTopBar.setOnTouchListener { v, event->
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        moveX = modelAnswer.x - event.rawX
                        moveY = modelAnswer.y - event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        modelAnswer.animate()
                            .x(event.rawX + moveX)
                            .y(event.rawY + moveY)
                            .setDuration(0)
                            .start()
                    }
                }
                return@setOnTouchListener true
            }
            Log.d("gpt", "WHY??")
            paintViewContainer.addView(modelAnswer)
            modelAnswer.bringToFront()
        }
        viewModel.modelAnswer.observe(viewLifecycleOwner, observer)
    }

    private fun currentColor(color: Int){
        MainActivity.strokePaint.color = color
        MainActivity.path = Path()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[PaintViewModel::class.java]
    }

    override fun onPathAdded(path: CustomPath) {
        viewModel.addPath(path)
    }

    override fun onStrokeSelected(pos: PointF) {
        println("${pos.x}, ${pos.y}")
        isStrokeSelected = true
        strokePosition.x = pos.x
        strokePosition.y = pos.y
    }

    private fun onGptRequest(paintView: PaintView) {
        if (isStrokeSelected) {
            isStrokeSelected = false
            paintView.isSelect = false

            viewModel.queryGPT(0, "What is YOLO?", PointF(0F, 0F))
        }
    }
}