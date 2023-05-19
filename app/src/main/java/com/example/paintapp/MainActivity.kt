package com.example.paintapp

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.Button
import android.widget.TextView
import android.net.Uri
import androidx.activity.result.ActivityResult
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.ui.PdfActivity
import com.pspdfkit.ui.PdfFragment

const val PICK_PDF_FILE = 1001
class MainActivity : AppCompatActivity() {
//    private lateinit var toolbar : Toolbar

    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var btnAddPdf: Button
    private lateinit var tvStorageInfo: TextView

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

//    private fun currentColor(color: Int){
//        strokePaint.color = color
//        path = Path()
//    }

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
                /*
                val documentUri = Uri.parse(uri.toString())
                val config = PdfActivityConfiguration.Builder(this).build()
                PdfActivity.showDocument(this,documentUri,config)

                 */
            }
        }
    }



}
