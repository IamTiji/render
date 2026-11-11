package cc.tijiisreal.render.mesh

import cc.tijiisreal.render.buffer.AbstractGLBuffer
import cc.tijiisreal.render.buffer.GLBuffer
import cc.tijiisreal.render.buffer.LegacyGLBuffer
import cc.tijiisreal.render.vao.GLVertexArray
import cc.tijiisreal.render.vao.VertexStructure
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL20C
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Immutable mesh
 *
 * @param indices indices for the mesh
 * @param vertices vertices data for the mesh
 * @param useBufferStorage whether if to use [GLBuffer] or [LegacyGLBuffer]. Set it to `true` if you use OpenGL 4.4+.
 * @param vertexStructure vertex structure for the mesh
 * @since 1.0
 */
class StaticMesh(
    indices: IntArray,
    vertices: ByteBuffer,
    useBufferStorage: Boolean,
    vertexStructure: VertexStructure
): Mesh() {
    override val vao: GLVertexArray
        get() = _vao

    private val _vao = GLVertexArray(vertexStructure)
    private val ebo: AbstractGLBuffer
    private val vbo: AbstractGLBuffer
    private val indicesStorageFormat: Int
    private val indicesCount: Int = indices.size

    init {
        val verticesCount = vertices.capacity() / vertexStructure.stride
        indicesStorageFormat = when {
            verticesCount < 256 -> GL15C.GL_UNSIGNED_BYTE
            verticesCount < 65536 -> GL15C.GL_UNSIGNED_SHORT
            else -> GL15C.GL_UNSIGNED_INT
        }
        val perIndicesBytes = when (indicesStorageFormat) {
            GL15C.GL_UNSIGNED_BYTE -> 1
            GL15C.GL_UNSIGNED_SHORT -> 2
            else -> 4
        }

        val size = indices.size * perIndicesBytes

        val eboBuffer = BufferUtils.createByteBuffer(size).order(ByteOrder.nativeOrder())
        indices.forEach {
            when (indicesStorageFormat) {
                GL15C.GL_UNSIGNED_BYTE -> {
                    eboBuffer.put(it.toByte())
                }
                GL15C.GL_UNSIGNED_SHORT -> {
                    eboBuffer.putShort(it.toShort())
                }
                else -> {
                    eboBuffer.putInt(it)
                }
            }
        }
        eboBuffer.flip()

        ebo = if (useBufferStorage) {
            GLBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, false, eboBuffer)
        } else {
            LegacyGLBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, GL15C.GL_STATIC_DRAW, eboBuffer)
        }
        ebo.bind()

        vbo = if (useBufferStorage) {
            GLBuffer(GL15C.GL_ARRAY_BUFFER, false, vertices)
        } else {
            LegacyGLBuffer(GL15C.GL_ARRAY_BUFFER, GL15C.GL_STATIC_DRAW, vertices)
        }
        vbo.bind()

        _vao.attach(ebo)
        _vao.attach(vbo)

        _vao.dataReady()

        attach(_vao)
    }

    override fun draw() {
        _vao.bind()
        GL20C.glDrawElements(GL15C.GL_TRIANGLES, indicesCount, indicesStorageFormat, 0)
    }

    override fun destroy() {
        super.destroy()
    }
}