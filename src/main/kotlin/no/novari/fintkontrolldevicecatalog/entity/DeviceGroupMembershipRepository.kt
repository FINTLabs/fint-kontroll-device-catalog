package no.novari.fintkontrolldevicecatalog.entity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DeviceGroupMembershipRepository: JpaRepository<DeviceGroupMembership, DeviceGroupMembershipId> {

    @Query("select d.device from DeviceGroupMembership d where d.deviceGroup.id = :id")
    fun getDevicesInDeviceGroupByDeviceGroupId(@Param("id") id : Long): List<Device>

    @Query("select d.device from DeviceGroupMembership d where d.deviceGroup.id = :id")
    fun getDevicesInDeviceGroupByDeviceGroupIdPaged(@Param("id") id : Long, pagable: Pageable): Page<Device>

}