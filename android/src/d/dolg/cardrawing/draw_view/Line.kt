package d.dolg.cardrawing

import android.graphics.Path
import d.dolg.cardrawing.draw_view.Action
import java.io.Writer
import java.security.InvalidParameterException

class Line(val x: Float, val y: Float) : Action {

    override fun perform(path: Path) {
        path.lineTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("L$x,$y")
    }
}