package com.tiji.render.texture

import org.lwjgl.opengl.GL11C

class GLDepthTexture: AbstractGLTexture {
    /**
     * Creates texture using raw bytes
     *
     * @param width The width of the texture
     * @param height The height of the texture
     *
     * @since 1.0
     */
    constructor(width: Int, height: Int) : super(
        width, height, null, true, true,
        GL11C.GL_DEPTH_COMPONENT, GL11C.GL_FLOAT, GL11C.GL_DEPTH_COMPONENT
    )
}