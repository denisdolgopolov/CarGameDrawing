package d.dolg.cardrawing

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World


object MyWorld {
    val world = World(Vector2(0f, -10f), true)
}