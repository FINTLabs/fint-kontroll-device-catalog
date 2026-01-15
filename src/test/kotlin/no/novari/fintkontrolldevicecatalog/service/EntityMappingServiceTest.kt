package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.entity.*
import no.novari.fintkontrolldevicecatalog.kaftaentity.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Date


class EntityMappingServiceTest {

    private lateinit var service: EntityMappingService

    @BeforeEach
    fun setup() {
        service = EntityMappingService()
    }

    @Test
    fun `mapKafkaDeviceToDevice should map entity from kafka to internal entity (new device)`() {
        val kafka = KafkaDevice(
            systemId = "sys-1",
            serialNumber = "SN-123",
            dataObjectId = "do-1",
            name = "Device A",
            isPrivateProperty = true,
            isShared = false,
            status = "ACTIVE",
            deviceType = "LAPTOP",
            platform = "FINT",
            administratorOrgUnitId = "adm-1",
            ownerOrgUnitId = "own-1"
        )

        val result = service.mapKafkaDeviceToDevice(kafka)

        assertEquals("sys-1", result.sourceId)
        assertEquals("SN-123", result.serialNumber)
        assertEquals("do-1", result.dataObjectId)
        assertEquals("Device A", result.name)
        assertEquals(true, result.isPrivateProperty)
        assertEquals(false, result.isShared)
        assertEquals("ACTIVE", result.status)
        assertNotNull(result.statusChanged)

        assertEquals("LAPTOP", result.deviceType)
        assertEquals("FINT", result.platform)
        assertEquals("adm-1", result.administratorOrgUnitId)
        assertEquals("own-1", result.ownerOrgUnitId)
    }

    @Test
    fun `mapKafkaDeviceToDevice should keep existing values when kafka values are null`() {
        val memberships = mutableSetOf<DeviceGroupMembership>()
        val existing = Device(
            id = 10L,
            sourceId = "sys-1",
            serialNumber = "OLD-SN",
            dataObjectId = "old-do",
            name = "Old Name",
            isPrivateProperty = false,
            isShared = true,
            status = "ACTIVE",
            statusChanged = Date(1_000),
            deviceType = "OLDTYPE",
            platform = "OLDPLATFORM",
            administratorOrgUnitId = "old-adm",
            ownerOrgUnitId = "old-own",
            memberships = memberships
        )

        val kafka = KafkaDevice(
            systemId = "sys-1",
            serialNumber = "NEW-SN",          // always overwrites
            dataObjectId = null,              // should keep old-do
            name = null,                      // should keep Old Name
            isPrivateProperty = null,          // should keep false
            isShared = null,                  // should keep true
            status = null,                    // should keep ACTIVE
            deviceType = "NEWTYPE",           // overwrites
            platform = "NEWPLATFORM",         // overwrites
            administratorOrgUnitId = null,    // should keep old-adm
            ownerOrgUnitId = null             // should keep old-own
        )

        val result = service.mapKafkaDeviceToDevice(kafka, existing)

        assertEquals(10L, result.id)
        assertEquals("sys-1", result.sourceId)

        assertEquals("NEW-SN", result.serialNumber)
        assertEquals("old-do", result.dataObjectId)
        assertEquals("Old Name", result.name)
        assertEquals(false, result.isPrivateProperty)
        assertEquals(true, result.isShared)
        assertEquals("ACTIVE", result.status)
        assertEquals(Date(1_000), result.statusChanged)

        assertEquals("NEWTYPE", result.deviceType)
        assertEquals("NEWPLATFORM", result.platform)

        assertEquals("old-adm", result.administratorOrgUnitId)
        assertEquals("old-own", result.ownerOrgUnitId)

        // memberships should be preserved (service assigns memberships = base.memberships)
        assertSame(memberships, result.memberships)
    }

    @Test
    fun `mapKafkaDeviceToDevice should update statusChanged only when status changes`() {
        val existing = Device(
            id = 10L,
            sourceId = "sys-1",
            serialNumber = "SN",
            dataObjectId = "do",
            name = "Name",
            isPrivateProperty = false,
            isShared = false,
            status = "ACTIVE",
            statusChanged = Date.from(Instant.parse("2024-01-15T10:30:00Z")),
            deviceType = "TYPE",
            platform = "PLATFORM",
            administratorOrgUnitId = "adm",
            ownerOrgUnitId = "own",
            memberships = mutableSetOf()
        )

        val kafka = KafkaDevice(
            systemId = "sys-1",
            serialNumber = "SN",
            dataObjectId = "do",
            name = "Name",
            isPrivateProperty = false,
            isShared = false,
            status = "INACTIVE", // change
            deviceType = "TYPE",
            platform = "PLATFORM",
            administratorOrgUnitId = "adm",
            ownerOrgUnitId = "own"
        )

        val before = System.currentTimeMillis()
        val result = service.mapKafkaDeviceToDevice(kafka, existing)
        val after = System.currentTimeMillis()

        assertEquals("INACTIVE", result.status)
        assertTrue(result.statusChanged?.time in before..after)
    }

