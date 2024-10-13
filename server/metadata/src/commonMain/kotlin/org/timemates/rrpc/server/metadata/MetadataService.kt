package org.timemates.rrpc.server.metadata

import com.google.protobuf.ProtoEmpty
import kotlinx.serialization.builtins.ListSerializer
import org.timemates.rrpc.common.metadata.RMService
import org.timemates.rrpc.common.metadata.RMType
import org.timemates.rrpc.options.OptionsWithValue
import org.timemates.rrpc.server.module.RRpcService
import org.timemates.rrpc.server.module.descriptors.ProcedureDescriptor
import org.timemates.rrpc.server.module.descriptors.ServiceDescriptor

public class MetadataService(
    private val group: MetadataLookupGroup,
) : RRpcService {
    override val descriptor: ServiceDescriptor = ServiceDescriptor(
        name = "org.timemates.rrpc.server.metadata",
        procedures = listOf(
            ProcedureDescriptor.RequestResponse(
                name = "GetAvailableServices",
                inputSerializer = ProtoEmpty.serializer(),
                outputSerializer = ListSerializer(RMService.serializer()),
                procedure = { _, _ -> getAvailableServices() },
                options = OptionsWithValue.EMPTY,
            )
        ),
        options = OptionsWithValue.EMPTY,
    )

    public fun getAvailableServices(): List<RMService> {
        return group.resolveAllServices().toList()
    }

    public fun getAvailableTypes(): List<RMType> {
        return group.resolveAllTypes().toList()
    }
}