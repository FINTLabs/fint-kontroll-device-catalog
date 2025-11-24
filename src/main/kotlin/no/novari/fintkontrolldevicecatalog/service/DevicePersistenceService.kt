package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.entity.*
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupMembershipPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import no.novari.fintkontrolldevicecatalog.kaftaentity.*
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroup
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import org.springframework.stereotype.Service
import java.security.PrivateKey

@Service
class DevicePersistenceService(
    private val deviceRepository: DeviceRepository,
    private val deviceGroupRepository: DeviceGroupRepository,
    private val deviceGroupMembershipRepository: DeviceGroupMembershipRepository,
    private val deviceMappingService: DeviceMappingService,
    private val membershipRetryBuffer: MembershipRetryBuffer,
    private val kontrollDeviceMappingService: KontrollDeviceMappingService,
    private val kontrollDevicePublishingComponent: KontrollDevicePublishingComponent,
    private val kontrollDeviceGroupPublishingComponent: KontrollDeviceGroupPublishingComponent,
    private val kontrollDeviceGroupMembershipPublishingComponent: KontrollDeviceGroupMembershipPublishingComponent

) {

    fun <T : KafkaEntity> handle(entity: T) {
        when (entity) {
            is KafkaDevice -> handleDevice(entity)
            is KafkaDeviceGroup -> handleDeviceGroup(entity)
            is KafkaDeviceGroupMembership -> handleDeviceGroupMembership(entity)
        }
    }

    private fun handleDevice(kafka: KafkaDevice) {
        val existing = deviceRepository.findBySourceId(kafka.systemId)
        val mapped = deviceMappingService.mapKafkaDeviceToDevice(kafka, existing)
        val savedDevice: Device = deviceRepository.save(mapped)
        val kontrollDevice: KontrollDevice = kontrollDeviceMappingService.mapDeviceToKontrollDevice(savedDevice)
        kontrollDevicePublishingComponent.publishOne(kontrollDevice)
    }

    private fun handleDeviceGroup(kafka: KafkaDeviceGroup) {
        val existing = deviceGroupRepository.findBySourceId(kafka.systemId)
        val mapped = deviceMappingService.mapKafkaDeviceGroupToDeviceGroup(kafka, existing)
        val savedDeviceGroup: DeviceGroup = deviceGroupRepository.save(mapped)
        val kontrollDeviceGroup: KontrollDeviceGroup = kontrollDeviceMappingService.mapDeviceGroupToKontrollDeviceGroup(savedDeviceGroup)
        kontrollDeviceGroupPublishingComponent.publishOne(kontrollDeviceGroup)
    }

    private fun handleDeviceGroupMembership(kafkaDeviceGroupMembership: KafkaDeviceGroupMembership) {
        println("Handling membership for ${kafkaDeviceGroupMembership.deviceId}_${kafkaDeviceGroupMembership.groupId}")
        val group = deviceGroupRepository.findBySourceId(kafkaDeviceGroupMembership.groupId)
        val device = deviceRepository.findBySourceId(kafkaDeviceGroupMembership.deviceId)

        if (group == null || device == null) {
            membershipRetryBuffer.add(kafkaDeviceGroupMembership)
            return
        }

        val id = DeviceGroupMembershipId(
            deviceGroupId = group.id!!,
            deviceId = device.id!!
        )

        val existing = deviceGroupMembershipRepository.findById(id).orElse(null)

        val mapped = deviceMappingService.mapKafkaDeviceGroupMembershipToDeviceGroupMembership(
            kafkaDeviceGroupMembership = kafkaDeviceGroupMembership,
            device = device,
            group = group,
            existing = existing
        )

        val savedDeviceGroupMembership = deviceGroupMembershipRepository.save(mapped)
        val kontrollDeviceGroupMembership: KontrollDeviceGroupMembership =
            kontrollDeviceMappingService.mapDeviceGroupMembershipToKontrollDeviceGroupMembership(savedDeviceGroupMembership)
        kontrollDeviceGroupMembershipPublishingComponent.publishOne(kontrollDeviceGroupMembership)
    }
}