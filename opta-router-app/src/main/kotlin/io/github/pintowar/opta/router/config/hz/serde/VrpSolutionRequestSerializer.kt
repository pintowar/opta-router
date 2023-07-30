package io.github.pintowar.opta.router.config.hz.serde

import com.hazelcast.nio.serialization.compact.CompactReader
import com.hazelcast.nio.serialization.compact.CompactSerializer
import com.hazelcast.nio.serialization.compact.CompactWriter
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import java.util.*

class VrpSolutionRequestSerializer : CompactSerializer<VrpSolutionRequest> {
    override fun read(reader: CompactReader): VrpSolutionRequest {
        val solution = reader.readCompact<VrpSolution>("solution")!!
        val status = reader.readString("status")!!
        val solverKey = reader.readString("solverKey")
        return VrpSolutionRequest(solution, SolverStatus.valueOf(status), UUID.fromString(solverKey))
    }

    override fun write(writer: CompactWriter, request: VrpSolutionRequest) {
        writer.writeCompact("solution", request.solution)
        writer.writeString("status", request.status.toString())
        writer.writeString("solverKey", request.solverKey?.toString())
    }

    override fun getTypeName(): String {
        return "VrpSolutionRequest"
    }

    override fun getCompactClass(): Class<VrpSolutionRequest> {
        return VrpSolutionRequest::class.java
    }
}