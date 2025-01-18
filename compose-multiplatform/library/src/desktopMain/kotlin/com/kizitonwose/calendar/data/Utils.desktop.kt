package com.kizitonwose.calendar.data

import java.util.logging.Level
import java.util.logging.Logger

internal actual fun log(tag: String, message: String) =
    logger.warning("$tag : $message")

private val logger = Logger.getLogger("Calendar").apply {
    level = Level.WARNING
}
