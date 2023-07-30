package io.github.pintowar.opta.router.config.hz

import com.hazelcast.nio.serialization.compact.CompactReader
import com.hazelcast.nio.serialization.compact.CompactSerializer
import com.hazelcast.nio.serialization.compact.CompactWriter
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import java.util.*

class RequestSolverCommandSerializer: CompactSerializer<SolverEventsPort.RequestSolverCommand> {
    override fun read(reader: CompactReader): SolverEventsPort.RequestSolverCommand {
        val detailedSolution = reader.readCompact<VrpDetailedSolution>("detailedSolution")!!
        val solverKey = reader.readString("solverKey")
        val solverName = reader.readString("solverName")
        return SolverEventsPort.RequestSolverCommand(detailedSolution, UUID.fromString(solverKey), solverName!!)
    }

    override fun write(writer: CompactWriter, cmd: SolverEventsPort.RequestSolverCommand) {
        writer.writeCompact("detailedSolution", cmd.detailedSolution)
        writer.writeString("solverKey", cmd.solverKey.toString())
        writer.writeString("solverName", cmd.solverName)
    }

    override fun getTypeName(): String {
        return "RequestSolverCommand"
    }

    override fun getCompactClass(): Class<SolverEventsPort.RequestSolverCommand> {
        return SolverEventsPort.RequestSolverCommand::class.java
    }

}