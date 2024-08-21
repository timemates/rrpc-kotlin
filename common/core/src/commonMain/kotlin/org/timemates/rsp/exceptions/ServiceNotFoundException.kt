package org.timemates.rsp.exceptions

public class ServiceNotFoundException(
    serviceName: String,
) : RSPException("Service '$serviceName' is not found.")