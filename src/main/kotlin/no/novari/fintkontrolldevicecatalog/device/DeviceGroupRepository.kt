package no.novari.fintkontrolldevicecatalog.device

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface DeviceGroupRepository: JpaRepository<DeviceGroup, Long> {
    fun findBySourceId(sourceId: String): DeviceGroup?

}