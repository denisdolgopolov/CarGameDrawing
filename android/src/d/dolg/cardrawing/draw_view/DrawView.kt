package d.dolg.cardrawing.draw_view
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import d.dolg.cardrawing.R
import d.dolg.cardrawing.activities.DrawingTrackActivity
import d.dolg.cardrawing.ground_manager.GroundSaver
import java.util.LinkedHashMap

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var mPaths = LinkedHashMap<MyPath, PaintOptions>()

    val listLine = ArrayList<GroundSaver>()
    private var groundSaver: GroundSaver = GroundSaver()

    private var mLastPaths = LinkedHashMap<MyPath, PaintOptions>()
    private var mUndonePaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mPaint = Paint()
    private var mPath = MyPath()
    private var mPaintOptions = PaintOptions()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var mIsSaving = false
    private var mIsStrokeWidthBarEnabled = false

    private var xFinish = 0f
    private var yFinish = 0f
    private var xStart = 0f
    private var yStart = 0f
    private var finishDrawable: Drawable? = null
    private var startDrawable: Drawable? = null

    init {
        mPaint.apply {
            color = mPaintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mPaintOptions.strokeWidth
            isAntiAlias = true
        }
        finishDrawable = VectorDrawableCompat.create(resources, R.drawable.finish, null)
        finishDrawable?.setBounds(0, 0, 100, 100)
        startDrawable =  VectorDrawableCompat.create(resources, R.drawable.start, null)
        startDrawable?.setBounds(0, 0, 100, 100)
    }



    fun undo() {
        if (mPaths.isEmpty() && mLastPaths.isNotEmpty()) {
            mPaths = mLastPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
            mLastPaths.clear()
            invalidate()
            return
        }
        if (mPaths.isEmpty()) {
            return
        }
        val lastPath = mPaths.values.lastOrNull()
        val lastKey = mPaths.keys.lastOrNull()

        mPaths.remove(lastKey)
        if (lastPath != null && lastKey != null) {
            mUndonePaths[lastKey] = lastPath
        }
        invalidate()
    }

    fun redo() {
        if (mUndonePaths.keys.isEmpty()) {
            return
        }

        val lastKey = mUndonePaths.keys.last()
        addPath(lastKey, mUndonePaths.values.last())
        mUndonePaths.remove(lastKey)
        invalidate()
    }

    fun setColor(newColor: Int) {
        @ColorInt
        val alphaColor = ColorUtils.setAlphaComponent(newColor, mPaintOptions.alpha)
        mPaintOptions.color = alphaColor
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun setAlpha(newAlpha: Int) {
        val alpha = (newAlpha*255)/100
        mPaintOptions.alpha = alpha
        setColor(mPaintOptions.color)
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        mPaintOptions.strokeWidth = newStrokeWidth
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun getBitmap(quality: Bitmap.Config) : Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, quality)
        val canvas = Canvas(bitmap)
        mIsSaving = true
        draw(canvas)
        mIsSaving = false
        return bitmap
    }


    fun addPath(path: MyPath, options: PaintOptions) {
        mPaths[path] = options
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for ((key, value) in mPaths) {
            changePaint(value)
            canvas.drawPath(key, mPaint)
        }

        if(isFinishAdd()) {
            canvas.translate(xFinish, yFinish)
            finishDrawable?.draw(canvas)
            canvas.translate(-xFinish, -yFinish)
        }

        if(isStartAdd()) {
            canvas.translate(xStart, yStart)
            startDrawable?.draw(canvas)
            canvas.translate(-xStart, -yStart)
        }


        changePaint(mPaintOptions)
        canvas.drawPath(mPath, mPaint)
    }

    fun isStartAdd() = xStart != 0f && yStart != 0f && startDrawable != null
    fun isFinishAdd() = xFinish != 0f && yFinish != 0f && finishDrawable != null


    private fun changePaint(paintOptions: PaintOptions) {
        mPaint.color = paintOptions.color
        mPaint.strokeWidth = paintOptions.strokeWidth
    }

    fun clearCanvas() {
        mLastPaths = mPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
        mPath.reset()
        mPaths.clear()
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mCurX = x
        mCurY = y
        groundSaver.listDote.add(GroundSaver.Dote(x, y))
    }

    private fun actionMove(x: Float, y: Float) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
        mCurX = x
        mCurY = y
        groundSaver.listDote.add(GroundSaver.Dote(x, y))
    }

    private fun actionUp() {
        mPath.lineTo(mCurX, mCurY)

        // draw a dot on click
        if (mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY)
        }

        mPaths.put(mPath, mPaintOptions)
        mPath = MyPath()
        mPaintOptions = PaintOptions(mPaintOptions.color, mPaintOptions.strokeWidth, mPaintOptions.alpha)

        listLine.add(groundSaver)
        groundSaver = GroundSaver()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(DrawingTrackActivity.isMapOpen) return true
        val x = event.x
        val y = event.y

        if(DrawingTrackActivity.isFinishSelect) {
            xFinish = x
            yFinish = y
            invalidate()
            return true
        } else if(DrawingTrackActivity.isStartSelect) {
            xStart = x
            yStart = y
            invalidate()
            return true
        }



        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                actionDown(x, y)
                mUndonePaths.clear()
            }
            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }

        invalidate()
        return true
    }
}