package com.kizitonwose.calendar.log

import platform.Foundation.NSLog

internal actual fun log(tag: String, message: String) {
    NSLog("$tag : $message")
}
