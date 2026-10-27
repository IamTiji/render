package com.tiji.render.texture

import com.tiji.render.Resource
import com.tiji.render.Utils
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL11C.glTexImage2D
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30C
import java.nio.ByteBuffer
import kotlin.math.ln
import kotlin.math.min

/**
 * Wrapper for OpenGL textures.
 *
 * @since 1.0
 */
abstract class AbstractGLTexture : Resource, Resizable {
    companion object {
        private const val REQ_MIPMAP_MIN_PIXEL = 8
    }

    private var _textureId: Int = -1
    val textureId: Int get() = _textureId
    var width: Int = -1
    var height: Int = -1

    val internalFormat: Int
    val type: Int
    val format: Int

    /**
     * Creates texture using raw bytes
     *
     * @param width The width of the texture
     * @param height The height of the texture
     * @param data The raw bytes of the texture
     * @param pixelated Whether the texture should use nearest or linear filtering
     * @param stat1c Whether if texture is ever scaled or not
     * @param internalFormat OpenGL field
     * @param type OpenGL field
     * @param format OpenGL field
     *
     * @since 1.0
     */
    constructor(width: Int,
                height: Int,
                data: ByteBuffer?,
                pixelated: Boolean,
                stat1c: Boolean,
                internalFormat: Int,
                type: Int,
                format: Int) : super() {
        Utils.ensureThread()

        this.internalFormat = internalFormat
        this.format = format
        this.type = type

        this.width = width
        this.height = height

        val minSize = min(width, height)

        _textureId = GL11C.glGenTextures()
        bind()
        glTexImage2D(
            GL11C.GL_TEXTURE_2D,
            0,
            internalFormat,
            width,
            height,
            0,
            format,
            type,
            data
        )

        GL11C.glTexParameteri(
            GL11C.GL_TEXTURE_2D,
            GL11C.GL_TEXTURE_MAG_FILTER,
            if (pixelated) GL11C.GL_NEAREST else GL11C.GL_LINEAR
        )
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER,
            if (stat1c) GL11C.GL_LINEAR else GL11C.GL_NEAREST_MIPMAP_LINEAR)

        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT)
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT)

        if (!stat1c) GL30C.glGenerateMipmap(GL11C.GL_TEXTURE_2D)

        unbind()
    }

    /**
     * Resizes the texture by discarding the data inside.
     * Mipmap is not re-generated.
     */
    override fun resize(width: Int, height: Int) {
        bind()
        this.width = width
        this.height = height
        glTexImage2D(
            GL11C.GL_TEXTURE_2D,
            0,
            internalFormat,
            width,
            height,
            0,
            format,
            type,
            0L
        )

        GL30C.glGenerateMipmap(GL11C.GL_TEXTURE_2D)
    }

    /**
     * Binds the texture
     *
     * @since 1.0
     */
    fun bind() {
        Utils.ensureThread()

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, _textureId)
    }

    /**
     * Unbinds the texture
     *
     * @since 1.0
     */
    fun unbind() {
        Utils.ensureThread()

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0)
    }

    /**
     * Binds the texture to texture slot
     *
     * @param channel The texture slot to bind to (0-7)
     * @since 1.0
     */
    fun bindToChannel(channel: Int) {
        Utils.ensureThread()

        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + channel)
        bind()
    }

    override fun destroy() {
        super.destroy()

        Utils.ensureThread()

        GL11C.glDeleteTextures(_textureId)
    }
}