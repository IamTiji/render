package com.tiji.render

import java.text.SimpleDateFormat
import java.util.Date
import java.time.Instant

interface Logger {
    enum class LogLevel {
        INFO, WARNING, ERROR, DEBUG
    }

    fun log(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun debug(message: String)

    fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.INFO -> log(message)
            LogLevel.WARNING -> warn(message)
            LogLevel.ERROR -> error(message)
            LogLevel.DEBUG -> debug(message)
        }
    }

    /**
     * Formats a string using the provided format and arguments
     * Same as [String.format] but can be overridden to provide custom formatting
     *
     * @param format the format string
     * @param args the arguments to be formatted into the format string
     * @return the formatted string
     * @since 1.0
     */
    fun format(format: String, vararg args: Any): String {
        return String.format(format, *args)
    }

    /**
     * Logs a message with a timestamp and level
     *
     * @param message the message to log
     * @param level the log level of the message
     * @return the formatted log line
     * @since 1.0
     */
    fun formatLogLine(message: String, level: LogLevel): String {
        return "${SimpleDateFormat("HH:mm:ss").format(Date.from(Instant.now()))} [${level.name}] $message"
    }
}