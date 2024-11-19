package org.timemates.rrpc.codegen.plugin

/**
 * Exception thrown when communication with the generator fails or produces an invalid response.
 */
public class CommunicationException(message: String) : Exception(message)