package app.timemate.rrpc.annotations

@RequiresOptIn(message = "This API is considered as internal.", level = RequiresOptIn.Level.ERROR)
public annotation class InternalRRpcAPI