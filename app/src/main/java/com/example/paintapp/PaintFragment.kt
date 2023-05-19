package com.example.paintapp

import android.content.Context
import android.graphics.Color
import android.graphics.Path
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import com.example.paintapp.data.PaintCanvas
import com.example.paintapp.UI.PaintView


class PaintFragment : Fragment(), CustomEventListener {
    private lateinit var mainActivity : MainActivity
    private lateinit var paintView: PaintView
    private lateinit var viewModel: PaintViewModel

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

        viewModel = ViewModelProvider(this)[PaintViewModel::class.java]
        viewModel.pathList.observe(viewLifecycleOwner, Observer<ArrayList<CustomPath>> {paths ->
            paintView.pathList = paths
        })

        viewModel.addFile(PaintCanvas(1, "Blank"))

        val selectBrush = mainActivity.findViewById<ImageView>(R.id.selectBrush)
        val brushBtnGroup = mainActivity.findViewById<LinearLayout>(R.id.brushGroup)
        val redBtn = mainActivity.findViewById<ImageButton>(R.id.redColor)
        val blueBtn = mainActivity.findViewById<ImageButton>(R.id.blueColor)
        val blackBtn = mainActivity.findViewById<ImageButton>(R.id.blackColor)
        val clearBtn = mainActivity.findViewById<ImageView>(R.id.clear)

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

        Log.d("paintf","no paint_view_include")
        val paintViewInclude = view.findViewById<FrameLayout>(R.id.paint_view_include)
        if (paintViewInclude != null) {
            println(paintViewInclude)
            Log.d("paintf","no paint_view_include")
        }
        Log.d("paintf", "no paint_view")
        paintView = paintViewInclude.findViewById(R.id.paint_view)
        if (paintViewInclude.findViewById<View>(R.id.paint_view) == null) {
            Log.d("paintf", "no paint_view")
        }
        paintView = view.findViewById(R.id.paint_view)
        paintView.setCustomEventListener(this)
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
}