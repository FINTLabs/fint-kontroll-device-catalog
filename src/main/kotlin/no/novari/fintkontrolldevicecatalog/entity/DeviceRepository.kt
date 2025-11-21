package no.novari.fintkontrolldevicecatalog.entity

import org.springframework.data.jpa.repository.JpaRepository

interface DeviceRepository: JpaRepository<Device, Long> {
    fun findBySourceId(sourceId: String): Device?
}