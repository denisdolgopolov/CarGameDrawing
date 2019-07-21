package d.dolg.cardrawing.activities

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler

import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import d.dolg.cardrawing.Cnst
import d.dolg.cardrawing.GroundPlayer
import d.dolg.cardrawing.draw_view.DrawView
import d.dolg.cardrawing.R
import java.util.*
import kotlin.math.round

class DrawingTrackActivity : AppCompatActivity() {
    private var drawingCanvas: DrawView? = null
    private var parentView: RelativeLayout?  = null
    private var speedScrolling = 0
    private var handlerScrolling: Handler? = null
    private val colorBackground = Color.WHITE
    private var miniMap: ImageView? = null
    private var parentMiniMap: CardView? = null
    private val display = Point()
    private var dialogWaiting: AlertDialog? = null
    private var handlerWaitingBitmap: Handler? = null
    private val paint = Paint()

    companion object {
        var isMapOpen = false
        var isStartSelect = false
        var isFinishSelect = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentView = LayoutInflater.from(this)
                .inflate(R.layout.activity_drawing_track, null) as RelativeLayout
        setContentView(parentView)
        windowManager.defaultDisplay.getSize(display)
        speedScrolling = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                30f, resources.displayMetrics).toInt()

        //mini map
        miniMap = findViewById(R.id.drawing_mini_map)
        parentMiniMap = findViewById(R.id.parent_mini_map)
        setDefaultMiniMap()
        setPaint()

        handlerWaitingBitmap = Handler {
            miniMap?.setImageBitmap(it.obj as Bitmap)
            closeDialogWaiting()
            true
        }

        miniMap?.setOnClickListener {
            if(!isMapOpen) {
                startLoadingDialog()
                setOpenMiniMap()
            } else {
                setDefaultMiniMap()
            }
            isMapOpen = !isMapOpen
        }
        //mini map


        val listDrawingTool = findViewById<LinearLayout>(R.id.list_drawing_tool)
        drawingCanvas = findViewById<DrawView>(R.id.drawing_canvas)
        drawingCanvas?.setBackgroundColor(colorBackground)
        drawingCanvas?.setStrokeWidth(15F)
        drawingCanvas?.setColor(Color.BLUE)


        val bUp = findViewById<ImageButton>(R.id.b_up)
        val bDown = findViewById<ImageButton>(R.id.b_down)
        val bRight = findViewById<ImageButton>(R.id.b_right)
        val bLeft = findViewById<ImageButton>(R.id.b_left)

        //handlerScrolling
        val paramsParent = RelativeLayout.LayoutParams(display.x, display.y)
        handlerScrolling = Handler {
            val translate = it.what
            when(it.obj.toString()) {
                "right" -> {
                    if(translate >= paramsParent.marginEnd) return@Handler true
                    paramsParent.marginEnd -= speedScrolling
                    paramsParent.width += speedScrolling
                }
                "down" -> {
                    if(translate >= paramsParent.bottomMargin) return@Handler true
                    paramsParent.bottomMargin -= speedScrolling
                    paramsParent.height += speedScrolling
                }
            }
            drawingCanvas?.layoutParams = paramsParent
            true
        }
        //handlerScrolling


        bUp.setOnTouchListener(ScrollingTouchListener(drawingCanvas!!, false, speedScrolling, handlerScrolling!!))
        bDown.setOnTouchListener(ScrollingTouchListener(drawingCanvas!!, false, -speedScrolling, handlerScrolling!!))
        bRight.setOnTouchListener(ScrollingTouchListener(drawingCanvas!!, true, speedScrolling, handlerScrolling!!))
        bLeft.setOnTouchListener(ScrollingTouchListener(drawingCanvas!!, true, -speedScrolling, handlerScrolling!!))

        val gameType = intent.extras?.get(Cnst.game_type) as Cnst.GameTypes

        listDrawingTool.addView(generateDrawingTools(R.string.brush))
        listDrawingTool.addView(generateDrawingTools(R.string.eraser))
        listDrawingTool.addView(generateDrawingTools(R.string.undo))
        listDrawingTool.addView(generateDrawingTools(R.string.redo))
        listDrawingTool.addView(generateDrawingTools(R.string.start))
        listDrawingTool.addView(generateDrawingTools(R.string.finish))
        listDrawingTool.addView(generateDrawingTools(R.string.colorPicker))

