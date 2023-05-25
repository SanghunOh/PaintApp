package com.example.paintapp

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display.Mode
import android.view.Window
import android.widget.ImageView
import android.widget.TextView

import org.w3c.dom.Text

class ModelPopupActivity(private val context : AppCompatActivity) {

    private val dlg = Dialog(context)
    private lateinit var answer_text : TextView
    private lateinit var closeBtn : ImageView
    private lateinit var minimizeBtn : ImageView
    private var isMinimized: Boolean = false





    fun show(content : String) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.activity_model_popup)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        answer_text = dlg.findViewById(R.id.answer_field_textview)
        answer_text.text = content

        closeBtn = dlg.findViewById(R.id.closepopup)
        closeBtn.setOnClickListener {
            dlg.dismiss()
        }
        minimizeBtn = dlg.findViewById(R.id.minimizepopup)
        dlg.show()
    }
}