package com.tiji.render.texture

import com.tiji.render.Utils
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

/**
 * Wrapper for OpenGL textures.
 *
 * @since 1.0
 */
class GLTexture: AbstractGLTexture {
    companion object {
        init {
            STBImage.stbi_set_flip_vertically_on_load(true);
        }

        /**
         * Creates texture from an image file
         *
         * @param filename The path to the image file
         * @param pixelated Whether the texture should use nearest or linear filtering
         * @param stat1c Whether if texture is ever scaled or not
         *
         * @since 1.0
         */
        fun fromFile(filename: String, pixelated: Boolean, stat1c: Boolean): GLTexture {
            return fromBytes(Utils.readResource(filename), pixelated, stat1c)
        }

        /**
         * Creates texture from in-memory image file
         *
         * @param bytes The bytes of image file
         * @param pixelated Whether the texture should use nearest or linear filtering
         * @param stat1c Whether if texture is ever scaled or not
         *
         * @since 1.0
         */
        fun fromBytes(bytes: ByteArray, pixelated: Boolean, stat1c: Boolean): GLTexture {
            Utils.ensureThread()

            val glTexture: GLTexture

            MemoryStack.stackPush().use { stack ->
                val width = stack.mallocInt(1)
                val height = stack.mallocInt(1)
                val channels = stack.mallocInt(1)

                val buffer = BufferUtils.createByteBuffer(bytes.size)
                buffer.put(bytes).flip()

                val data = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4)
                requireNotNull(data) { "Failed to load image" }

                data.position(data.capacity())
                data.flip()

                glTexture = GLTexture(width.get(0), height.get(0), data, pixelated, stat1c)

                STBImage.stbi_image_free(data)
            }
            return glTexture
        }
    }

    /**
     * Creates texture using raw bytes
     *
     * @param width The width of the texture
     * @param height The height of the texture
     * @param data The raw bytes of the texture
     * @param pixelated Whether the texture should use nearest or linear filtering
     * @param stat1c Whether if texture is ever scaled or not
     *
     * @since 1.0
     */
    constructor(width: Int, height: Int, data: ByteBuffer?, pixelated: Boolean, stat1c: Boolean) : super(
            width, height, data, pixelated, stat1c,
            GL11C.GL_RGB8, GL11C.GL_UNSIGNED_BYTE, GL11C.GL_RGBA
            )
}