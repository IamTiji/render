package cc.tijiisreal.render.mesh

import cc.tijiisreal.render.Resource
import cc.tijiisreal.render.vao.GLVertexArray

abstract class Mesh : Resource() {
    abstract val vao: GLVertexArray

    /**
     * Draws the mesh
     */
    abstract fun draw()
}