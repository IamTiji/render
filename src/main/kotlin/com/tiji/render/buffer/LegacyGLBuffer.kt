package com.tiji.render.buffer

import com.tiji.render.Utils
import org.lwjgl.opengl.GL15C
import java.nio.ByteBuffer

/**
 * Represents a buffer object in OpenGL.
 * If you use OpenGL 4.4+, you should use [GLBuffer] instead.
 *
 * * **Requires:** OpenGL 1.5+
 */
class LegacyGLBuffer private constructor(type: Int, var useStrategy: Int) : AbstractGLBuffer(type) {
    init {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        bind()
    }

    /**
     * ### `useStrategy`
     * Combination of `STATIC`, `DYNAMIC`, `STREAM` and `READ`, `COPY` and `DRAW`
     * * **`STATIC`:** Buffer data will not change.
     * * **`DYNAMIC`:** Buffer data may change.
     * * **`STREAM`:** Buffer data may change frequently.
     * * **`READ`:** Buffer data are read by CPU (GPU -> CPU).
     * * **`COPY`:** Buffer data copied across GPU (GPU -> GPU).
     * * **`DRAW`:** Buffer data are written by GPU (CPU -> GPU).
     *
     * @param type type of buffer (e.g., [GL15C.GL_ARRAY_BUFFER])
     * @param useStrategy use strategy for buffer data (e.g., [GL15C.GL_STATIC_READ])
     * @param data data to initialize buffer with
     *
     * @since 1.0
     */
    constructor(type: Int, useStrategy: Int, data: ByteBuffer) : this(type, useStrategy) {
        GL15C.glBindBuffer(type, bufferId)
        GL15C.glBufferData(type, data, useStrategy)
        GL15C.glBindBuffer(type, 0)
    }

    /**
     * Note that using this method will not populate the buffer data.
     *
     * ### `useStrategy`
     * Combination of `STATIC`, `DYNAMIC`, `STREAM` and `READ`, `COPY` and `DRAW`
     * * **`STATIC`:** Buffer data will not change.
     * * **`DYNAMIC`:** Buffer data may change.
     * * **`STREAM`:** Buffer data may change frequently.
     * * **`READ`:** Buffer data are read by CPU (GPU -> CPU).
     * * **`COPY`:** Buffer data copied across GPU (GPU -> GPU).
     * * **`DRAW`:** Buffer data are written by GPU (CPU -> GPU).
     *
     * @param type type of buffer (e.g., [GL15C.GL_ARRAY_BUFFER])
     * @param useStrategy use strategy for buffer data (e.g., [GL15C.GL_STATIC_READ])
     * @param size size of buffer in bytes
     *
     * @since 1.0
     */
    constructor(type: Int, useStrategy: Int, size: Int) : this(type, useStrategy) {
        GL15C.glBindBuffer(type, bufferId)
        GL15C.glBufferData(type, size.toLong(), useStrategy)
        GL15C.glBindBuffer(type, 0)
    }

    /**
     * Updates and resize (if different) buffer data.
     * Significantly slower than [subdata], use sparingly.
     *
     * @param data new data to update buffer with
     * @param newUseStrategy new use strategy for buffer data. Can be null to use current use strategy.
     * @since 1.0
     */
    fun data(data: ByteBuffer, newUseStrategy: Int? = null) {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(1 to 5)

        newUseStrategy?.let {
            bind()
            GL15C.glBufferData(type, data, it)
            this.useStrategy = it
            unbind()
        }?: data(data, this.useStrategy)
    }
}