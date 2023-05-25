package com.example.paintapp

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.forms.FormType
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationSelectionController
import com.pspdfkit.ui.special_mode.controller.AnnotationTool
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.special_mode.manager.AnnotationManager.OnAnnotationCreationModeChangeListener
import com.pspdfkit.ui.special_mode.manager.AnnotationManager.OnAnnotationSelectedListener
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


var pdflist = mutableListOf<PdfFragment>()
const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener, OnAnnotationCreationModeChangeListener {
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var btnAddPdf: Button
    private lateinit var answerBtn: ImageButton
    private lateinit var changeMode: Button
    private lateinit var viewModel: PaintViewModel
    private lateinit var paintViewContainer: FrameLayout
    private lateinit var tvStorageInfo: TextView
    private lateinit var global_frag: PdfFragment
    private lateinit var btnNavi: ImageButton
    private lateinit var menu: Menu
    private var pdf_count = 1
    private var strokePosition: PointF = PointF(0F, 0F)


    private var paintFragment: PaintFragment = PaintFragment()

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


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navi_main)
        navigationView.setNavigationItemSelectedListener(this)
        btnNavi = findViewById(R.id.btnNavi)

        btnNavi.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

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

        val checkButton = findViewById<Button>(R.id.btnAddPdf)

        menu = navigationView.menu
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "메인 메뉴")
        displayView(menu.getItem(0))

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

//                frag.addOnAnnotationSelectedListener(object : AnnotationManager.OnAnnotationSelectedListener {
//                    override fun onPrepareAnnotationSelection(controller: AnnotationSelectionController, annotation: Annotation, annotationCreated: Boolean): Boolean {
//                        // Returning `false` here would prevent the annotation from being selected.
//                        return true
//                    }
//
//                    override fun onAnnotationSelected(annotation: Annotation, annotationCreated: Boolean) {
////                        Log.i(TAG, "The annotation was selected.")
////                        annotation.
//                    }
//                })

                pdflist.add(frag)
                menu.add(Menu.NONE, Menu.FIRST+pdf_count, Menu.NONE, getFileName(uri))
                val item = menu.getItem(pdf_count)
                pdf_count+=1
                displayView(item)

                changeMode = findViewById(R.id.changeEditmodeBtn)
                changeMode.setOnClickListener{
                    //Toast.makeText(this,"Toolbar 표시해라",Toast.LENGTH_LONG).show()
                    //frag.enterAnnotationCreationMode(tool)
//                    toolbarCoordinatorLayout = findViewById(R.id.toolbarCoordinatorLayout)
                    annotationCreationToolbar = AnnotationCreationToolbar(this)
                    frag.addOnAnnotationCreationModeChangeListener(this)
                    frag.enterAnnotationCreationMode()
                }


                answerBtn = findViewById(R.id.answerBtn)
                answerBtn.setOnClickListener {
                    val p0 = frag.textSelection
                    if(p0 == null)
                        Toast.makeText(this, "PDF를 open하고 원하는 텍스트를 선택하세요!", Toast.LENGTH_LONG).show()
                    else {
                        val Popup = ModelPopupActivity(this)


                        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                        val okHttpClient = OkHttpClient()
                        var json = "{" +
                                "  \"model\": \"gpt-3.5-turbo\"," +
                                "  \"messages\": [{\"role\": \"user\"," +
                                "  \"content\": \"Hello!!\"}]" +
                                "}"

                        val prompt = p0.text!!
                        json = json.replace("Hello!!", prompt)
                        val requestBody: RequestBody = json.toRequestBody(mediaType)
                        val request: Request =
                            Request.Builder()
                                .url("https://api.openai.com/v1/chat/completions")
                                .addHeader(
                                    "Authorization",
                                    BuildConfig.API_KEY
                                )
                                .post(requestBody)
                                .build()

                        okHttpClient.newCall(request).enqueue(object : Callback {
                            override fun onResponse(call: Call, response: Response) {
                                //Log.i("MyErrorMSG", "onResponse: ${response.body.toString()}")
                                val json_obj = JSONObject(response.body?.string())
                                val json_array = json_obj.optJSONArray("choices")

                                val json_text = json_array.getJSONObject(0).getString("message")
                                val json_obj2 = JSONObject(json_text)
                                val json_text2 = json_obj2.getString("content")

                                runOnUiThread {
                                    Popup.show(json_text2)
                                }
                            }
                            override fun onFailure(call: Call, e: IOException) {
                                Popup.show("Something Wrong!! Please try again...")
                            }
                        })
                    }
                }
            }
        }
    }
    override fun onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers()
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displayView(item)
        return false
    }
    private fun displayView(item:MenuItem){
        var frag: Fragment? = null
        val idx= (item.itemId - Menu.FIRST)
        if(idx == 0)
            frag = paintFragment
        else if(idx >= 1)
            frag = pdflist[idx-1]
        if(frag != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, frag)
                .commit()
        }
        drawerLayout.closeDrawers()
    }
    fun getFileName(uri:Uri):String?{
        var result: String? = null
        if(uri.scheme == "content"){
            val cursor: Cursor? = contentResolver.query(uri,null,null,null,null)
            try {
                if(cursor != null && cursor.moveToFirst()){
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(nameIndex >= 0) {
                        result =
                            cursor.getString(nameIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if(result == null){
            return uri.lastPathSegment
        }
        return result
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