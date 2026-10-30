package cc.tijiisreal.render.texture

import cc.tijiisreal.render.Window

object DefaultGLFrameBuffer : AbstractGLFrameBuffer() {
    override val fbo = 0
    override val glTexture: GLTexture? = null

    override val width: Int get() = Window.width
    override val height: Int get() = Window.height

    init {
        markNonResource(this)
    }
}