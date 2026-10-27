package com.tiji.render

import java.io.IOException

object Utils {
    fun expect(actual: Any?, expected: Any?, message: String?) {
        if (actual == expected) {
            throw AssertionError("Expected: $expected, but got: $actual. $message")
        }
    }

    fun expectNot(actual: Any?, unexpected: Any?, message: String?) {
        if (actual == unexpected) {
            throw IllegalArgumentException("Unexpected: $unexpected, but got: $actual. $message")
        }
    }

    fun readResource(path: String): ByteArray {
        try {
            Window.clazz.getResourceAsStream(path).use { inputStream ->
                requireNotNull(inputStream) { "Resource not found: $path" }
                return inputStream.readBytes()
            }
        } catch (e: IOException) {
            throw RuntimeException("Error reading resource: $path", e)
        }
    }

    fun readResourceAsString(path: String): String {
        return String(readResource(path))
    }

    fun ensureOpenGLVersion(minVersion: Pair<Int, Int>) {
        val realVersion = Window.glVersion

        if (realVersion.first < minVersion.first ||
            (realVersion.first == minVersion.first && realVersion.second < minVersion.second)) {
            throw RuntimeException("OpenGL version ${minVersion.first}.${minVersion.second} is required," +
                    " but found ${realVersion.first}.${realVersion.second}")
        }
    }

    fun ensureThread() {
        require(Thread.currentThread() === Window.thread) { "This method must be called from the rendering thread" }
    }
}
