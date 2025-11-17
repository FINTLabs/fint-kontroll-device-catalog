package no.novari.fintkontrolldevicecatalog.device

import org.springframework.data.jpa.repository.JpaRepository

interface DeviceGroupRepository: JpaRepository<DeviceGroup, Long> {
    fun findBySourceId(sourceId: String): DeviceGroup?
}