package cc.tijiisreal.render.buffer

import cc.tijiisreal.render.Resource
import cc.tijiisreal.render.Utils
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL30C
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

    /**
     * Bind this buffer as different buffer type
     *
     * @since 1.0
     */
    fun bindAs(type: Int) {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        GL15C.glBindBuffer(type, bufferId)
    }

    /**
     * Bind this buffer for shader to read
     *
     * @since 1.0
     */
    fun bindBufferBase(location: Int) {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        GL30C.glBindBufferBase(type, location, bufferId)
    }

    override fun destroy() {
        super.destroy()

        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        GL15C.glDeleteBuffers(bufferId)
    }
}