        val bStart = findViewById<ImageButton>(R.id.b_start_game)
        bStart.setOnClickListener {
            /*if(!drawingCanvas!!.isStartAdd()) {
                createErrorDialog("start don't add")
                return@setOnClickListener
            }
            if(!drawingCanvas!!.isFinishAdd()) {
                createErrorDialog("finish don't add")
                return@setOnClickListener
            } */

            //start gdx world
            //make ground
            GroundPlayer.makeBodies(drawingCanvas!!.listLine, display.y)

            val intent = Intent()
            intent.setClass(this, AndroidLauncher::class.java)
            intent.putExtra(Cnst.game_type, gameType)
            startActivity(intent)
        }
    }

    private fun generateDrawingTools(nameTool: Int) : View {
        val card = LayoutInflater.from(this).inflate(R.layout.one_tool_drawing, parentView, false)
        val imageViewTool = card.findViewById<ImageView>(R.id.image_drawing_tool)
        val textTool = card.findViewById<TextView>(R.id.name_drawing_tool)

        val imageResource = when(nameTool) {
            R.string.brush -> R.drawable.brush
            R.string.eraser ->R.drawable.eraser
            R.string.start -> R.drawable.start
            R.string.finish -> R.drawable.finish
            R.string.undo -> R.drawable.undo
            R.string.redo -> R.drawable.ic_redo
            R.string.colorPicker -> R.drawable.ic_color_picker
            else -> 0
        }
        imageViewTool.setImageResource(imageResource)

        card.setOnClickListener {
            isStartSelect = false
            isFinishSelect = false
            when(nameTool) {
                R.string.brush -> {
                    drawingCanvas?.setColor(Color.BLUE)
                    card.setOnLongClickListener {
                        createDialogSelectWidthDrawing()
                        false
                    }
                }
                R.string.eraser -> {
                    drawingCanvas?.setColor(colorBackground)
                    card.setOnLongClickListener {
                        createDialogSelectWidthDrawing()
                        false
                    }
                }
                R.string.undo -> drawingCanvas?.undo()
                R.string.redo -> drawingCanvas?.redo()
                R.string.start -> {
                    isStartSelect = true
                }
                R.string.finish -> {
                    isFinishSelect = true
                }
                R.string.colorPicker -> {
                    ColorPickerDialogBuilder.with(this)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .setPositiveButton("Save") { _, color, _ ->
                                drawingCanvas?.setColor(color)
                            }
                            .setNegativeButton("Cancel", null)
                            .build()
                            .show()
                }
            }
        }

        textTool.text = resources.getString(nameTool)
        return card
    }

    private fun createDialogSelectWidthDrawing() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_select_width, null)
        val seekBar = view.findViewById<SeekBar>(R.id.seek_bar_select_width)
        var width = 0F
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                width = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("Cancael", null)
                .setPositiveButton("Ok") { _, _ ->
                    drawingCanvas?.setStrokeWidth(width)
                }
                .create()
                .show()
    }


    //MINI MAP
    private fun setDefaultMiniMap() {
        miniMap?.layoutParams?.width = display.x/10
        miniMap?.layoutParams?.height = display.y/10
        miniMap?.setImageResource(R.drawable.ic_map)
        parentMiniMap?.elevation = 0F
    }

    private fun setOpenMiniMap() {
        Thread {
            parentMiniMap?.elevation = 5F
            miniMap?.layoutParams?.width = round(display.x / 1.1).toInt()
            miniMap?.layoutParams?.height = round(display.y / 1.1).toInt()

            val bitmap = Bitmap.createBitmap(drawingCanvas!!.width,
                    drawingCanvas!!.height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            drawingCanvas?.mPaths?.keys?.forEach {
                val value = drawingCanvas?.mPaths?.get(it)
                paint.color = value!!.color
                paint.strokeWidth = value.strokeWidth
                canvas.drawPath(it, paint)
            }
            val scaled = Bitmap.createScaledBitmap(bitmap,
                    miniMap!!.layoutParams.width,
                    miniMap!!.layoutParams.height,
                    false)

            handlerWaitingBitmap?.sendMessage(handlerWaitingBitmap?.obtainMessage(0, scaled))
        }.start()
    }
    //MINI MAP


    //LOADING DIALOG
    private fun startLoadingDialog() {
        val progressBar = LayoutInflater.from(this).inflate(R.layout.dialog_waiting, null)
        dialogWaiting = AlertDialog.Builder(this@DrawingTrackActivity)
                .setView(progressBar)
                .setCancelable(false)
                .create()
        dialogWaiting?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogWaiting?.show()
    }

    private fun closeDialogWaiting() = dialogWaiting?.dismiss()
    //LOADING DIALOG


    private fun hideActionBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun setPaint() = paint.apply {
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
    }

    private fun createErrorDialog(error: String) {
        AlertDialog.Builder(this).setMessage(error)
                .setPositiveButton("ok", null)
                .create()
                .show()
    }


    override fun onResume() {
        super.onResume()
        hideActionBar()
    }


    private class TimerTaskScrolling(private val drawingView: DrawView,
                                     private val axis: Boolean, //true - x, false - y
                                     private val speed: Int,
                                     private val handler: Handler) : TimerTask() {
        private var startWidthDrawingView = 0
        init {
            startWidthDrawingView = drawingView.layoutParams.width
        }

        override fun run() {
            if(axis) {
                drawingView.translationX += speed
                if(speed < 0) handler.sendMessage(handler.obtainMessage(drawingView.translationX.toInt(),"right"))
            } else {
                drawingView.translationY += speed
                if(speed < 0) handler.sendMessage(handler.obtainMessage(drawingView.translationY.toInt(),"down"))
            }
        }
    }


    private class ScrollingTouchListener(private val drawingView: DrawView,
                                         private val axis: Boolean,
                                         private val speed: Int,
                                         private val handler: Handler) : View.OnTouchListener {
        private var timer = Timer()

        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if(isMapOpen) return true
            if(event.action == MotionEvent.ACTION_DOWN) {
                timer = Timer()
                timer.schedule(TimerTaskScrolling(drawingView, axis, speed, handler), 0, 50)
            } else {
                timer.cancel()
            }
            return true
        }
    }
}