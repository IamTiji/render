package com.tiji.render.texture

import com.tiji.render.Utils
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30C

/**
 * A framebuffer backed by a texture
 *
 * **Requires:** OpenGL 3.3+
 */
class TextureBackedFrameBuffer(val texture: GLTexture): AbstractGLFrameBuffer() {
    val backingTexture: GLTexture
        get() = texture

    override val fbo: Int = GL30C.glGenFramebuffers()
    override val glTexture: GLTexture
        get() = texture

    init {
        Utils.ensureOpenGLVersion(3 to 3)

        texture.attach(this)

        bind()
        GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER,
                                     GL30C.GL_COLOR_ATTACHMENT0,
                                     GL30C.GL_TEXTURE_2D,
                                     texture.textureId, 0)
    }

    fun verifyBuffer() {
        bind()
        val framebufferStatus = GL30C.glCheckFramebufferStatus(GL30C.GL_FRAMEBUFFER)
        require(framebufferStatus == GL30.GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete: $framebufferStatus" }
    }
}