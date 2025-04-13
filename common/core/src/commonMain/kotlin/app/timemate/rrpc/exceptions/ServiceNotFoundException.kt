package app.timemate.rrpc.exceptions

public class ServiceNotFoundException(
    serviceName: String,
) : RRpcException("Service '$serviceName' is not found.")