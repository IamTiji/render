package com.tiji.render.mesh

import com.tiji.render.Utils
import com.tiji.render.buffer.AbstractGLBuffer
import com.tiji.render.buffer.GLBuffer
import com.tiji.render.buffer.LegacyGLBuffer
import com.tiji.render.vao.GLVertexArray
import com.tiji.render.vao.VertexStructure
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL20C
import java.nio.ByteBuffer

/**
 * Automatically managed push-pop based mesh.
 *
 * @param maxObjects maximum number of objects (e.g, triangle, quad) in the mesh
 * @param perObjectVertices number of vertices per object (e.g., 3 for triangle, 4 for quad)
 * @param perObjectIndices number of indices per object (e.g., 3 for triangle, 6 for quad)
 * @param useBufferStorage whether if to use [GLBuffer] or [LegacyGLBuffer]. Set it to `true` if you use OpenGL 4.4+.
 * @param vertexStructure vertex structure for the mesh
 * @since 1.0
 */
class StackMesh(val maxObjects: Int,
                val perObjectVertices: Int,
                val perObjectIndices: Int,
                useBufferStorage: Boolean,
                val vertexStructure: VertexStructure) : Mesh() {
    private var stackPos = 0
    private val eboStack: AbstractGLBuffer
    private val vboStack: AbstractGLBuffer
    private val vao: GLVertexArray = GLVertexArray(vertexStructure)
    private val indicesStorageFormat: Int
    private val perIndicesBytes: Int

    private val pendingData: ByteBuffer
    private val pendingIndices: ByteBuffer
    private var pendingObjects = 0

    override fun draw() {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(2 to 0)

        vao.bind()
        flushPendingObjects()
        GL20C.glDrawElements(GL15C.GL_TRIANGLES, stackPos*perObjectIndices, indicesStorageFormat, 0)
        vao.unbind()
    }

    override fun destroy() {
        super.destroy()
    }

    init {
        val verticesCount = maxObjects * perObjectVertices
        indicesStorageFormat = when {
            verticesCount < 256 -> GL15C.GL_UNSIGNED_BYTE
            verticesCount < 65536 -> GL15C.GL_UNSIGNED_SHORT
            else -> GL15C.GL_UNSIGNED_INT
        }
        perIndicesBytes = when (indicesStorageFormat) {
            GL15C.GL_UNSIGNED_BYTE -> 1
            GL15C.GL_UNSIGNED_SHORT -> 2
            else -> 4
        }

        var size = maxObjects * perObjectIndices * perIndicesBytes
        eboStack = if (useBufferStorage) {
            GLBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, true, size)
        } else {
            LegacyGLBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, GL15C.GL_DYNAMIC_DRAW, size)
        }
        eboStack.bind()
        pendingIndices = BufferUtils.createByteBuffer(size)

        size = maxObjects * vertexStructure.stride * perObjectVertices
        vboStack = if (useBufferStorage) {
            GLBuffer(GL15C.GL_ARRAY_BUFFER, true, size)
        } else {
            LegacyGLBuffer(GL15C.GL_ARRAY_BUFFER, GL15C.GL_DYNAMIC_DRAW, size)
        }
        vboStack.bind()
        pendingData = BufferUtils.createByteBuffer(size)

        vao.attach(eboStack)
        vao.attach(vboStack)

        vao.dataReady()

        attach(vao)
    }

    /**
     * Push a new object to the stack.
     *
     * @param vertices vertex data for the object
     * @param indices index data for the object. It is relative to first argument, starting from 0.
     * @since 1.0
     */
    fun addObject(vertices: ByteArray, indices: IntArray) {
        require(vertices.size == perObjectVertices*vertexStructure.stride) { "Invalid vertices size" }
        require(indices.size == perObjectIndices) { "Invalid indices" }
        require(stackPos + pendingObjects < maxObjects) { "Buffer overflow" }

        pendingData.put(vertices)
        val indicesStart = (stackPos + pendingObjects) * perObjectVertices
        indices.forEach {
            when (indicesStorageFormat) {
                GL15C.GL_UNSIGNED_BYTE ->  pendingIndices.put     ((it + indicesStart).toByte())
                GL15C.GL_UNSIGNED_SHORT -> pendingIndices.putShort((it + indicesStart).toShort())
                else ->                    pendingIndices.putInt  ((it + indicesStart))
            }
        }
        pendingObjects++
    }

    /**
     * Push multiple objects to the stack
     *
     * @param vertices vertex data for the objects
     * @param indices index data for the objects. It is relative to first argument, starting from 0.
     * @param count number of objects in new objects data.
     */
    fun addObjects(vertices: ByteArray, indices: IntArray, count: Int) {
        require(vertices.size == perObjectVertices*vertexStructure.stride*count) { "Invalid vertices size" }
        require(indices.size == perObjectIndices*count) { "Invalid indices" }
        require(stackPos + pendingObjects + count < maxObjects) { "Buffer overflow" }

        pendingData.put(vertices)
        val indicesStart = (stackPos + pendingObjects) * perObjectVertices
        indices.forEach {
            when (indicesStorageFormat) {
                GL15C.GL_UNSIGNED_BYTE ->  pendingIndices.put     ((it + indicesStart).toByte())
                GL15C.GL_UNSIGNED_SHORT -> pendingIndices.putShort((it + indicesStart).toShort())
                else ->                    pendingIndices.putInt  ((it + indicesStart))
            }
        }
        pendingObjects += count
    }

    /**
     * Pops specified number of objects from the stack.
     *
     * @param count number of objects to pop from the stack
     * @since 1.0
     */
    fun popObjects(count: Int) {
        require((pendingObjects + stackPos) >= count) { "Buffer underflow" }

        if (pendingObjects > count) {
            pendingObjects -= count
            pendingData.position(pendingData.position() - (count * perObjectVertices * vertexStructure.stride))
            pendingIndices.position(pendingIndices.position() - (count * perObjectIndices * perIndicesBytes))
        } else if (pendingObjects > 0) {
            val offset = count - pendingObjects
            stackPos -= offset
            pendingObjects = 0
            pendingData.position(offset * perObjectVertices * vertexStructure.stride)
            pendingIndices.position(offset * perObjectIndices * perIndicesBytes)
        } else {
            stackPos -= count
        }
    }

    /**
     * Pops one object from the stack.
     *
     * @since 1.0
     */
    fun popObject() {
        popObjects(1)
    }

    private fun flushPendingObjects() {
        if (pendingObjects == 0) return

        val pendingIndicesBytes = pendingObjects * perObjectIndices * perIndicesBytes
        val limitedPendingIndices = pendingIndices.slice(0, pendingIndicesBytes)
        eboStack.subdata(stackPos * perIndicesBytes * perObjectIndices, limitedPendingIndices)

        val pendingVertexBytes = pendingObjects * perObjectVertices * vertexStructure.stride
        val limitedPendingData = pendingData.slice(0, pendingVertexBytes)
        vboStack.subdata(stackPos * perObjectVertices * vertexStructure.stride, limitedPendingData)

        stackPos += pendingObjects
        pendingObjects = 0
        pendingData.clear()
        pendingIndices.clear()
    }
}