package com.tiji.render

import com.tiji.render.Logger.LogLevel
import com.tiji.render.pipeline.PipelineBatch
import com.tiji.render.texture.Resizable
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.ARBDebugOutput
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL43C
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.renderdoc.RenderDoc
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.stream.Collectors
import kotlin.math.sqrt

object Window: Resource() {
    private var window: Long = -1
    private val resizables: MutableList<Resizable> = mutableListOf()
    private var currentRenderBatch: PipelineBatch? = null

    lateinit var glVersion: Pair<Int, Int>
    lateinit var additionalSettings: List<Pair<Int, Int>>
    lateinit var requireExt: List<String>
    lateinit var logger: Logger
    private var _width = -1
    private var _height = -1
    private var _diagonal = -1.0
    private var fullscreen = false
    private var cursorMode = CursorMode.NORMAL

    lateinit var thread: Thread

    private val executeQueue = ConcurrentLinkedQueue<Runnable>()

    internal lateinit var clazz: Class<*>

    val width: Int get() = _width
    val height: Int get() = _height
    val diagonal: Double get() = _diagonal

    var debug = false

    /**
     * Initializes OpenGL context and creates a window.
     * After making this call, all calls to this library
     * must be made in the same thread as caller of this
     * function unless noted otherwise.
     *
     * `width` and `height` are ignored if `fullscreen` is true.
     *
     * @param title The default title of window
     * @param glVersion Minimum OpenGL version you want
     * @param width Default width of window
     * @param height Default height of window
     * @param fullscreen Whether if window should be fullscreen or not
     * @param debug Whether to create debug context or not
     *
     * @since 1.0
     */
    fun init(
        title: String,
        logger: Logger,
        glVersion: Pair<Int, Int>,
        additionalSettings: List<Pair<Int, Int>> = emptyList(),
        additionalInitSettings: List<Pair<Int, Int>> = emptyList(),
        requireExt: List<String> = emptyList(),
        width: Int = 800,
        height: Int = 600,
        fullscreen: Boolean = false,
        debug: Boolean = false,
        clazz: Class<*> = Window::class.java
    ) {
        this.clazz = clazz
        this.debug = debug

        Window.glVersion = glVersion
        Window.additionalSettings = additionalSettings
        Window.requireExt = requireExt
        _width = width
        _height = height
        _diagonal = sqrt((width*width)+(height*height).toDouble())
        Window.fullscreen = fullscreen
        Window.logger = logger
        thread = Thread.currentThread()

        additionalInitSettings.forEach { (key, value) ->
            glfwInitHint(key, value)
        }

        glfwSetErrorCallback { error, description ->
            logger.error("GLFW error: $error - ${GLFWErrorCallback.getDescription(description)}")
        }

        // if (debug && System.getProperty("os.name").lowercase().contains("nux")) {
        //     glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11)
        //     // RenderDoc is not available in Wayland
        // }

        if (!glfwInit()) throw RuntimeException("GLFW initialization failed")

        logger.log("Using ${glfwGetPlatform()} platform for GLFW.")

        requireExt.forEach {
            if (!glfwExtensionSupported(it)) throw RuntimeException("Extension $it is not supported")
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, glVersion.first)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, glVersion.second)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

        if (debug) {
            glfwWindowHint(GLFW_CONTEXT_ROBUSTNESS, GLFW_LOSE_CONTEXT_ON_RESET)
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)

