package com.tiji.render.vao

/**
 * A data class representing a vertex structure's field.
 *
 * @since 1.0
 * @property size The size of the field in bytes.
 * @property type Type of the field.
 * @property normalize A flag indicating whether the field should be normalized to float in range [[-1, 1]].
 */
data class VertexStructureField(val size: Int, val type: Int, val normalize: Boolean)