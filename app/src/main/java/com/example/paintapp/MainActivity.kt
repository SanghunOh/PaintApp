package com.example.paintapp

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.Button
import android.widget.TextView
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.ui.NavigationUI
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


var pdflist = mutableListOf<PdfFragment>()
const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var btnAddPdf: Button
    private lateinit var answerBtn: Button
    private lateinit var btnNavi: ImageButton
    private lateinit var btnTemp: ImageButton
    private lateinit var tvStorageInfo: TextView
    private lateinit var global_frag: PdfFragment
    private lateinit var menu: Menu
    private var pdf_count = 1

    private var paintFragment: PaintFragment = PaintFragment()

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
        if(requestCode == PICK_PDF_FILE && resultCode== Activity.RESULT_OK){
            resultData?.data?.also{uri->

                val documentUri = Uri.parse(uri.toString())
                Log.d("PDFURI", "${uri.toString()}")
                val config = PdfConfiguration.Builder()
                    .scrollDirection(PageScrollDirection.VERTICAL)
                    .scrollMode(PageScrollMode.CONTINUOUS)
                    .layoutMode(PageLayoutMode.SINGLE)
                    .build()

                val frag = PdfFragment.newInstance(documentUri, config)


                pdflist.add(frag)
                menu.add(Menu.NONE, Menu.FIRST+pdf_count, Menu.NONE, getFileName(uri))
                val item = menu.getItem(pdf_count)
                pdf_count+=1
                displayView(item)


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
    fun displayView(item:MenuItem){
        var frag:Fragment? = null
        var idx= (item.itemId - Menu.FIRST)
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
}
