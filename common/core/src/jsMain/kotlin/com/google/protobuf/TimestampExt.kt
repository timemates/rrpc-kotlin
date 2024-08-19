package com.google.protobuf

import kotlin.js.Date

public fun Timestamp.toJsDate(): Date = Date(
    seconds * 1000,
)