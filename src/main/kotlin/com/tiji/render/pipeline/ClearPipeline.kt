package com.tiji.render.pipeline

import com.tiji.render.texture.AbstractGLFrameBuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11C

class ClearPipeline(var red  : Float,
                    var green: Float,
                    var blue : Float,
                    var alpha: Float) : AbstractPipeline() {

    constructor(red: Float, green: Float, blue: Float, fbo: AbstractGLFrameBuffer): this(red, green, blue, 1.0f) {
        super.fbo = fbo
    }
    constructor(fbo: AbstractGLFrameBuffer): this(0f, 0f, 0f, fbo)

    var depth: Double? = null

    override fun render() {
        fbo.bind()

        depth?.let {
            GL11C.glClearDepth(it)
            GL11C.glClear(GL11.GL_COLOR_BUFFER_BIT)
        }

        GL11C.glClearColor(red, green, blue, alpha)
        GL11C.glClear(GL11.GL_COLOR_BUFFER_BIT)
    }

    override fun destroy() {
        super.destroy()
    }
}