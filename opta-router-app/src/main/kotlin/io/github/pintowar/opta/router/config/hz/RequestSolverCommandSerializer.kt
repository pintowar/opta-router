package io.github.pintowar.opta.router.config.hz

import com.hazelcast.nio.serialization.compact.CompactReader
import com.hazelcast.nio.serialization.compact.CompactSerializer
import com.hazelcast.nio.serialization.compact.CompactWriter
import io.github.pintowar.opta.router.core.domain.ports.SolverQueuePort
import java.util.*

class RequestSolverCommandSerializer: CompactSerializer<SolverQueuePort.RequestSolverCommand> {
    override fun read(reader: CompactReader): SolverQueuePort.RequestSolverCommand {
        val problemId = reader.readInt64("problemId")
        val uuid = reader.readString("uuid")
        val solverName = reader.readString("solverName")
        return SolverQueuePort.RequestSolverCommand(problemId, UUID.fromString(uuid), solverName!!)
    }

    override fun write(writer: CompactWriter, cmd: SolverQueuePort.RequestSolverCommand) {
        writer.writeInt64("problemId", cmd.problemId)
        writer.writeString("uuid", cmd.uuid.toString())
        writer.writeString("solverName", cmd.solverName)
    }

    override fun getTypeName(): String {
        return "RequestSolverCommand"
    }

    override fun getCompactClass(): Class<SolverQueuePort.RequestSolverCommand> {
        return SolverQueuePort.RequestSolverCommand::class.java
    }

}