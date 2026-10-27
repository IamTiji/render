package com.tiji.render.vao

/**
 * A data class representing a vertex structure.
 *
 * @since 1.0
 * @property stride The size of the vertex in bytes.
 * @property fields A list of fields that make up the vertex structure.
 */
data class VertexStructure(val stride: Int, val fields: MutableList<VertexStructureField>)