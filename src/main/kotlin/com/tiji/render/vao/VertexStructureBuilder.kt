package com.tiji.render.vao

import org.lwjgl.opengl.GL11C.*

/**
 * Builds a vertex structure for a VAO.
 *
 * @since 1.0
 */
class VertexStructureBuilder {
    companion object {
        val TYPE_SIZE: Map<Int, Int> = mapOf(
            GL_FLOAT          to 4,
            GL_INT            to 4,
            GL_UNSIGNED_INT   to 4,
            GL_SHORT          to 2,
            GL_UNSIGNED_SHORT to 2,
            GL_BYTE           to 1,
            GL_UNSIGNED_BYTE  to 1
        )
    }

    private val fields: ArrayList<VertexStructureField> = ArrayList()
    private var stride = 0

    /**
     * Adds a field to the vertex structure.
     *
     * @param count The number of elements in the field, i.e., `vec3`: 3. Can only be within [[1, 4]]
     * @param type The data type of the field, e.g., `GL_FLOAT`
     * @param normalize If true, normalize the field's values to the range [[-1, 1]]
     *
     * @since 1.0
     */
    fun add(name: String, count: Int, type: Int, normalize: Boolean): VertexStructureBuilder {
        require(count in 1..4) { "'$name' takes invalid number of elements." }
        fields.add(VertexStructureField(count, type, normalize))
        stride += count * TYPE_SIZE[type]!!
        return this
    }

    /**
     * Builds the vertex structure.
     *
     * @return A new instance of `VertexStructure`
    */
    fun build(): VertexStructure {
        return VertexStructure(stride, fields)
    }
}