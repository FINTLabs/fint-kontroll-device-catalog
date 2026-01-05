package no.novari.fintkontrolldevicecatalog.entity

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DeviceGroupRepository: JpaRepository<DeviceGroup, Long> {
    fun findBySourceId(sourceId: String): DeviceGroup?

    @Modifying
    @Transactional
    @Query(
        """
        update DeviceGroup g
        set g.noOfMembers = (
            select count(dgm)
            from DeviceGroupMembership dgm
            where dgm.deviceGroup.id = :groupId
        )
        where g.id = :groupId
        """
    )
    fun syncNoOfMembers(@Param("groupId") groupId: Long): Int
}