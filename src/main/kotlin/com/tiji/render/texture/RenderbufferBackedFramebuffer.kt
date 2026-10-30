package com.tiji.render.texture

import com.tiji.render.Utils
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30C

class RenderbufferBackedFramebuffer(
    val renderbuffer: Array<GLRenderBuffer>,
    val depthRenderbuffer: GLRenderBuffer?): AbstractGLFrameBuffer() {
    override val fbo: Int = GL30C.glGenFramebuffers()
    override val glTexture: GLTexture?
        get() = null

    init {
        Utils.ensureOpenGLVersion(3 to 3)

        renderbuffer.forEach { this.attach(it) }

        bind()
        renderbuffer.forEachIndexed { index, buffer ->
            GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER,
                GL30C.GL_COLOR_ATTACHMENT0 + index,
                GL30C.GL_RENDERBUFFER,
                buffer.rbo, 0)
        }

        depthRenderbuffer?.let {
            GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER,
                GL30C.GL_DEPTH_ATTACHMENT,
                GL30C.GL_RENDERBUFFER,
                it.rbo, 0)
        }
    }

    fun verifyBuffer() {
        bind()
        val framebufferStatus = GL30C.glCheckFramebufferStatus(GL30C.GL_FRAMEBUFFER)
        require(framebufferStatus == GL30.GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete: $framebufferStatus" }
    }
}