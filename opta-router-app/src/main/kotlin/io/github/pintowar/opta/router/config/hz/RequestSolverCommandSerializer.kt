package io.github.pintowar.opta.router.config.hz

import com.hazelcast.nio.serialization.compact.CompactReader
import com.hazelcast.nio.serialization.compact.CompactSerializer
import com.hazelcast.nio.serialization.compact.CompactWriter
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import java.util.*

class RequestSolverCommandSerializer: CompactSerializer<SolverEventsPort.RequestSolverCommand> {
    override fun read(reader: CompactReader): SolverEventsPort.RequestSolverCommand {
        val problemId = reader.readInt64("problemId")
        val uuid = reader.readString("uuid")
        val solverName = reader.readString("solverName")
        return SolverEventsPort.RequestSolverCommand(problemId, UUID.fromString(uuid), solverName!!)
    }

    override fun write(writer: CompactWriter, cmd: SolverEventsPort.RequestSolverCommand) {
        writer.writeInt64("problemId", cmd.problemId)
        writer.writeString("uuid", cmd.uuid.toString())
        writer.writeString("solverName", cmd.solverName)
    }

    override fun getTypeName(): String {
        return "RequestSolverCommand"
    }

    override fun getCompactClass(): Class<SolverEventsPort.RequestSolverCommand> {
        return SolverEventsPort.RequestSolverCommand::class.java
    }

}