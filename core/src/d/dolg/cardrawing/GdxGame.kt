package d.dolg.cardrawing

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer

class GdxGame : Game() {
    private var camera: OrthographicCamera? = null
    private var renderer: Box2DDebugRenderer? = null


    override fun create() {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        camera = OrthographicCamera(width, height)
        camera?.position?.set(width/2, height/2, 0f)
        camera?.update()

        renderer = Box2DDebugRenderer()
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 232F/255)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer?.render(MyWorld.world, camera?.combined)
        MyWorld.world.step(1f/60f, 4, 4)
    }

    override fun pause() {
        super.pause()
    }

    override fun resume() {
        super.resume()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
    }

    override fun dispose() {
        MyWorld.world.dispose()
    }
}