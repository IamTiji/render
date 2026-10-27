package com.tiji.render.pipeline

import com.tiji.render.Resource
import com.tiji.render.Utils
import com.tiji.render.Window
import glm_.mat2x2.Mat2
import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import org.lwjgl.opengl.GL20C

/**
 * Represents a shader program for rendering.
 *
 * **Requires:** OpenGL 2.0+
 *
 * @param vertexShaderPath The path to the vertex shader source file.
 * @param fragmentShaderPath The path to the fragment shader source file.
 * @since 1.0
 */
class ShaderProgram(vertexShaderPath: String, fragmentShaderPath: String) : Resource() {
    private val programID: Int = GL20C.glCreateProgram()
    private val cachedUniformLocation: MutableMap<String, Int> = mutableMapOf()

    init {
        Utils.ensureThread()
        Utils.ensureOpenGLVersion(2 to 0)

        Utils.expectNot(programID, 0, "Failed to create shader program")

        val vertexShaderSource = Utils.readResourceAsString(vertexShaderPath)
        val fragmentShaderSource = Utils.readResourceAsString(fragmentShaderPath)

        val vertexShaderID = compileShader(GL20C.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShaderID = compileShader(GL20C.GL_FRAGMENT_SHADER, fragmentShaderSource)

        GL20C.glAttachShader(programID, vertexShaderID)
        GL20C.glAttachShader(programID, fragmentShaderID)

        GL20C.glLinkProgram(programID)
        Utils.expectNot(
            GL20C.glGetProgrami(programID, GL20C.GL_LINK_STATUS),
            0,
            "Failed to link shader program"
        )

        GL20C.glValidateProgram(programID)
        Utils.expectNot(
            GL20C.glGetProgrami(programID, GL20C.GL_VALIDATE_STATUS),
            0,
            "Failed to validate shader program"
        )

        GL20C.glDeleteShader(vertexShaderID)
        GL20C.glDeleteShader(fragmentShaderID)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shaderID: Int = GL20C.glCreateShader(type)
        Utils.expectNot(shaderID, 0, "Failed to create shader")

        GL20C.glShaderSource(shaderID, source)
        GL20C.glCompileShader(shaderID)

        val compileStatus = IntArray(1)
        GL20C.glGetShaderiv(shaderID, GL20C.GL_COMPILE_STATUS, compileStatus)
        if (compileStatus[0] == 0) {
            Window.logger.error("Shader compilation failed: " + GL20C.glGetShaderInfoLog(shaderID))
            throw RuntimeException("Shader compilation failed")
        }

        return shaderID
    }

    /**
     * Binds this shader program for rendering.
     *
     * @since 1.0
     */
    fun use() {
        GL20C.glUseProgram(programID)
    }

    override fun destroy() {
        super.destroy()

        GL20C.glDeleteProgram(programID)
    }

    /**
     * Returns the location of the uniform variable with the given name.
     * Returns -1 if the uniform variable does not exist.
     *
     * @since 1.0
     */
    fun getUniformLocation(name: String): Int {
        if (cachedUniformLocation.containsKey(name)) {
            return cachedUniformLocation[name]!!
        }

        val location: Int = GL20C.glGetUniformLocation(programID, name)
        cachedUniformLocation[name] = location
        return location
    }


    // Annoyingly many uniform functions

    // -- Single value uniforms --
    operator fun set(name: String, value: Int)   = GL20C.glUniform1i(getUniformLocation(name), value)
    operator fun set(name: String, value: Float) = GL20C.glUniform1f(getUniformLocation(name), value)

    // -- Vector uniforms --
    operator fun set(name: String, value: Vec2) = GL20C.glUniform2f(getUniformLocation(name), value.x, value.y)
    operator fun set(name: String, value: Vec3) = GL20C.glUniform3f(getUniformLocation(name), value.x, value.y, value.z)
    operator fun set(name: String, value: Vec4) = GL20C.glUniform4f(getUniformLocation(name), value.x, value.y, value.z, value.w)

    operator fun set(name: String, value: Vec2i) = GL20C.glUniform2i(getUniformLocation(name), value.x, value.y)
    operator fun set(name: String, value: Vec3i) = GL20C.glUniform3i(getUniformLocation(name), value.x, value.y, value.z)
    operator fun set(name: String, value: Vec4i) = GL20C.glUniform4i(getUniformLocation(name), value.x, value.y, value.z, value.w)

    // -- Matrix uniforms --
    operator fun set(name: String, value: Mat4) = GL20C.glUniformMatrix4fv(getUniformLocation(name), false, value.toFloatArray())
    operator fun set(name: String, value: Mat3) = GL20C.glUniformMatrix3fv(getUniformLocation(name), false, value.toFloatArray())
    operator fun set(name: String, value: Mat2) = GL20C.glUniformMatrix2fv(getUniformLocation(name), false, value.toFloatArray())
}