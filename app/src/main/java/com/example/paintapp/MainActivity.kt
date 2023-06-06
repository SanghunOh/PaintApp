package com.example.paintapp

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.util.Base64
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.paintapp.UI.ModelAnswer
import com.example.paintapp.UI.PaintFragment
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationSelectionController
import com.pspdfkit.ui.special_mode.controller.AnnotationTool
import com.pspdfkit.ui.special_mode.controller.TextSelectionController
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.special_mode.manager.AnnotationManager.OnAnnotationCreationModeChangeListener
import com.pspdfkit.ui.special_mode.manager.TextSelectionManager
import com.pspdfkit.ui.toolbar.AnnotationCreationToolbar
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*
import java.util.*
import java.net.HttpURLConnection
import java.net.URL


var pdflist = mutableListOf<PdfFragment>()
const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener, OnAnnotationCreationModeChangeListener{
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var brushGroup : LinearLayout
    private lateinit var selectBrush : ImageButton
    private lateinit var btnAddPdf: ImageButton
    private lateinit var answerBtn: ImageButton
    private lateinit var summarizeBtn: ImageButton
    private lateinit var changeMode: ImageButton
    private lateinit var viewModel: PaintViewModel
    private lateinit var paintViewContainer: FrameLayout
    private lateinit var tvStorageInfo: TextView
    private lateinit var frag: PdfFragment
    private lateinit var btnNavi: ImageButton
    private lateinit var menu: Menu
    private var bitmapToSend: Bitmap? = null
    private var pdf_count = 1
    private var strokePosition: PointF = PointF(0F, 0F)
    private var isAnnotationSelected = false


    private var paintFragment: PaintFragment = PaintFragment.newInstance()

    private lateinit var annotationCreationToolbar: AnnotationCreationToolbar
    private lateinit var toolbarCoordinatorLayout : ToolbarCoordinatorLayout
    private lateinit var fragment: PdfFragment

    companion object{
        // accessible throughout the application
        var path = Path()
        var strokePaint = Paint()
        var isPdfLoaded = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navi_main)
        selectBrush = findViewById(R.id.selectBrush)
        brushGroup = findViewById(R.id.brushGroup)
        answerBtn = findViewById(R.id.answerBtn)
        btnAddPdf = findViewById(R.id.btnAddPdf)
        changeMode = findViewById(R.id.change_edit_modeBtn)
        summarizeBtn = findViewById(R.id.summarizeBtn)
        navigationView.setNavigationItemSelectedListener(this)
        btnNavi = findViewById(R.id.btnNavi)

        btnNavi.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        supportActionBar?.hide()

        paintViewContainer = findViewById(R.id.frame_layout)

        menu = navigationView.menu
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Empty Note")
        displayView(menu.getItem(0))

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        btnAddPdf.setOnClickListener {
            startActivityForResult(intent, PICK_PDF_FILE)
        }

        answerBtn.setOnClickListener { answerBtnOnClickListener() }
        changeMode.setOnClickListener{ changeModeBtnOnClickListener() }
        summarizeBtn.setOnClickListener { summarizeBtnOnClickListener() }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int,resultData:Intent?){
        super.onActivityResult(requestCode, resultCode, resultData)
        val annotationTools = mutableListOf(*AnnotationTool.values())
        toolbarCoordinatorLayout = findViewById<ToolbarCoordinatorLayout>(R.id.toolbarCoordinatorLayout)

        annotationTools.remove(AnnotationTool.MAGIC_INK)

        val enabledAnnotationTools = AnnotationTool.values().toMutableList()
        enabledAnnotationTools.remove(AnnotationTool.IMAGE)
        enabledAnnotationTools.add(AnnotationTool.MAGIC_INK)


        if(requestCode == PICK_PDF_FILE && resultCode== Activity.RESULT_OK){
            isPdfLoaded = true
            resultData?.data?.also{uri->

                val documentUri = Uri.parse(uri.toString())
                val config = PdfConfiguration.Builder()
                    .scrollDirection(PageScrollDirection.VERTICAL)
                    .scrollMode(PageScrollMode.CONTINUOUS)
                    .layoutMode(PageLayoutMode.SINGLE)
                    .enabledAnnotationTools(enabledAnnotationTools)
                    .build()

                frag = PdfFragment.newInstance(documentUri, config)

                frag.addOnTextSelectionModeChangeListener(object : TextSelectionManager.OnTextSelectionModeChangeListener {
                    override fun onEnterTextSelectionMode(p0: TextSelectionController) {
                        answerBtn.visibility = View.VISIBLE
                        summarizeBtn.visibility = View.VISIBLE
                    }

                    override fun onExitTextSelectionMode(p0: TextSelectionController) {
                        answerBtn.visibility = View.GONE
                        summarizeBtn.visibility = View.GONE
                    }
                })

                frag.addOnAnnotationSelectedListener(object : AnnotationManager.OnAnnotationSelectedListener {
                    override fun onPrepareAnnotationSelection(controller: AnnotationSelectionController, annotation: Annotation, annotationCreated: Boolean): Boolean {
                        // Returning `false` here would prevent the annotation from being selected.
                        return true
                    }

                    override fun onAnnotationSelected(annotation: Annotation, annotationCreated: Boolean) {
                        isAnnotationSelected = true
                        answerBtn.visibility = View.VISIBLE
                        val annotationWidth = annotation.boundingBox.width()
                        val annotationHeight = -annotation.boundingBox.height()

                        val bitmapWidth = 400
                        val heightFactor = bitmapWidth / annotationWidth
                        val bitmapHeight = (annotationHeight * heightFactor).toInt()

                        bitmapToSend = Bitmap.createBitmap(
                            bitmapWidth,
                            bitmapHeight,
                            Bitmap.Config.ARGB_8888)
                        annotation.renderToBitmap(bitmapToSend!!)

//                        val pictureFileDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PaintApp")
//                        val pictureFile = File(pictureFileDir.path + System.currentTimeMillis() + ".jpeg")
//
//                        if (!pictureFileDir.exists()) {
//                            pictureFileDir.mkdirs()
//                        }
//                        var fos: FileOutputStream? = null
//                        fos = FileOutputStream(pictureFile)
//                        bitmapToSend.compress(Bitmap.CompressFormat.JPEG, 100, fos)
//                        fos.flush()
//                        fos.close()
                    }
                })

                frag.addOnAnnotationDeselectedListener(object : AnnotationManager.OnAnnotationDeselectedListener{
                    override fun onAnnotationDeselected(p0: Annotation, p1: Boolean) {
                        isAnnotationSelected = false
                        answerBtn.visibility = View.GONE
                    }
                })

                pdflist.add(frag)
                menu.add(Menu.NONE, Menu.FIRST+pdf_count, Menu.NONE, getFileName(uri))
                val item = menu.getItem(pdf_count)
                pdf_count+=1
                displayView(item)
            }
        }
    }

    private fun changeModeBtnOnClickListener() {
        annotationCreationToolbar = AnnotationCreationToolbar(this)
        frag.addOnAnnotationCreationModeChangeListener(this)
        frag.enterAnnotationCreationMode()
    }
    private fun answerBtnOnClickListener() {
        if (isPdfLoaded) {
            var question: String = ""
            if (isAnnotationSelected) {
                question = OCR_API(bitmapToSend!!)

                answerBtn.visibility = View.GONE
                summarizeBtn.visibility = View.GONE
            }
            else {
                question = frag.textSelection?.text.toString()
            }
            if (question == null)
                Toast.makeText(this, "PDF를 open하고 원하는 텍스트를 선택하세요!", Toast.LENGTH_LONG).show()
            else {

                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                val okHttpClient = OkHttpClient()
                question = question.replace("\n", " ")
//{  "model": "gpt-3.5-turbo",  "messages": [{"role": "user",  "content": "A Log Buffer-Based Flash Translation Layer"}]}
//{  "model": "gpt-3.5-turbo",  "messages": [{"role": "user",  "content": "A Log Buffer-Based Flash Translation Layer Using Fully-Associative Sector Translation"}]}

                val json = "{" +
                        "  \"model\": \"gpt-3.5-turbo\"," +
                        "  \"messages\": [{\"role\": \"user\"," +
                        "  \"content\": \"$question\"}]" +
                        "}"

                println("$question, $json")

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
                        Log.i("MyErrorMSG", "onResponse: ${response.body.toString()}")
                        println(response.body.toString())
                        val json_obj = JSONObject(response.body?.string())
                        println(json_obj.toString())
                        val json_array = json_obj.optJSONArray("choices")

                        val json_text = json_array.getJSONObject(0).getString("message")
                        val json_obj2 = JSONObject(json_text)
                        val json_text2 = json_obj2.getString("content")

                        val modelAnswer = ModelAnswer(this@MainActivity)
                        val closeBtn = modelAnswer.findViewById<ImageView>(R.id.close)
                        val minimizeBtn = modelAnswer.findViewById<ImageView>(R.id.minimize)
                        val questionBarTextView = modelAnswer.findViewById<TextView>(R.id.question_bar)
                        val questionTextView = modelAnswer.findViewById<EditText>(R.id.question)
                        val answerTextView = modelAnswer.findViewById<TextView>(R.id.answer)
                        val modelAnswerTopBar = modelAnswer.findViewById<LinearLayout>(R.id.model_answer_top_bar)
                        val gptReRequest = modelAnswer.findViewById<ImageButton>(R.id.model_answer_gpt_request)
                        val scrollView = modelAnswer.findViewById<ScrollView>(R.id.answer_field)

                        val layoutParams = LinearLayout.LayoutParams(800, 500)
                        layoutParams.leftMargin = 1000
                        layoutParams.topMargin = 500

                        modelAnswer.layoutParams = layoutParams

                        questionTextView.text = Editable.Factory.getInstance().newEditable(question)
                        questionBarTextView.text = this@MainActivity.getString(R.string.app_gpt_full_question, question) ?: ""
                        answerTextView.text = this@MainActivity.getString(R.string.app_gpt_answer, json_text2)

                        closeBtn.setOnClickListener {
                            paintViewContainer.removeView(modelAnswer)
                        }

                        minimizeBtn.setOnClickListener {
                            if (scrollView.visibility == View.VISIBLE)
                                scrollView.visibility = View.GONE
                            else if(scrollView.visibility == View.GONE)
                                scrollView.visibility = View.VISIBLE
                        }

                        gptReRequest.setOnClickListener {
                            it.clearFocus()

                            (this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(
                                questionTextView.windowToken,
                                0
                            );
                            questionBarTextView.text = this@MainActivity.getString(R.string.app_gpt_full_question, questionTextView.text.toString())
                            gptRequest(questionTextView.text.toString(), answerTextView)
                        }

                        var moveX = strokePosition.x
                        var moveY = strokePosition.y
                        modelAnswerTopBar.setOnTouchListener { _, event->
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

                        runOnUiThread {
                            paintViewContainer.addView(modelAnswer)
                            modelAnswer.bringToFront()
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "GPT request failed...", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }
        else {
            paintFragment.onGptRequest()
        }
    }

    private fun summarizeBtnOnClickListener() {
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val okHttpClient = OkHttpClient()
        val prompt = "summarize please '${frag.textSelection?.text}'"
        val json = "{" +
                "  \"model\": \"gpt-3.5-turbo\"," +
                "  \"messages\": [{\"role\": \"user\"," +
                "  \"content\": \"$prompt\"}]" +
                "}"

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

                val modelAnswer = ModelAnswer(this@MainActivity)
                val closeBtn = modelAnswer.findViewById<ImageView>(R.id.close)
                val minimizeBtn = modelAnswer.findViewById<ImageView>(R.id.minimize)
                val questionBarTextView = modelAnswer.findViewById<TextView>(R.id.question_bar)
                val questionTextView = modelAnswer.findViewById<EditText>(R.id.question)
                val answerTextView = modelAnswer.findViewById<TextView>(R.id.answer)
                val modelAnswerTopBar = modelAnswer.findViewById<LinearLayout>(R.id.model_answer_top_bar)
                val scrollView = modelAnswer.findViewById<ScrollView>(R.id.answer_field)

                val layoutParams = LinearLayout.LayoutParams(800, 500)
                layoutParams.leftMargin = 1000
                layoutParams.topMargin = 500

                modelAnswer.layoutParams = layoutParams

//                questionBarTextView.text = this@MainActivity.getString(R.string.app_gpt_full_question, prompt)
                answerTextView.text = this@MainActivity.getString(R.string.app_gpt_answer, json_text2)

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
                modelAnswerTopBar.setOnTouchListener { _, event->
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

                runOnUiThread {
                    paintViewContainer.addView(modelAnswer)
                    modelAnswer.bringToFront()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "GPT request failed...", Toast.LENGTH_LONG).show()
                }
            }
        })
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
        if(idx == 0) {
            frag = paintFragment
            paintFragment.onResume()
            isPdfLoaded = false
            brushGroup.visibility = View.VISIBLE
            selectBrush.visibility = View.VISIBLE
            changeMode.visibility = View.GONE
        }
        else if(idx >= 1) {
            frag = pdflist[idx - 1]
            isPdfLoaded = true
            brushGroup.visibility = View.GONE
            selectBrush.visibility = View.GONE
            changeMode.visibility = View.VISIBLE
        }
        answerBtn.visibility = View.GONE
        summarizeBtn.visibility = View.GONE
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

    private fun gptRequest(q: String, view: TextView) {

        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val okHttpClient = OkHttpClient()
        val json = "{" +
                "  \"model\": \"gpt-3.5-turbo\"," +
                "  \"messages\": [{\"role\": \"user\"," +
                "  \"content\": \"$q\"}]" +
                "}"

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
                val json_obj = JSONObject(response.body?.string())
                val json_array = json_obj.optJSONArray("choices")

                val json_text = json_array.getJSONObject(0).getString("message")
                val json_obj2 = JSONObject(json_text)
                val json_text2 = json_obj2.getString("content")

                runOnUiThread {
                    view.text = json_text2
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "GPT request failed...", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onEnterAnnotationCreationMode(controller: AnnotationCreationController) {
        // Bind the toolbar to the controller.
        annotationCreationToolbar.bindController(controller)

        // Now display the toolbar in the `toolbarCoordinatorLayout`.
        toolbarCoordinatorLayout.displayContextualToolbar(annotationCreationToolbar, true)
    }

    override fun onChangeAnnotationCreationMode(p0: AnnotationCreationController) {
    }

    override fun onExitAnnotationCreationMode(p0: AnnotationCreationController) {
        annotationCreationToolbar.unbindController()
    }

    fun OCR_API(bitmap: Bitmap):String{
        val base64Image: String = encodeImageToBase64(bitmap)

        // 서버로 이미지 전송
        return sendImageToServer(base64Image)
    }


    private fun encodeImageToBase64(imageBitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }
    private fun sendImageToServer(base64Image: String):String {
        var response = ""
        try {
            val thread = Thread {
                try {
                    val url = URL("http://10.0.1.82:5002/upload")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    // JSON 데이터 생성
                    val requestData = JSONObject()
                    requestData.put("image", base64Image)

                    // 데이터 전송
                    val outputStream = DataOutputStream(connection.outputStream)
                    outputStream.writeBytes(requestData.toString())
                    outputStream.flush()
                    outputStream.close()

                    // 응답 코드 확인
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 응답 데이터 읽기
                        val inputStream = connection.inputStream
                        response = readInputStream(inputStream)
                        println(response)

                        // 서버에서 보낸 응답 처리
                        handleServerResponse(response)


                    } else {
                        println("POST request failed. Response Code: $responseCode")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            thread.start()
            thread.join()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }


    @Throws(IOException::class)
    private fun readInputStream(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        reader.close()
        return stringBuilder.toString()
    }

    private fun handleServerResponse(response: String) {
        Log.d("Server Response", response)
    }
}