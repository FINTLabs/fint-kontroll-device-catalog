package no.novari.fintkontrolldevicecatalog.device

import org.springframework.data.jpa.repository.JpaRepository

interface DeviceGroupMembershipRepository: JpaRepository<DeviceGroupMembership, DeviceGroupMembershipId> {
}