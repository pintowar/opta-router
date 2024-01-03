package io.github.pintowar.opta.router.config.hz.serde

import com.hazelcast.nio.serialization.compact.CompactReader
import com.hazelcast.nio.serialization.compact.CompactSerializer
import com.hazelcast.nio.serialization.compact.CompactWriter
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import java.util.*

class RequestSolverCommandSerializer : CompactSerializer<RequestSolverCommand> {
    override fun read(reader: CompactReader): RequestSolverCommand {
        val detailedSolution = reader.readCompact<VrpDetailedSolution>("detailedSolution")!!
        val solverKey = reader.readString("solverKey")
        val solverName = reader.readString("solverName")
        return RequestSolverCommand(detailedSolution, UUID.fromString(solverKey), solverName!!)
    }

    override fun write(writer: CompactWriter, cmd: RequestSolverCommand) {
        writer.writeCompact("detailedSolution", cmd.detailedSolution)
        writer.writeString("solverKey", cmd.solverKey.toString())
        writer.writeString("solverName", cmd.solverName)
    }

    override fun getTypeName(): String {
        return "RequestSolverCommand"
    }

    override fun getCompactClass(): Class<RequestSolverCommand> {
        return RequestSolverCommand::class.java
    }
}