            try {
                RenderDoc.create()
            } catch (_: IllegalStateException) {
                logger.warn("RenderDoc is not detected even though debug flag is set!")
            }
        }

        additionalSettings.forEach { (key, value) ->
            glfwWindowHint(key, value)
        }

        var realWidth = width
        var realHeight = height

        window = if (fullscreen) {
            val monitor = glfwGetPrimaryMonitor()
            val mode = glfwGetVideoMode(monitor)!!

            realWidth = mode.width()
            realHeight = mode.height()

            glfwCreateWindow(realWidth, realHeight, title, glfwGetPrimaryMonitor(), 0L)
        } else {
            glfwCreateWindow(width, height, title, 0L, 0L)
        }
        if (window == 0L) throw RuntimeException("Failed to create window")

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        GL.createCapabilities()

        glViewport(0, 0, realWidth, realHeight)

        fun logCallback() = { _:Int, type:Int, id:Int, severity:Int, length:Int, message:Long, _:Long ->
            var lineNumberText: String
            try {
                val stackWalker = StackWalker.getInstance(setOf(
                    StackWalker.Option.RETAIN_CLASS_REFERENCE
                ))
                val frames: MutableList<StackWalker.StackFrame> =
                    stackWalker.walk { stream -> stream.collect(Collectors.toList()) }
                val problematicFrame = frames.find { frame ->
                    frame.fileName != "Window.kt" &&
                            frame.declaringClass.packageName != "org.lwjgl.opengl"
                }!! // (Hopefully) this error won't be from this package

                lineNumberText =
                    "${problematicFrame.fileName}:" +
                            "${if (problematicFrame.lineNumber < 0) "??" else problematicFrame.lineNumber}"
            } catch (e: Exception) {
                lineNumberText = "??? ($e)"
            }


            val readMessage = MemoryUtil.memUTF8(message, length)
            logger.log(
                when (severity) {
                    GL43C.GL_DEBUG_SEVERITY_NOTIFICATION -> LogLevel.DEBUG
                    GL43C.GL_DEBUG_SEVERITY_LOW          -> LogLevel.INFO
                    GL43C.GL_DEBUG_SEVERITY_MEDIUM       -> LogLevel.WARNING
                    GL43C.GL_DEBUG_SEVERITY_HIGH         -> LogLevel.ERROR
                    else -> throw IllegalStateException("Unknown severity $severity")
                },
                "GL Debug Message (Type: $type, ID: $id, Message: $readMessage) " +
                        "@ $lineNumberText")
        }

        if ((glVersion.first >= 4) and (glVersion.second >= 3)) {
            glEnable(GL43C.GL_DEBUG_OUTPUT)
            glEnable(GL43C.GL_DEBUG_OUTPUT_SYNCHRONOUS)
            GL43C.glDebugMessageCallback (logCallback(), Constants.DEBUG_CALLBACK_PARAM)
        } else if (glfwExtensionSupported("GL_ARB_debug_output")) {
            glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB)
            ARBDebugOutput.glDebugMessageCallbackARB(logCallback(), Constants.DEBUG_CALLBACK_PARAM)
        } else {
            logger.warn("GL Debug Output is not supported; OpenGL logs won't be logged")
        }

        glfwSetFramebufferSizeCallback(window) { w, fbW, fbH ->
            glViewport(0, 0, fbW, fbH)
            _width = fbW
            _height = fbH
            _diagonal = sqrt((fbW*fbW)+(fbH*fbH).toDouble())
            resizables.forEach { it.resize(this.width, this.height) }
        }
    }

    /**
     * Sets the title of the window.
     *
     * @param title The new title of window
     * @since 1.0
     */
    fun setTitle(title: String) {
        Utils.ensureThread()

        glfwSetWindowTitle(window, title)
    }

    /**
     * Destroys all resources associated with the window and terminates GLFW.
     * It is automatically called upon window close.
     *
     * @since 1.0
     */
    override fun destroy() {
        Utils.ensureThread()

        super.destroy()

        glfwDestroyWindow(window)
        glfwTerminate()
        window = -1
    }

    /**
     * Register this resource as dependent on window size.
     * Any resource (that implements [Resizable]) which is
     * registered here will automatically be requested to
     * resize when window dimension changes.
     *
     * @param resource The resource to be resized upon window resize
     * @return The resource itself
     * @since 1.0
     */
    fun <T: Resizable>registerResizable(resource: T) : T {
        Utils.ensureThread()

        resizables.add(resource)

        return resource
    }

    /**
     * Schedule a runnable to be executed on the next frame,
     * after rendering and event polling is complete. Runnable
     * is guaranteed to be executed on the render thread.
     * Thread-safe, can be called from any thread.
     *
     * @param runnable The runnable to be executed
     * @since 1.0
     */
    fun schedule(runnable: Runnable) {
        executeQueue.add(runnable)
    }

    /**
     * Schedule a task to be executed on the next frame.
     * This function is identical to [schedule], except
     * that it blocks until operation is returned.
     *
     * @param runnable The runnable to be executed
     * @since 1.0
     */
    fun blockingSchedule(runnable: Runnable) {
        val latch = CountDownLatch(1)
        schedule {
            runnable.run()
            latch.countDown()
        }
        latch.await()
    }

    /**
     * Starts the rendering loop. This function will block until the window is closed.
     *
     * @since 1.0
     */
    fun start() {
        Utils.ensureThread()

        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()

            currentRenderBatch?.render()

            glfwSwapBuffers(window)

            executeQueue.forEach { it.run() }
            executeQueue.clear()
        }

        destroy()
        Resource.logUnfreedResources()
    }

    /**
     * Checks if certain key is pressed. Returns true if the key is pressed, false otherwise.
     *
     * @param key The key code
     * @since 1.0
     */
    fun isKeyPressed(key: Int): Boolean {
        Utils.ensureThread()

        return glfwGetKey(window, key) == GLFW_PRESS
    }

    /**
     * Checks if certain mouse button is pressed. Returns true if the button is pressed, false otherwise.
     *
     * @param button The button code
     * @since 1.0
     */
    fun isMouseButtonPressed(button: Int): Boolean {
        Utils.ensureThread()

        return glfwGetMouseButton(window, button) == GLFW_PRESS
    }

    /**
     * Gets the current cursor position in window coordinates.
     *
     * @return The current cursor position in window coordinates
     * @since 1.0
     */
    fun getCursorPosition(): DoubleArray {
        Utils.ensureThread()

        val width = DoubleArray(1)
        val height = DoubleArray(1)
        glfwGetCursorPos(window, width, height)

        return doubleArrayOf(width[0], height[0])
    }

    enum class CursorMode {
        NO_CURSOR,
        NORMAL
    }

    /**
     * Sets the cursor mode.
     *
     * @param mode The new cursor mode
     * @since 1.0
     */
    fun setCursorMode(mode: CursorMode) {
        Utils.ensureThread()

        cursorMode = mode
        glfwSetInputMode(window, GLFW_CURSOR, when (mode) {
            CursorMode.NO_CURSOR -> GLFW_CURSOR_DISABLED
            CursorMode.NORMAL -> GLFW_CURSOR_NORMAL
        })
    }

    /**
     * Sets render batch that will be used after current frame.
     *
     * @param batch The new render batch
     * @since 1.0
     */
    fun setRenderBatch(batch: PipelineBatch) {
        Utils.ensureThread()

        currentRenderBatch = batch
    }
}