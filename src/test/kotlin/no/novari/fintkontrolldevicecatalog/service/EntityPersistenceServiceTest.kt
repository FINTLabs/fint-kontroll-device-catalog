package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.entity.*
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupMembershipPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDevice
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.util.Date

class EntityPersistenceServiceTest {
    private val deviceRepository = mock(DeviceRepository::class.java)
    private val groupRepository = mock(DeviceGroupRepository::class.java)
    private val membershipRepository = mock(DeviceGroupMembershipRepository::class.java)
    private val devicePublisher = mock(KontrollDevicePublishingComponent::class.java)
    private val groupPublisher = mock(KontrollDeviceGroupPublishingComponent::class.java)
    private val membershipPublisher = mock(KontrollDeviceGroupMembershipPublishingComponent::class.java)
    private val mappingService = EntityMappingService()
    private val kontrollMappingService = KontrollEntityMappingService()
    private val service = EntityPersistenceService(
        deviceRepository,
        groupRepository,
        membershipRepository,
        mappingService,
        mock(DeviceGroupMembershipRetryBuffer::class.java),
        kontrollMappingService,
        devicePublisher,
        groupPublisher,
        membershipPublisher
    )

    @BeforeEach
    fun setUp() {
        reset(deviceRepository, membershipRepository, devicePublisher, membershipPublisher)
        `when`(deviceRepository.save(any(Device::class.java))).thenAnswer { it.getArgument<Device>(0) }
        `when`(membershipRepository.save(any(DeviceGroupMembership::class.java)))
            .thenAnswer { it.getArgument<DeviceGroupMembership>(0) }
    }

    @Test
    fun `invalidating device makes existing memberships inactive and republishes them`() {
        val existingDevice = device("ACTIVE")
        val oldTimestamp = Date(2_000)
        `when`(deviceRepository.findBySourceId(existingDevice.sourceId)).thenReturn(existingDevice)
        `when`(membershipRepository.findAllByDeviceId(10L)).thenReturn(
            listOf(membership(existingDevice, "ACTIVE", oldTimestamp), membership(existingDevice, "INACTIVE", oldTimestamp, 6L))
        )

        service.handle(message("INVALID"))

        val saved = savedMembership()
        assertEquals("INACTIVE", saved.membershipStatus)
        assertTrue(saved.membershipStatusChanged.after(oldTimestamp))
        assertEquals("INACTIVE", publishedMembership().membershipStatus)
        assertEquals("INVALID", publishedDevice().status)
    }

    @Test
    fun `reactivating device saves and republishes inactive memberships`() {
        val existingDevice = device("INVALID")
        val oldTimestamp = Date(2_000)
        `when`(deviceRepository.findBySourceId(existingDevice.sourceId)).thenReturn(existingDevice)
        `when`(membershipRepository.findAllByDeviceId(10L)).thenReturn(
            listOf(membership(existingDevice, "INACTIVE", oldTimestamp), membership(existingDevice, "ACTIVE", oldTimestamp, 6L))
        )

        service.handle(message("ACTIVE"))

        val saved = savedMembership()
        assertEquals("ACTIVE", saved.membershipStatus)
        assertTrue(saved.membershipStatusChanged.after(oldTimestamp))
        assertEquals("ACTIVE", publishedMembership().membershipStatus)
        assertEquals("ACTIVE", publishedDevice().status)
    }

    @Test
    fun `device with inactive status makes its membership inactive`() {
        val existingDevice = device("ACTIVE")
        `when`(deviceRepository.findBySourceId(existingDevice.sourceId)).thenReturn(existingDevice)
        `when`(membershipRepository.findAllByDeviceId(10L)).thenReturn(
            listOf(membership(existingDevice, "ACTIVE", Date(2_000)))
        )

        service.handle(message("INACTIVE"))

        assertEquals("INACTIVE", savedMembership().membershipStatus)
        assertEquals("INACTIVE", publishedMembership().membershipStatus)
    }

    @Test
    fun `unchanged device status does not update memberships`() {
        val existingDevice = device("ACTIVE")
        `when`(deviceRepository.findBySourceId(existingDevice.sourceId)).thenReturn(existingDevice)

        service.handle(message("ACTIVE"))

        verify(membershipRepository, never()).findAllByDeviceId(10L)
        verifyNoInteractions(membershipPublisher)
        verify(membershipRepository, never()).save(any(DeviceGroupMembership::class.java))
    }

    private fun savedMembership(): DeviceGroupMembership {
        val captor = ArgumentCaptor.forClass(DeviceGroupMembership::class.java)
        verify(membershipRepository).save(captor.capture())
        return captor.value
    }

    private fun publishedMembership(): KontrollDeviceGroupMembership {
        val calls = mockingDetails(membershipPublisher).invocations.filter { it.method.name == "publishOne" }
        assertEquals(1, calls.size)
        return calls.single().getArgument(0)
    }

    private fun publishedDevice(): KontrollDevice {
        val calls = mockingDetails(devicePublisher).invocations.filter { it.method.name == "publishOne" }
        assertEquals(1, calls.size)
        return calls.single().getArgument(0)
    }

    private fun device(status: String) = Device(
        id = 10L,
        sourceId = "sys-1",
        serialNumber = "SN",
        dataObjectId = null,
        name = "Name",
        isPrivateProperty = false,
        isShared = false,
        status = status,
        statusChanged = Date(1_000),
        deviceType = "TYPE",
        platform = "PLATFORM",
        administratorOrgUnitId = "adm",
        ownerOrgUnitId = null
    )

    private fun membership(device: Device, status: String, changed: Date, groupId: Long = 5L): DeviceGroupMembership {
        val group = DeviceGroup(
            id = groupId,
            sourceId = "group-$groupId",
            name = "Group",
            orgUnitId = null,
            orgUnitName = null,
            platform = "FINT",
            deviceType = "TYPE"
        )
        return DeviceGroupMembership(
            id = DeviceGroupMembershipId(deviceGroupId = groupId, deviceId = 10L),
            deviceGroup = group,
            device = device,
            membershipStatus = status,
            membershipStatusChanged = changed
        )
    }

    private fun message(status: String) = KafkaDevice(
        sourceId = "sys-1",
        serialNumber = "SN",
        dataObjectId = null,
        name = "Name",
        isPrivateProperty = false,
        isShared = false,
        status = status,
        deviceType = "TYPE",
        platform = "PLATFORM",
        administratorOrgUnitId = "adm",
        ownerOrgUnitId = null
    )
}
