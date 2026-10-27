package com.tiji.render.pipeline

import org.lwjgl.opengl.GL33C
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class TimerPipeline {
    private val query = GL33C.glGenQueries()
    private var _lastMeasurement = 0L

    val startPipeline: AbstractPipeline get() {
        return object : AbstractPipeline() {
            override fun render() {
                GL33C.glBeginQuery(GL33C.GL_TIME_ELAPSED, query)
            }
        }
    }

    val endPipeline: AbstractPipeline get() {
        return object : AbstractPipeline() {
            override fun render() {
                GL33C.glEndQuery(GL33C.GL_TIME_ELAPSED)
                _lastMeasurement = GL33C.glGetQueryObjectui64(query, GL33C.GL_QUERY_RESULT)
            }
        }
    }

    val lastMeasurement: Duration get() {
        return _lastMeasurement.nanoseconds
    }
}