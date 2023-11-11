package com.y9vad9.rsocket.proto.metadata

import kotlinx.serialization.Serializable

/**
 * Represents the metadata for the incoming requests.
 *
 * @property service The name of the service.
 * @property method The name of the method.
 */
@Serializable
public data class Metadata(
    val service: String = "",
    val method: String = "",
    val custom: Map<String, String>,
)

