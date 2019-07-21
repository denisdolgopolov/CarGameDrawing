package d.dolg.cardrawing

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d.*
import d.dolg.cardrawing.ground_manager.GroundSaver
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


object GroundPlayer {
    private val def = BodyDef()

    init {
        def.fixedRotation = true
        def.type = BodyDef.BodyType.StaticBody
    }

    fun makeBodies(listLine: ArrayList<GroundSaver>, displayHeight: Int) {
        listLine.forEach {line ->
            var x1 = line.listDote[0].x
            var y1 = displayHeight - line.listDote[0].y
            for(i in 1 until line.listDote.size) {
                val body = MyWorld.world.createBody(def)

                val dote = line.listDote[i]
                val shape = EdgeShape()

                val x2 = dote.x
                val y2 = displayHeight - dote.y
                val length = calculateLength(x1, y1, x2, y2)

                shape.set(-length/2, 0f, length/2, 0f)
                val deltaX = x2-x1
                val deltaY = y2-y1
                val angle = atan2(deltaY, deltaX)
                body.setTransform(x1 + deltaX/2, y1 + deltaY/2, angle)

                x1 = x2
                y1 = y2

                body.createFixture(shape, 0f)
                shape.dispose()
            }
        }
    }

    private fun calculateLength(x1: Float, y1: Float, x2: Float, y2: Float) : Float {
        val deltaX = abs(x2 - x1)
        val deltaY = abs(y2 - y1)
        return sqrt(deltaX*deltaX + deltaY*deltaY)
    }



}
