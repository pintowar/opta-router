package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Instance

interface InstanceRepository {

    fun listAll(): List<Instance>

    fun getById(id: Long): Instance?
}