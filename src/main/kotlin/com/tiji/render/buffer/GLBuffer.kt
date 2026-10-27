package com.tiji.render.buffer

import com.tiji.render.Utils
import org.lwjgl.opengl.GL44C
import java.nio.ByteBuffer

/**
 * A GL buffer object, managed using modern [GL44C.glBufferStorage].
 * Much more efficient than [LegacyGLBuffer] but cannot be resized after creation.
 * Unless you have specific reason, use this instead.
 *
 * **Requires:** OpenGL 4.4+
 *
 * @since 1.0
 */
class GLBuffer private constructor(type: Int, val dynamic: Boolean) : AbstractGLBuffer(type) {
    init {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(4 to 4)

        bind()
    }

    /**
     * @param type The type of buffer (GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY)
     * @param dynamic Whether the buffer is dynamic. Non-dynamic buffers cannot call [subdata]
     * @param data The initial data for the buffer
     */
    constructor(type: Int, dynamic: Boolean, data: ByteBuffer) : this(type, dynamic) {
        bind()
        GL44C.glBufferStorage(type, data, GL44C.GL_DYNAMIC_STORAGE_BIT*(if (dynamic) 1 else 0))
        unbind()
    }

    /**
     * Note that using this method will not populate the buffer data.
     *
     * @param type The type of buffer (GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY)
     * @param dynamic Whether the buffer is dynamic. Non-dynamic buffers cannot call [subdata]
     * @param size size of buffer in bytes
     */
    constructor(type: Int, dynamic: Boolean, size: Int) : this(type, dynamic) {
        bind()
        GL44C.glBufferStorage(type, size.toLong(), GL44C.GL_DYNAMIC_STORAGE_BIT*(if (dynamic) 1 else 0))
        unbind()
    }

    /**
     * Updates a portion of the buffer's data.
     * Will fail if the buffer is not set to be dynamic.
     *
     * @param offset offset in bytes from the beginning of the buffer to update
     * @param data data to update
     * @since 1.0
     */
    override fun subdata(offset: Int, data: ByteBuffer) {
        require(dynamic)

        super.subdata(offset, data)
    }
}