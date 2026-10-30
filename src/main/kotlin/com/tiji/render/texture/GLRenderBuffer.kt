package com.tiji.render.texture

import com.tiji.render.Resource
import com.tiji.render.Utils
import org.lwjgl.opengl.GL30C

/**
 * Wrapper for RenderBuffers
 */
class GLRenderBuffer(
    val width: Int,
    val height: Int,
    val format: Int,
): Resource() {
    val rbo: Int

    init {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        rbo = GL30C.glGenRenderbuffers()
        bind()
        GL30C.glRenderbufferStorage(GL30C.GL_RENDERBUFFER, format, width, height)
    }

    /**
     * Binds the render buffer
     *
     * @since 1.0
     */
    fun bind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, rbo)
    }

    /**
     * Unbinds the render buffer
     *
     * @since 1.0
     */
    fun unbind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0)
    }
}