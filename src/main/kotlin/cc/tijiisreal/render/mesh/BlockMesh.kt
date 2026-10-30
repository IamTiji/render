package cc.tijiisreal.render.mesh

import cc.tijiisreal.render.Utils
import cc.tijiisreal.render.buffer.AbstractGLBuffer
import cc.tijiisreal.render.buffer.GLBuffer
import cc.tijiisreal.render.buffer.LegacyGLBuffer
import cc.tijiisreal.render.vao.GLVertexArray
import cc.tijiisreal.render.vao.VertexStructure
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL20C
import java.nio.ByteBuffer

/**
 * Mesh only providing method for replacing all data at once.
 *
 * @param useBufferStorage whether if to use [GLBuffer] or [LegacyGLBuffer]. Set it to `true` if you use OpenGL 4.4+.
 * @param vertexStructure vertex structure for the mesh
 * @since 1.0
 */
class BlockMesh(val maxIndicesCount: Int,
                val maxVertexSize: Int,
                useBufferStorage: Boolean,
                val vertexStructure: VertexStructure) : Mesh() {
    private var amount = 0

    private val ebo: AbstractGLBuffer
    private val vbo: AbstractGLBuffer
    private val vao: GLVertexArray = GLVertexArray(vertexStructure)

    private val indicesStorageFormat: Int
    private val perIndicesBytes: Int

    override fun draw() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(2 to 0)

        vao.bind()
        GL20C.glDrawElements(GL15C.GL_TRIANGLES, amount, indicesStorageFormat, 0)
        vao.unbind()
    }

    init {
        indicesStorageFormat = when {
            maxIndicesCount < 256 -> GL15C.GL_UNSIGNED_BYTE
            maxIndicesCount < 65536 -> GL15C.GL_UNSIGNED_SHORT
            else -> GL15C.GL_UNSIGNED_INT
        }
        perIndicesBytes = when (indicesStorageFormat) {
            GL15C.GL_UNSIGNED_BYTE -> 1
            GL15C.GL_UNSIGNED_SHORT -> 2
            else -> 4
        }

        ebo = if (useBufferStorage) {
            GLBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, true, maxIndicesCount*perIndicesBytes)
        } else {
            LegacyGLBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, GL15C.GL_DYNAMIC_DRAW, maxIndicesCount*perIndicesBytes)
        }
        ebo.bind()

        vbo = if (useBufferStorage) {
            GLBuffer(GL15C.GL_ARRAY_BUFFER, true, maxVertexSize)
        } else {
            LegacyGLBuffer(GL15C.GL_ARRAY_BUFFER, GL15C.GL_DYNAMIC_DRAW, maxVertexSize)
        }
        vbo.bind()

        vao.attach(ebo)
        vao.attach(vbo)

        vao.dataReady()

        attach(vao)
    }

    fun replace(vertexData: ByteBuffer,
                indicesData: IntArray) {
        val newEboData = BufferUtils.createByteBuffer(indicesData.size * perIndicesBytes)
        indicesData.forEach {
            when (indicesStorageFormat) {
                GL15C.GL_UNSIGNED_BYTE -> newEboData.put(it.toByte())
                GL15C.GL_UNSIGNED_SHORT -> newEboData.putShort(it.toShort())
                else -> newEboData.putInt(it)
            }
        }
        newEboData.flip()
        ebo.subdata(0, newEboData)
        amount = indicesData.size

        vbo.subdata(0, vertexData)
    }
}