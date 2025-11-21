package no.novari.fintkontrolldevicecatalog.entity

import org.springframework.data.jpa.repository.JpaRepository

interface DeviceGroupRepository: JpaRepository<DeviceGroup, Long> {
    fun findBySourceId(sourceId: String): DeviceGroup?

}