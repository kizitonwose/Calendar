package com.kizitonwose.calendar.data

import android.util.Log

internal actual fun log(tag: String, message: String) = Log.w(tag, message).asUnit()
