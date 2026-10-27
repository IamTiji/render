package com.tiji.render.pipeline

import org.lwjgl.opengl.GL11C.*

enum class BlendMethod(val f1: Int, val f2: Int, val f3: Int, val f4: Int) {
    DISABLED(-1, -1, -1, -1),
    STANDARD(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA),
    PREMULTIPLIED(GL_ONE, GL_ONE_MINUS_SRC_ALPHA),
    KEEP_ALPHA(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA),
    ADDITIVE(GL_ONE, GL_ONE);

    constructor(f1: Int, f2: Int): this(f1, f2, f1, f2)
}