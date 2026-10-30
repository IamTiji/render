package cc.tijiisreal.render.texture

import cc.tijiisreal.render.Resource
import cc.tijiisreal.render.Utils
import org.lwjgl.opengl.GL30C.*

abstract class AbstractGLFrameBuffer : Resource() {
    abstract val fbo: Int
    abstract val glTexture: GLTexture?
    open val width: Int get() = glTexture!!.width
    open val height: Int get() = glTexture!!.height

    /**
     * Binds the frame buffer for rendering.
     */
    fun bind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    }

    /**
     * Unbinds the frame buffer.
     */
    fun unbind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    override fun destroy() {
        super.destroy()

        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        glDeleteFramebuffers(fbo)
    }
}