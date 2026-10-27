package com.tiji.render.pipeline

import com.tiji.render.Resource
import com.tiji.render.Window
import com.tiji.render.mesh.Mesh
import com.tiji.render.texture.AbstractGLFrameBuffer
import com.tiji.render.texture.DefaultGLFrameBuffer
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL21C

/**
 * An abstract pipeline for rendering a mesh with a shader program.
 * Do note that this doesn't clean up resources, as this assumes that
 * resources are reused, therefore cleaning up might break other pipelines.
 *
 * @since 1.0
 */
abstract class AbstractPipeline: Resource() {
    private val uniforms: MutableList<String> = mutableListOf()
    lateinit var program: ShaderProgram
    lateinit var mesh: Mesh
    var fbo: AbstractGLFrameBuffer = DefaultGLFrameBuffer

    /**
     * Request a uniform for this pipeline.
     *
     * @param name The name of the uniform.
     * @since 1.0
     */
    protected fun requestUniform(name: String) {
        if (!::program.isInitialized) throw IllegalStateException("Program must be set before requesting uniforms")
        if (program.getUniformLocation(name) == -1) {
            Window.logger.warn("Uniform '$name' wasn't used. Consider removing this request")
        } else {
            uniforms.add(name)
        }
    }

    /**
     * Render the mesh using the shader program.
     * If [callRender] isn't called, no rendering
     * will be done for this pipeline.
     *
     * @since 1.0
     */
    abstract fun render()

    /**
     * Call before setting uniforms, when manually setting
     * uniforms. Don't use it with [callRender]
     *
     * @since 1.0
     */
    protected fun beforeUniforms() {
        program.use()
    }

    /**
     * Call after setting uniforms, when manually setting
     * uniforms. Don't use it with [callRender]
     *
     * @since 1.0
     */
    protected fun afterUniforms() {
        fbo.bind()
        mesh.draw()
    }

    /**
     * Call the render method of the shader program.
     * This is intended to be called by [render] function.
     * This function does not set any uniforms.
     *
     * @since 1.0
     */
    protected fun callRender() {
        program.use()
        fbo.bind()
        mesh.draw()
    }

    /**
     * Sets the depth function.
     *
     * @since 1.0
     */
    protected fun setDepthFunction(newFunction: DepthTest) {
        if (newFunction == DepthTest.DISABLED) {
            GL11C.glDisable(GL11C.GL_DEPTH_TEST)
        } else {
            GL11C.glEnable(GL11C.GL_DEPTH_TEST)
            GL11C.glDepthFunc(newFunction.glValue)
        }
    }

    /**
     * Sets the alpha function
     *
     * **Requires:** OpenGL 2.1+
     * @since 1.0
     */
    protected fun setAlphaFunction(newFunction: BlendMethod) {
        if (newFunction == BlendMethod.DISABLED) {
            GL11C.glDisable(GL11C.GL_BLEND)
        } else {
            GL11C.glEnable(GL11C.GL_BLEND)
            GL21C.glBlendFuncSeparate(newFunction.f1, newFunction.f2,
                newFunction.f3, newFunction.f4)
        }
    }
}