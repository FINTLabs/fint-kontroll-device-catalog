package no.novari.fintkontrolldevicecatalog.entity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DeviceGroupMembershipRepository: JpaRepository<DeviceGroupMembership, DeviceGroupMembershipId> {

    @Query("select d.device from DeviceGroupMembership d where d.deviceGroup.id = :id")
    fun getDevicesInDeviceGroupByDeviceGroupId(@Param("id") id : Long): List<Device>

    fun getDevicesInDeviceGroupByDeviceGroupIdPaged(
        @Param("id") id : Long,
        pageable: Pageable,
    ): Page<Device>

    @Query(
        """
        select d.device
        from DeviceGroupMembership d
        where d.deviceGroup.id = :id
        and lower(coalesce(d.device.name, '')) like lower(concat('%', :search, '%'))
        """,
    )
    fun getDevicesInDeviceGroupByDeviceGroupIdPagedWithSearch(
        @Param("id") id : Long,
        @Param("search") search: String,
        pageable: Pageable,
    ): Page<Device>

}
