package com.example.paintapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.Button
import android.widget.TextView
import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResult
import com.example.paintapp.API.RetrofitInstance
import com.example.paintapp.API.response.Message
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.drawable.PdfDrawable
import com.pspdfkit.ui.drawable.PdfDrawableProvider
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationTool
import com.pspdfkit.ui.special_mode.manager.AnnotationManager.OnAnnotationCreationModeChangeListener
import com.pspdfkit.ui.toolbar.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*


const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity(), OnAnnotationCreationModeChangeListener{
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var btnAddPdf: Button
    private lateinit var answerBtn: Button
    private lateinit var changeMode: Button
    private lateinit var tvStorageInfo: TextView

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
        val annotationTools = mutableListOf(*AnnotationTool.values())
        toolbarCoordinatorLayout = findViewById(R.id.toolbarCoordinatorLayout) as ToolbarCoordinatorLayout

        annotationTools.remove(AnnotationTool.MAGIC_INK)

        val enabledAnnotationTools = AnnotationTool.values().toMutableList()
        enabledAnnotationTools.remove(AnnotationTool.IMAGE)

        val tool = AnnotationTool.SIGNATURE


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
