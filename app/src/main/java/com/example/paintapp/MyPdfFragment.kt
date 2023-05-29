package com.example.paintapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.LinkAnnotation
import com.pspdfkit.annotations.NoteAnnotation
import com.pspdfkit.annotations.actions.ActionType
import com.pspdfkit.annotations.actions.UriAction
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.page.PageLayoutMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.configuration.page.PageScrollMode
import com.pspdfkit.document.DocumentSaveOptions
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.document.search.SearchResult
import com.pspdfkit.forms.FormType
import com.pspdfkit.listeners.DocumentListener
import com.pspdfkit.listeners.OnDocumentLongPressListener
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.PdfOutlineView
import com.pspdfkit.ui.PdfThumbnailBar
import com.pspdfkit.ui.PdfThumbnailGrid
import com.pspdfkit.ui.outline.DefaultBookmarkAdapter
import com.pspdfkit.ui.outline.DefaultOutlineViewListener
import com.pspdfkit.ui.search.PdfSearchViewModular
import com.pspdfkit.ui.search.SearchResultHighlighter
import com.pspdfkit.ui.search.SimpleSearchResultListener
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationTool
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.toolbar.AnnotationCreationToolbar
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import com.pspdfkit.utils.PdfUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MyPdfFragment : AppCompatActivity(),DocumentListener,OnDocumentLongPressListener,
    AnnotationManager.OnAnnotationCreationModeChangeListener{
    private lateinit var fragment: PdfFragment
    private lateinit var thumbnailBar: PdfThumbnailBar
    private lateinit var configuration: PdfConfiguration
    private lateinit var modularSearchView: PdfSearchViewModular
    private lateinit var thumbnailGrid: PdfThumbnailGrid
    private lateinit var highlighter: SearchResultHighlighter
    private lateinit var pdfOutlineView: PdfOutlineView
    private lateinit var annotationCreationToolbar: AnnotationCreationToolbar
    private lateinit var toolbarCoordinatorLayout : ToolbarCoordinatorLayout
    private lateinit var changeMode: Button
    private lateinit var btnAddPdf: Button
    private lateinit var answerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom_fragment)

        supportFragmentManager.beginTransaction()
            .add(R.id.frame_layout, PaintFragment())
            .addToBackStack(null)
            .commit()
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }

//        //shyum
//        val documentUri = Uri.parse(uri.toString())
//        val config = PdfConfiguration.Builder()
//            .scrollDirection(PageScrollDirection.VERTICAL)
//            .scrollMode(PageScrollMode.CONTINUOUS)
//            .layoutMode(PageLayoutMode.SINGLE)
//            .enabledAnnotationTools(enabledAnnotationTools)
//            .build()
//
//        val frag = PdfFragment.newInstance(documentUri, config)
//        //overlay mode
//
//
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.add(R.id.frame_layout, frag)
//        transaction.addToBackStack("detail")
//        transaction.commit()
//
//
//        // Wire up fragment with this custom activity and all UI components.
//        fragment.apply {
//            addDocumentListener(this@MyPdfFragment)
//            addDocumentListener(modularSearchView)
//            addDocumentListener(thumbnailBar.documentListener)
//            addDocumentListener(thumbnailGrid)
//            setOnDocumentLongPressListener(this@MyPdfFragment)
//        }
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
                //overlay mode


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

    private fun createNoteAnnotation() {
        val pageRect = RectF(180f, 692f, 212f, 660f)
        val contents = "This is note annotation was created from code."
        val icon = NoteAnnotation.CROSS
        val color = Color.GREEN

        // Create the annotation, and set the color.
        NoteAnnotation(1, pageRect, contents, icon).also { noteAnnotation ->
            noteAnnotation.color = color
            fragment.addAnnotationToPage(noteAnnotation, false)
        }
    }

    override fun onBackPressed() {
        when {
            modularSearchView.isDisplayed -> {
                modularSearchView.hide()
                return
            }
            thumbnailGrid.isDisplayed -> {
                thumbnailGrid.hide()
                return
            }
            pdfOutlineView.isDisplayed -> {
                pdfOutlineView.hide()
                return
            }
            else -> super.onBackPressed()
        }
    }

    /**
     * This method binds the thumbnail bar and the search view to the fragment, once the document is loaded.
     */
    @UiThread
    override fun onDocumentLoaded(document: PdfDocument) {
        fragment.addDocumentListener(modularSearchView)
        thumbnailBar.setDocument(document, configuration)
        modularSearchView.setDocument(document, configuration)
        pdfOutlineView.setDocument(document, configuration)
        thumbnailGrid.setDocument(document, configuration)

        // Adding note annotation to populate Annotation section in PdfOutlineView
        createNoteAnnotation()
    }

    override fun onDocumentLongPress(
        document: PdfDocument,
        @IntRange(from = 0) pageIndex: Int,
        event: MotionEvent?,
        pagePosition: PointF?,
        longPressedAnnotation: Annotation?
    ): Boolean {
        // This code showcases how to handle long click gesture on the document links.
        fragment.view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

        if (longPressedAnnotation is LinkAnnotation) {
            val action = longPressedAnnotation.action
            if (action?.type == ActionType.URI) {
                val uri = (action as UriAction).uri ?: return true
                Toast.makeText(this@MyPdfFragment, uri, Toast.LENGTH_LONG).show()
                return true
            }
        }
        return false
    }

    // Rest of the `DocumentListener` methods are unused.
    override fun onDocumentLoadFailed(exception: Throwable) = Unit

    override fun onDocumentSave(document: PdfDocument, saveOptions: DocumentSaveOptions): Boolean = true

    override fun onDocumentSaved(document: PdfDocument) = Unit

    override fun onDocumentSaveFailed(document: PdfDocument, exception: Throwable) = Unit

    override fun onDocumentSaveCancelled(document: PdfDocument) = Unit

    override fun onPageClick(
        document: PdfDocument,
        @IntRange(from = 0) pageIndex: Int,
        event: MotionEvent?,
        pagePosition: PointF?,
        clickedAnnotation: Annotation?
    ): Boolean = false

    override fun onDocumentClick(): Boolean = false

    override fun onPageChanged(document: PdfDocument, @IntRange(from = 0) pageIndex: Int) = Unit

    override fun onDocumentZoomed(document: PdfDocument, @IntRange(from = 0) pageIndex: Int, scaleFactor: Float) = Unit

    override fun onPageUpdated(document: PdfDocument, @IntRange(from = 0) pageIndex: Int) = Unit

    /**
     * Applies the `tint` color to the given `drawable`.
     */
    private fun tintDrawable(drawable: Drawable, tint: Int): Drawable {
        val tintedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(tintedDrawable, tint)
        return tintedDrawable
    }

    companion object {
        const val EXTRA_CONFIGURATION = "CustomFragmentActivity.EXTRA_CONFIGURATION"
        const val EXTRA_URI = "CustomFragmentActivity.EXTRA_URI"
    }
    override fun onEnterAnnotationCreationMode(p0: AnnotationCreationController) {}
    override fun onChangeAnnotationCreationMode(p0: AnnotationCreationController) {}
    override fun onExitAnnotationCreationMode(p0: AnnotationCreationController) {}

}