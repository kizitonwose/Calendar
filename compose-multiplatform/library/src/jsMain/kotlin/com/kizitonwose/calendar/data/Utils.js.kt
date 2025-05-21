package com.kizitonwose.calendar.data

internal actual fun log(tag: String, message: String) =
    console.log("$tag : $message")
