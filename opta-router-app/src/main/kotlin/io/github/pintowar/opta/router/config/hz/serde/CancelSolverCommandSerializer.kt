package io.github.pintowar.opta.router.config.hz.serde

import com.hazelcast.nio.serialization.compact.CompactReader
import com.hazelcast.nio.serialization.compact.CompactSerializer
import com.hazelcast.nio.serialization.compact.CompactWriter
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import java.util.*

class CancelSolverCommandSerializer : CompactSerializer<SolverEventsPort.CancelSolverCommand> {
    override fun read(reader: CompactReader): SolverEventsPort.CancelSolverCommand {
        val solverKey = reader.readString("solverKey")
        val currentStatus = SolverStatus.valueOf(reader.readString("currentStatus")!!)
        val clear = reader.readBoolean("clear")
        return SolverEventsPort.CancelSolverCommand(UUID.fromString(solverKey), currentStatus, clear)
    }

    override fun write(writer: CompactWriter, cmd: SolverEventsPort.CancelSolverCommand) {
        writer.writeString("solverKey", cmd.solverKey.toString())
        writer.writeString("currentStatus", cmd.currentStatus.toString())
        writer.writeBoolean("clear", cmd.clear)
    }

    override fun getTypeName(): String {
        return "CancelSolverCommand"
    }

    override fun getCompactClass(): Class<SolverEventsPort.CancelSolverCommand> {
        return SolverEventsPort.CancelSolverCommand::class.java
    }
}