package com.google.protobuf

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

public fun Timestamp.toJavaInstant(): Instant =
    Instant.ofEpochSecond(seconds, nanos.toLong())

public fun Timestamp.toJavaLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(toJavaInstant(), ZoneOffset.UTC)

public fun Timestamp.toJavaLocalDate(): LocalDate =
    LocalDate.ofInstant(toJavaInstant(), ZoneOffset.UTC)