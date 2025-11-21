package no.novari.fintkontrolldevicecatalog.entity

import org.springframework.data.jpa.repository.JpaRepository

interface DeviceGroupMembershipRepository: JpaRepository<DeviceGroupMembership, DeviceGroupMembershipId> {
}