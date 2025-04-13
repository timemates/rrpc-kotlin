package app.timemate.rrpc

import app.timemate.rrpc.instances.InstanceContainer
import app.timemate.rrpc.instances.ProvidableInstance

public enum class RequestType : ProvidableInstance {
    REQUEST_RESPONSE,
    REQUEST_STREAM,
    REQUEST_CHANNEL,
    FIRE_AND_FORGET,
    METADATA_PUSH;

    override val key: ProvidableInstance.Key<*>
        get() = Key

    public companion object Key : ProvidableInstance.Key<RequestType>
}

public val InstanceContainer.underlyingRequestType: RequestType
    inline get() = getInstance(RequestType.Key) ?: error("Unable to get underlying request type. It might happen, because you run not in correct context or if InstanceContainer was modified incorrectly, removing the request type in chain.")