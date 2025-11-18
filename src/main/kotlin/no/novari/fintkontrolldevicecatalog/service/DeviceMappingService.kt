package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.device.*
import no.novari.fintkontrolldevicecatalog.kaftadevice.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeviceMappingService {

    fun mapKafkaDevice(
        kafkaDevice: KafkaDevice,
        existing: Device? = null
    ): Device {
        println("Mapping KafkaDevice: systemId=${kafkaDevice.systemId}, platform=${kafkaDevice.platform}")

        val base = existing ?: Device(
            sourceId = kafkaDevice.systemId,
            serialNumber = kafkaDevice.serialNumber,
            dataObjectId = kafkaDevice.dataObjectId,
            name = kafkaDevice.name,
            isPrivateProperty = kafkaDevice.isPrivateProperty,
            isShared = kafkaDevice.isShared,
            status = kafkaDevice.status,
            statusChanged = Date(),
            deviceType = kafkaDevice.deviceType,
            platform = kafkaDevice.platform,
            administratorOrgUnitId = kafkaDevice.administratorOrgUnitId,
            ownerOrgUnitId = kafkaDevice.ownerOrgUnitId,
            memberships = mutableSetOf()
        )

        val newStatus = kafkaDevice.status ?: base.status
        val statusChanged =
            if (newStatus != null && newStatus != base.status) Date() else base.statusChanged

        return base.copy(
            serialNumber = kafkaDevice.serialNumber,
            deviceType = kafkaDevice.deviceType,
            platform = kafkaDevice.platform,
            dataObjectId = kafkaDevice.dataObjectId ?: base.dataObjectId,
            name = kafkaDevice.name ?: base.name,
            isPrivateProperty = kafkaDevice.isPrivateProperty ?: base.isPrivateProperty,
            isShared = kafkaDevice.isShared ?: base.isShared,
            status = newStatus,
            statusChanged = statusChanged,
            administratorOrgUnitId = kafkaDevice.administratorOrgUnitId ?: base.administratorOrgUnitId,
            ownerOrgUnitId = kafkaDevice.ownerOrgUnitId ?: base.ownerOrgUnitId,
            memberships = base.memberships
        )
    }

    fun mapKafkaDeviceGroup(
        kafkaDeviceGroup: KafkaDeviceGroup,
        existing: DeviceGroup? = null
    ): DeviceGroup {
        println("Mapping KafkaDeviceGroup: systemId=${kafkaDeviceGroup.systemId}, name=${kafkaDeviceGroup.name}")

        val base = existing ?: DeviceGroup(
            sourceId = kafkaDeviceGroup.systemId,
            name = kafkaDeviceGroup.name,
            orgUnitId = kafkaDeviceGroup.orgUnitId,
            platform = kafkaDeviceGroup.platform,
            deviceType = kafkaDeviceGroup.deviceType,
            memberships = mutableSetOf()
        )

        return base.copy(
            name = kafkaDeviceGroup.name,
            orgUnitId = kafkaDeviceGroup.orgUnitId ?: base.orgUnitId,
            platform = kafkaDeviceGroup.platform,
            deviceType = kafkaDeviceGroup.deviceType,
            memberships = base.memberships
        )
    }


    fun mapKafkaDeviceGroupMembership(
        kafkaDeviceGroupMembership: KafkaDeviceGroupMembership,
        device: Device,
        group: DeviceGroup,
        existing: DeviceGroupMembership? = null
    ): DeviceGroupMembership {
        println("Mapping KafkaDeviceGroupMembership: groupId=${kafkaDeviceGroupMembership.groupId}, deviceId=${kafkaDeviceGroupMembership.deviceId}")

        val id = existing?.id ?: DeviceGroupMembershipId(
            deviceGroupId = group.id!!,
            deviceId = device.id!!
        )

        val base = existing ?: DeviceGroupMembership(
            id = id,
            deviceGroup = group,
            device = device,
            groupId = kafkaDeviceGroupMembership.groupId,
            membershipStatus = null,
            membershipStatusChanged = null
        )

        return base.copy(
            deviceGroup = group,
            device = device,
            groupId = kafkaDeviceGroupMembership.groupId,
            membershipStatus = base.membershipStatus,
            membershipStatusChanged = base.membershipStatusChanged
        )
    }
}