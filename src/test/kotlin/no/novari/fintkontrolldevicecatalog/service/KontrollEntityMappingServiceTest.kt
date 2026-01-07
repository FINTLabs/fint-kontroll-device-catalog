package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.entity.Device
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroup
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupMembershipId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

class KontrollEntityMappingServiceTest {

    private lateinit var service: KontrollEntityMappingService

    @BeforeEach
    fun setup() {
        service = KontrollEntityMappingService()
    }

    @Test
    fun `mapDeviceToKontrollDevice should map all fields and use id when present`() {
        val device = Device(
            id = 10L,
            sourceId = "sys-1",
            serialNumber = "SN-123",
            dataObjectId = "do-1",
            name = "Device A",
            isPrivateProperty = true,
            isShared = false,
            status = "ACTIVE",
            statusChanged = Date(),
            deviceType = "LAPTOP",
            platform = "FINT",
            administratorOrgUnitId = "adm-1",
            ownerOrgUnitId = "own-1",
            memberships = mutableSetOf()
        )

        val result = service.mapDeviceToKontrollDevice(device)

        assertEquals(10L, result.id)
        assertEquals("sys-1", result.sourceId)
        assertEquals("SN-123", result.serialNumber)
        assertEquals("do-1", result.dataObjectId)
        assertEquals("Device A", result.name)
        assertEquals(true, result.isPrivateProperty)
        assertEquals(false, result.isShared)
        assertEquals("ACTIVE", result.status)
        assertEquals("LAPTOP", result.deviceType)
        assertEquals("FINT", result.platform)
        assertEquals("adm-1", result.administratorOrgUnitId)
        assertEquals("own-1", result.ownerOrgUnitId)
    }

    @Test
    fun `mapDeviceToKontrollDevice should default nullable fields to UNKNOWN and id to 0`() {
        val device = Device(
            id = null,
            sourceId = "sys-1",
            serialNumber = "SN-123",
            dataObjectId = null,
            name = null,
            isPrivateProperty = false,
            isShared = true,
            status = null,
            statusChanged = null,
            deviceType = "TYPE",
            platform = "PLATFORM",
            administratorOrgUnitId = null,
            ownerOrgUnitId = null,
            memberships = mutableSetOf()
        )

        val result = service.mapDeviceToKontrollDevice(device)

        assertEquals(0L, result.id)
        assertEquals("sys-1", result.sourceId)
        assertEquals("SN-123", result.serialNumber)
        assertNull(result.dataObjectId)

        assertEquals("UNKNOWN", result.name)
        assertEquals(false, result.isPrivateProperty)
        assertEquals(true, result.isShared)

        assertEquals("UNKNOWN", result.status)
        assertEquals("TYPE", result.deviceType)
        assertEquals("PLATFORM", result.platform)

        assertEquals("UNKNOWN", result.administratorOrgUnitId)
        assertNull(result.ownerOrgUnitId)
    }

    @Test
    fun `mapDeviceGroupToKontrollDeviceGroup should map all fields and use id when present`() {
        val group = DeviceGroup(
            id = 5L,
            sourceId = "grp-1",
            name = "Group A",
            orgUnitId = "ou-1",
            platform = "FINT",
            deviceType = "LAPTOP",
            memberships = mutableSetOf()
        )

        val result = service.mapDeviceGroupToKontrollDeviceGroup(group)

        assertEquals(5L, result.id)
        assertEquals("grp-1", result.sourceId)
        assertEquals("Group A", result.name)
        assertEquals("ou-1", result.orgUnitId)
        assertEquals("FINT", result.platform)
        assertEquals("LAPTOP", result.deviceType)
    }

    @Test
    fun `mapDeviceGroupToKontrollDeviceGroup should default id to 0 when null`() {
        val group = DeviceGroup(
            id = null,
            sourceId = "grp-1",
            name = "Group A",
            orgUnitId = "ou-1",
            platform = "FINT",
            deviceType = "LAPTOP",
            memberships = mutableSetOf()
        )

        val result = service.mapDeviceGroupToKontrollDeviceGroup(group)

        assertEquals(0L, result.id)
        assertEquals("grp-1", result.sourceId)
    }

    @Test
    fun `mapDeviceGroupMembershipToKontrollDeviceGroupMembership should map ids and membershipStatus`() {
        val device = Device(
            id = 10L,
            sourceId = "sys-1",
            serialNumber = "SN",
            dataObjectId = "do",
            name = "Device",
            isPrivateProperty = false,
            isShared = false,
            status = "ACTIVE",
            statusChanged = Date(),
            deviceType = "TYPE",
            platform = "PLATFORM",
            administratorOrgUnitId = "adm",
            ownerOrgUnitId = "own",
            memberships = mutableSetOf()
        )

        val group = DeviceGroup(
            id = 5L,
            sourceId = "grp-1",
            name = "Group",
            orgUnitId = "ou",
            platform = "FINT",
            deviceType = "TYPE",
            memberships = mutableSetOf()
        )

        val membership = DeviceGroupMembership(
            id = DeviceGroupMembershipId(deviceGroupId = 5L, deviceId = 10L),
            deviceGroup = group,
            device = device,
            membershipStatus = "MEMBER",
            membershipStatusChanged = Date()
        )

        val result = service.mapDeviceGroupMembershipToKontrollDeviceGroupMembership(membership)

        assertEquals(5L, result.deviceGroupId)
        assertEquals(10L, result.deviceId)
        assertEquals("MEMBER", result.membershipStatus)
    }

    @Test
    fun `mapDeviceGroupMembershipToKontrollDeviceGroupMembership should default ids to 0 and membershipStatus to UNKNOWN`() {
        val device = Device(
            id = null,
            sourceId = "sys-1",
            serialNumber = "SN",
            dataObjectId = null,
            name = null,
            isPrivateProperty = false,
            isShared = false,
            status = null,
            statusChanged = null,
            deviceType = "TYPE",
            platform = "PLATFORM",
            administratorOrgUnitId = null,
            ownerOrgUnitId = null,
            memberships = mutableSetOf()
        )

        val group = DeviceGroup(
            id = null,
            sourceId = "grp-1",
            name = "Group",
            orgUnitId = "ou",
            platform = "FINT",
            deviceType = "TYPE",
            memberships = mutableSetOf()
        )

        val membership = DeviceGroupMembership(
            id = DeviceGroupMembershipId(deviceGroupId = 0L, deviceId = 0L),
            deviceGroup = group,
            device = device,
            membershipStatus = null,
            membershipStatusChanged = null
        )

        val result = service.mapDeviceGroupMembershipToKontrollDeviceGroupMembership(membership)

        assertEquals(0L, result.deviceGroupId)
        assertEquals(0L, result.deviceId)
        assertEquals("UNKNOWN", result.membershipStatus)
    }
}