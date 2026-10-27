package com.tiji.render.vao

import com.tiji.render.Resource
import com.tiji.render.Utils
import org.lwjgl.opengl.GL30C.*

/**
 * Wrapper for VAOs
 *
 * **Requires:** OpenGL 3.0+
 *
 * @param structure
 * @since 1.0
 */
class GLVertexArray(private val structure: VertexStructure) : Resource() {
    private val bufferId: Int = glGenVertexArrays()

    init {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        bind()
    }

    /**
     * Run after vertices are loaded.
     *
     * **Requires:** OpenGL 3.0+
     *
     * @since 1.0
     */
    fun dataReady() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        var pointer = 0
        var index = 0

        for ((size, type, normalize) in structure.fields) {
            glVertexAttribPointer(
                index++,
                size,
                type,
                normalize,
                structure.stride,
                pointer.toLong()
            )
            glEnableVertexAttribArray(index - 1)
            pointer += size * VertexStructureBuilder.TYPE_SIZE[type]!!
        }

        unbind()
    }

    /**
     * Bind the VAO
     *
     * **Requires:** OpenGL 3.0+
     *
     * @since 1.0
     */
    fun bind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        glBindVertexArray(bufferId)
    }

    /**
     * Unbind the VAO
     *
     * **Requires:** OpenGL 3.0+
     *
     * @since 1.0
    */
    fun unbind() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        glBindVertexArray(0)
    }

    /**
     * Destroys the VAO
     *
     * **Requires:** OpenGL 3.0+
     *
     * @since 1.0
    */
    override fun destroy() {
        super.destroy()

        Utils.ensureThread()
        Utils.ensureOpenGLVersion(3 to 0)

        glDeleteBuffers(bufferId)
    }
}
