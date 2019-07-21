package d.dolg.cardrawing.draw_view

import android.graphics.Color

data class PaintOptions(var color: Int = Color.BLUE,
                        var strokeWidth: Float = 8f,
                        var alpha: Int = 255,
                        var isEraserOn: Boolean = false)
