package com.example.paintapp

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.paintapp.API.RetrofitInstance
import com.example.paintapp.API.response.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.forms.FormType
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationTool
import com.pspdfkit.ui.special_mode.manager.AnnotationManager.OnAnnotationCreationModeChangeListener
import com.pspdfkit.ui.toolbar.AnnotationCreationToolbar
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.math.log
import java.lang.Float.max
import java.lang.Float.min


const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity(), OnAnnotationCreationModeChangeListener{
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var btnAddPdf: Button
    private lateinit var answerBtn: ImageButton
    private lateinit var changeMode: Button
    private lateinit var viewModel: PaintViewModel
    private lateinit var paintViewContainer: FrameLayout
    private lateinit var tvStorageInfo: TextView
    private var strokePosition: PointF = PointF(0F, 0F)


    private lateinit var paintFragment: PaintFragment

    private lateinit var annotationCreationToolbar: AnnotationCreationToolbar
    private lateinit var toolbarCoordinatorLayout : ToolbarCoordinatorLayout
    // private lateinit var toolbarCoordinatorLayout: ToolbarCoordinatorLayout
    private lateinit var fragment: PdfFragment

    companion object{
        // accessible throughout the application
        var path = Path()
        var strokePaint = Paint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ocrapi.OcrTest("KakaoTalk_Photo_2023-05-06-21-59-55.jpg")
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[PaintViewModel::class.java]
        paintViewContainer = findViewById<FrameLayout>(R.id.frame_layout)

        val observer = Observer<String> { m ->
            val modelAnswer = ModelAnswer(this)
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

            questionTextView.text = getString(R.string.app_gpt_question, "What is YOLO") ?: ""
            questionBarTextView.text = getString(R.string.app_gpt_question, "What is YOLO") ?: ""
            answerTextView.text = getString(R.string.app_gpt_answer, m)

            closeBtn.setOnClickListener {
                paintViewContainer.removeView(modelAnswer)
            }

            minimizeBtn.setOnClickListener {
                if (scrollView.visibility == View.VISIBLE)
                    scrollView.visibility = View.GONE
                else if(scrollView.visibility == View.GONE)
                    scrollView.visibility = View.VISIBLE
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
            paintViewContainer.addView(modelAnswer)
            modelAnswer.bringToFront()
        }
        viewModel.modelAnswer.observe(this, observer)

//        val selectBrush = findViewById<ImageView>(R.id.selectBrush)
//        val brushBtnGroup = findViewById<LinearLayout>(R.id.brushGroup)
//        val redBtn = findViewById<ImageButton>(R.id.redColor)
//        val blueBtn = findViewById<ImageButton>(R.id.blueColor)
//        val blackBtn = findViewById<ImageButton>(R.id.blackColor)
//        val clearBtn = findViewById<ImageView>(R.id.clear)
        val checkButton = findViewById<Button>(R.id.btnAddPdf)

        supportFragmentManager.beginTransaction()
            .add(R.id.frame_layout, PaintFragment())
            .addToBackStack(null)
            .commit()


//        selectBrush.setOnClickListener {
//            displaySelectBox = false
//            isSelected = false
//            selectedStroke.clear()
//            if (selectMode) {
//                selectMode = false
//                brushBtnGroup.visibility = VISIBLE
//            }
//            else {
//                selectMode = true
//                brushBtnGroup.visibility = GONE
//            }
//        }
//        redBtn.setOnClickListener {
//            if (!selectMode) {
//                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
//                currentBrush = Color.RED
//                currentColor(currentBrush)
//            }
//        }
//        blueBtn.setOnClickListener {
//            if (!selectMode) {
//                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
//                currentBrush = Color.BLUE
//                currentColor(currentBrush)
//            }
//        }
//        blackBtn.setOnClickListener {
//            if (!selectMode) {
//                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
//                currentBrush = Color.BLACK
//                currentColor(currentBrush)
//            }
//        }
//        clearBtn.setOnClickListener {
//            if (!selectMode) {
//                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
//                pathList.clear()
//                path.reset()
//            }
//        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        btnAddPdf = findViewById(R.id.btnAddPdf)
        btnAddPdf.setOnClickListener {
            startActivityForResult(intent, PICK_PDF_FILE)
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int,resultData:Intent?){
        super.onActivityResult(requestCode, resultCode, resultData)
        val annotationTools = mutableListOf(*AnnotationTool.values())
        toolbarCoordinatorLayout = findViewById(R.id.toolbarCoordinatorLayout) as ToolbarCoordinatorLayout

        annotationTools.remove(AnnotationTool.MAGIC_INK)

        val enabledAnnotationTools = AnnotationTool.values().toMutableList()
        enabledAnnotationTools.remove(AnnotationTool.IMAGE)



        if(requestCode == PICK_PDF_FILE && resultCode== Activity.RESULT_OK){
            resultData?.data?.also{uri->

                val documentUri = Uri.parse(uri.toString())
                val config = PdfConfiguration.Builder()
                    .scrollDirection(PageScrollDirection.VERTICAL)
                    .scrollMode(PageScrollMode.CONTINUOUS)
                    .layoutMode(PageLayoutMode.SINGLE)
                    .enabledAnnotationTools(enabledAnnotationTools)
                    .build()

                val frag = PdfFragment.newInstance(documentUri, config)

                val transaction = supportFragmentManager.beginTransaction()
                transaction.add(R.id.frame_layout, frag)
                transaction.addToBackStack("detail")
                transaction.commit()


                changeMode = findViewById(R.id.changeEditmodeBtn)
                changeMode.setOnClickListener{
                    //Toast.makeText(this,"Toolbar 표시해라",Toast.LENGTH_LONG).show()
                    //frag.enterAnnotationCreationMode(tool)
//                    toolbarCoordinatorLayout = findViewById(R.id.toolbarCoordinatorLayout)
                    annotationCreationToolbar = AnnotationCreationToolbar(this)
                    frag.addOnAnnotationCreationModeChangeListener(this)
                    frag.enterAnnotationCreationMode()
                }

                frag.addOnFormElementClickedListener{ formElement ->
                    when{
                        formElement.type == FormType.UNDEFINED->{
                            Toast.makeText(this, "손글씨 선택", Toast.LENGTH_LONG).show()
                            true
                        }
                        formElement.type == FormType.TEXT->{
                            Toast.makeText(this,"tttt",Toast.LENGTH_LONG).show()
                            true
                        }
                        formElement.type == FormType.SIGNATURE->{
                            Toast.makeText(this,"Select Sign",Toast.LENGTH_LONG).show()
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }


                answerBtn = findViewById(R.id.answerBtn)
                answerBtn.setOnClickListener {
                    if (frag.textSelection != null) {
                        var minX = 999999F
                        var minY = 0F

                        for (el in frag.textSelection!!.textBlocks) {

                            println("${frag.textSelection!!.pageIndex}, ${el.top}, ${el.bottom}, ${el.right}, ${el.left}")
                            minX = min(el.left, minX)
                            minY = max(el.bottom, minY)
                        }
                    }
                }
            }
        }
    }

    override fun onEnterAnnotationCreationMode(controller: AnnotationCreationController) {
        // Bind the toolbar to the controller.

        Toast.makeText(this,"Toolbar 표시해라",Toast.LENGTH_LONG).show()
        annotationCreationToolbar.bindController(controller)

        // Now display the toolbar in the `toolbarCoordinatorLayout`.
        toolbarCoordinatorLayout.displayContextualToolbar(annotationCreationToolbar, true)
    }

    override fun onChangeAnnotationCreationMode(p0: AnnotationCreationController) {
    }

    override fun onExitAnnotationCreationMode(p0: AnnotationCreationController) {
        annotationCreationToolbar.unbindController()
    }

}
