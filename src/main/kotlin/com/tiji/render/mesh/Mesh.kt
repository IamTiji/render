package com.tiji.render.mesh

import com.tiji.render.Resource

abstract class Mesh : Resource() {
    /**
     * Draws the mesh
     */
    abstract fun draw()
}