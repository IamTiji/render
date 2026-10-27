package com.tiji.render

import com.tiji.render.Logger.LogLevel

/**
 * Default implementation of Logger interface.
 * If you have different logger, don't use this class
 *
 * @since 1.0
 *
 * @see com.tiji.render.Logger
 */
object DefaultLogger : Logger {
    override fun log(message: String) {
        println(formatLogLine(message, LogLevel.INFO))
    }

    override fun warn(message: String) {
        println(formatLogLine(message, LogLevel.WARNING))
    }

    override fun error(message: String) {
        println(formatLogLine(message, LogLevel.ERROR))
    }

    override fun debug(message: String) {
        println(formatLogLine(message, LogLevel.DEBUG))
    }
}