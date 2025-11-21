package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.entity.Device
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroup
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroup
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(KontrollDeviceMappingService::class.java)

@Service
class KontrollDeviceMappingService {

    fun mapDeviceToKontrollDevice(
        device: Device,
    ): KontrollDevice {
        logger.info("Mapping device to kontrolldevice ${device.name}")

        return KontrollDevice(
            id = device.id ?: 0,
            sourceId = device.sourceId,
            serialNumber = device.serialNumber,
            dataObjectId = device.dataObjectId,
            name = device.name ?: "UNKNOWN",
            isPrivateProperty = device.isPrivateProperty,
            isShared = device.isShared,
            status = device.status ?: "UNKNOWN",
            deviceType = device.deviceType,
            platform = device.platform,
            administratorOrgUnitId = device.administratorOrgUnitId ?: "UNKNOWN",
            ownerOrgUnitId = device.ownerOrgUnitId
        )
    }

    fun mapDeviceGroupToKontrollDeviceGroup(
        deviceGroup: DeviceGroup
    ): KontrollDeviceGroup {
        logger.info("Mapping device group to kontrolldevice group ${deviceGroup.name}")
        return KontrollDeviceGroup(
            id = deviceGroup.id ?: 0,
            sourceId = deviceGroup.sourceId,
            name = deviceGroup.name,
            orgUnitId = deviceGroup.orgUnitId,
            platform = deviceGroup.platform,
            deviceType = deviceGroup.deviceType
        )
    }

    fun mapDeviceGroupMembershipToKontrollDeviceGroupMembership(
        deviceGroupMembership: DeviceGroupMembership
    ): KontrollDeviceGroupMembership {
        return KontrollDeviceGroupMembership(
            deviceGroupId = deviceGroupMembership.deviceGroup.id ?: 0,
            deviceId = deviceGroupMembership.device.id ?: 0,
            membershipStatus = deviceGroupMembership.membershipStatus ?: "UNKNOWN",
        )
    }

}