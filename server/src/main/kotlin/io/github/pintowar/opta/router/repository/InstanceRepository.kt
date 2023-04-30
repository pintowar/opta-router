package io.github.pintowar.opta.router.repository

import io.github.pintowar.opta.router.vrp.Instance

interface InstanceRepository {

    fun listAll(): List<Instance>

    fun getById(id: Long): Instance?
}