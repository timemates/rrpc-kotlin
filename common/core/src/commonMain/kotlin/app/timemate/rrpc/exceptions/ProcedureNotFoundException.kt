package app.timemate.rrpc.exceptions

import app.timemate.rrpc.metadata.ClientMetadata

public class ProcedureNotFoundException(
    metadata: ClientMetadata
) : Exception("Procedure '${metadata.procedureName}' is not found in service named '${metadata.serviceName}'")