    @Test
    fun `mapKafkaDeviceToDevice should NOT change statusChanged when status stays the same`() {
        val existing = Device(
            id = 10L,
            sourceId = "sys-1",
            serialNumber = "SN",
            dataObjectId = "do",
            name = "Name",
            isPrivateProperty = false,
            isShared = false,
            status = "ACTIVE",
            statusChanged = Date(1_000),
            deviceType = "TYPE",
            platform = "PLATFORM",
            administratorOrgUnitId = "adm",
            ownerOrgUnitId = "own",
            memberships = mutableSetOf()
        )

        val kafka = KafkaDevice(
            systemId = "sys-1",
            serialNumber = "SN",
            dataObjectId = "do",
            name = "Name",
            isPrivateProperty = false,
            isShared = false,
            status = "ACTIVE", // same
            deviceType = "TYPE",
            platform = "PLATFORM",
            administratorOrgUnitId = "adm",
            ownerOrgUnitId = "own"
        )

        val result = service.mapKafkaDeviceToDevice(kafka, existing)

        assertEquals("ACTIVE", result.status)
        assertEquals(Date(1_000), result.statusChanged)
    }

    @Test
    fun `mapKafkaDeviceGroupToDeviceGroup should map new group`() {
        val kafka = KafkaDeviceGroup(
            systemId = "grp-1",
            name = "Group A",
            orgUnitId = "ou-1",
            orgUnitName = "ou-1",
            platform = "FINT",
            deviceType = "LAPTOP"
        )

        val result = service.mapKafkaDeviceGroupToDeviceGroup(kafka)

        assertEquals("grp-1", result.sourceId)
        assertEquals("Group A", result.name)
        assertEquals("ou-1", result.orgUnitId)
        assertEquals("FINT", result.platform)
        assertEquals("LAPTOP", result.deviceType)

    }

    @Test
    fun `mapKafkaDeviceGroupMembershipToDeviceGroupMembership should create id from device and group when existing null`() {
        val device = Device(
            id = 10L,
            sourceId = "sys-1",
            serialNumber = "SN",
            dataObjectId = "do",
            name = "Name",
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
            orgUnitName = "ou-1",
            platform = "FINT",
            deviceType = "TYPE",
            memberships = mutableSetOf()
        )

        val kafkaMembership = KafkaDeviceGroupMembership(
            groupId = "grp-1",
            deviceId = "sys-1"
        )

        val result = service.mapKafkaDeviceGroupMembershipToDeviceGroupMembership(
            kafkaDeviceGroupMembership = kafkaMembership,
            device = device,
            group = group,
            existing = null
        )

        assertEquals(5L, result.id.deviceGroupId)
        assertEquals(10L, result.id.deviceId)
        assertSame(group, result.deviceGroup)
        assertSame(device, result.device)
        assertNull(result.membershipStatus)
        assertNull(result.membershipStatusChanged)
    }

    @Test
    fun `mapKafkaDeviceGroupMembershipToDeviceGroupMembership should preserve membership status fields when existing provided`() {
        val device = Device(
            id = 10L,
            sourceId = "sys-1",
            serialNumber = "SN",
            dataObjectId = "do",
            name = "Name",
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
            orgUnitName = "ou-1",
            platform = "FINT",
            deviceType = "TYPE",
            memberships = mutableSetOf()
        )

        val existing = DeviceGroupMembership(
            id = DeviceGroupMembershipId(deviceGroupId = 5L, deviceId = 10L),
            deviceGroup = group,
            device = device,
            membershipStatus = "MEMBER",
            membershipStatusChanged = Date(2_000)
        )

        val kafkaMembership = KafkaDeviceGroupMembership(
            groupId = "grp-1",
            deviceId = "sys-1"
        )

        val result = service.mapKafkaDeviceGroupMembershipToDeviceGroupMembership(
            kafkaDeviceGroupMembership = kafkaMembership,
            device = device,
            group = group,
            existing = existing
        )

        assertEquals("MEMBER", result.membershipStatus)
        assertEquals(Date(2_000), result.membershipStatusChanged)
        assertSame(group, result.deviceGroup)
        assertSame(device, result.device)
    }
}