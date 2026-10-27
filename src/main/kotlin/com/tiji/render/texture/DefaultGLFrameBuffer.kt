package com.tiji.render.texture

import com.tiji.render.Window

object DefaultGLFrameBuffer : AbstractGLFrameBuffer() {
    override val fbo = 0
    override val glTexture: GLTexture? = null

    override val width: Int get() = Window.width
    override val height: Int get() = Window.height

    init {
        markNonResource(this)
    }
}