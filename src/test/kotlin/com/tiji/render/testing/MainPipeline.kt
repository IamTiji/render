package com.tiji.render.testing

import com.tiji.render.Window
import com.tiji.render.mesh.StaticMesh
import com.tiji.render.pipeline.AbstractPipeline
import com.tiji.render.pipeline.ShaderProgram
import com.tiji.render.vao.VertexStructureBuilder
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C.GL_FLOAT
import org.lwjgl.opengl.GL20C

class MainPipeline : AbstractPipeline() {
    override fun render() {
        beforeUniforms()

        GL20C.glUniform1f(program.getUniformLocation("iTime"), System.nanoTime()/1e9f)
        GL20C.glUniform2f(program.getUniformLocation("iResolution"), Window.width.toFloat(), Window.height.toFloat())
        val mousePos = Window.getCursorPosition().map { it.toFloat() }
        GL20C.glUniform4f(program.getUniformLocation("iMouse"), mousePos[0], mousePos[1],0f, 0f)

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