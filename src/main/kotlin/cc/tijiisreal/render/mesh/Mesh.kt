package cc.tijiisreal.render.mesh

import cc.tijiisreal.render.Resource

abstract class Mesh : Resource() {
    /**
     * Draws the mesh
     */
    abstract fun draw()
}