package com.example.paintapp

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.paintapp.API.RetrofitInstance
import com.example.paintapp.API.response.Message
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.PdfFragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Float.max
import java.lang.Float.min


const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity() {
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var btnAddPdf: Button
    private lateinit var answerBtn: ImageButton
    private lateinit var viewModel: PaintViewModel
    private lateinit var paintViewContainer: FrameLayout
    private lateinit var tvStorageInfo: TextView
    private var strokePosition: PointF = PointF(0F, 0F)


    private lateinit var paintFragment: PaintFragment

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
        if(requestCode == PICK_PDF_FILE && resultCode== Activity.RESULT_OK){
            resultData?.data?.also{uri->

                val documentUri = Uri.parse(uri.toString())
                val config = PdfConfiguration.Builder()
                    .scrollDirection(PageScrollDirection.VERTICAL)
                    .scrollMode(PageScrollMode.CONTINUOUS)
                    .layoutMode(PageLayoutMode.SINGLE)
                    .build()

                val frag = PdfFragment.newInstance(documentUri, config)
                //val mytext = MyText()
                //frag.addOnTextSelectionChangeListener(mytext)
                //frag.addOnAnnotationCreationModeChangeListener(mytext)

                val transaction = supportFragmentManager.beginTransaction()
                transaction.add(R.id.frame_layout, frag)
                transaction.addToBackStack("detail")
                transaction.commit()


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
//                answerBtn.setOnClickListener {
//                    val p0 = frag.textSelection
//                    if(p0 == null)
//                        Toast.makeText(this, "PDF를 open하고 원하는 텍스트를 선택하세요!", Toast.LENGTH_LONG).show()
//                    else {
//                        val Popup = ModelPopupActivity(this)
//
//
//                        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
//                        val okHttpClient = OkHttpClient()
//                        var json = "{" +
//                                "  \"model\": \"gpt-3.5-turbo\"," +
//                                "  \"messages\": [{\"role\": \"user\"," +
//                                "  \"content\": \"Hello!!\"}]" +
//                                "}"
//
//                        val prompt = p0.text!!
//                        json = json.replace("Hello!!", prompt)
//                        val requestBody: RequestBody = json.toRequestBody(mediaType)
//                        val request: Request =
//                            Request.Builder()
//                                .url("https://api.openai.com/v1/chat/completions")
//                                .addHeader(
//                                    "Authorization",
//                                    BuildConfig.API_KEY
//                                )
//                                .post(requestBody)
//                                .build()
//
//                            okHttpClient.newCall(request).enqueue(object : Callback {
//                            override fun onResponse(call: Call, response: Response) {
//                                //Log.i("MyErrorMSG", "onResponse: ${response.body.toString()}")
//                                val json_obj = JSONObject(response.body?.string())
//                                val json_array = json_obj.optJSONArray("choices")
//
//                                val json_text = json_array.getJSONObject(0).getString("message")
//                                val json_obj2 = JSONObject(json_text)
//                                val json_text2 = json_obj2.getString("content")
//
//                                runOnUiThread {
//                                    Popup.show(json_text2)
//                                }
//                            }
//                            override fun onFailure(call: Call, e: IOException) {
//                                Popup.show("Something Wrong!! Please try again...")
//                            }
//                        })
//                    }
//                }
            }
        }
    }
}
