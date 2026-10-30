package cc.tijiisreal.render.testing

import cc.tijiisreal.render.Window
import cc.tijiisreal.render.mesh.StaticMesh
import cc.tijiisreal.render.pipeline.AbstractPipeline
import cc.tijiisreal.render.pipeline.ShaderProgram
import cc.tijiisreal.render.vao.VertexStructureBuilder
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C.GL_FLOAT

class MainPipeline : AbstractPipeline() {
    override fun render() {
        beforeUniforms()

        program["iTime"] = System.nanoTime()/1e9f
        program["iResolution"] = Vec2(Window.width.toFloat(), Window.height.toFloat())
        val mousePos = Window.getCursorPosition().map { it.toFloat() }
        program["iMouse"] = Vec4(mousePos[0], mousePos[1], 0f, 0f)

        afterUniforms()
    }

    init {
        program = Window.attach(ShaderProgram("/main.vert", "/main.frag"))
        requestUniform("iTime")
        requestUniform("iResolution")
        requestUniform("iMouse")

        val structure = VertexStructureBuilder()
            .add("position", 3, GL_FLOAT, false)
            .add("uv"      , 2, GL_FLOAT, false)
            .build()

        val vertexData = BufferUtils.createByteBuffer(structure.stride*4)
        vertexData.asFloatBuffer().put(
            floatArrayOf(
                 1f,  1f, 0f,         1f,  1f,   // Vertex 1
                -1f,  1f, 0f,        -1f,  1f,   // Vertex 2
                -1f, -1f, 0f,        -1f, -1f,   // Vertex 3
                 1f, -1f, 0f,         1f, -1f    // Vertex 4
            ))

        mesh = StaticMesh(intArrayOf(0, 1, 2, 0, 2, 3), vertexData, true, structure)
        attach(mesh)
    }
}