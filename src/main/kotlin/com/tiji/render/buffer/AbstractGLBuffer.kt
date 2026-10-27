package com.tiji.render.buffer

import com.tiji.render.Resource
import com.tiji.render.Utils
import org.lwjgl.opengl.GL15C
import java.nio.ByteBuffer

sealed class AbstractGLBuffer(val type: Int) : Resource() {
    protected val bufferId: Int = GL15C.glGenBuffers()

    /**
     * Updates a portion of the buffer data.
     *
     * @param offset offset in bytes from the beginning of the buffer to update
     * @param data data to update
     * @since 1.0
     */
    open fun subdata(offset: Int, data: ByteBuffer) {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        bind()
        GL15C.glBufferSubData(type, offset.toLong(), data)
    }

    /**
     * Binds buffer.
     *
     * @since 1.0
     */
    fun bind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        GL15C.glBindBuffer(type, bufferId)
    }

    /**
     * Unbinds buffer.
     *
     * @since 1.0
     */
    fun unbind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        GL15C.glBindBuffer(type, 0)
    }

    override fun destroy() {
        super.destroy()

        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        GL15C.glDeleteBuffers(bufferId)
    }
}