package com.tiji.render.pipeline

import org.lwjgl.opengl.GL11C.*

enum class DepthTest(val glValue: Int) {
    DISABLED(-1),

    ALWAYS(GL_ALWAYS),
    LESS(GL_LESS),
    LESS_OR_EQUAL(GL_LEQUAL),
    GREATER(GL_GREATER),
    GREATER_OR_EQUAL(GL_GEQUAL),
}