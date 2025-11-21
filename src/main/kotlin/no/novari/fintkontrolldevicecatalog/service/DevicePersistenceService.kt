package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.entity.*
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import no.novari.fintkontrolldevicecatalog.kaftaentity.*
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import org.springframework.stereotype.Service

@Service
class DevicePersistenceService(
    private val deviceRepository: DeviceRepository,
    private val deviceGroupRepository: DeviceGroupRepository,
    private val deviceGroupMembershipRepository: DeviceGroupMembershipRepository,
    private val deviceMappingService: DeviceMappingService,
    private val membershipRetryBuffer: MembershipRetryBuffer,
    private val kontrollDeviceMappingService: KontrollDeviceMappingService,
    private val kontrollDevicePublishingComponent: KontrollDevicePublishingComponent

) {

    fun <T : KafkaEntity> handle(entity: T) {
        when (entity) {
            is KafkaDevice -> handleDevice(entity)
            is KafkaDeviceGroup -> handleDeviceGroup(entity)
            is KafkaDeviceGroupMembership -> handleMembership(entity)
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
        deviceGroupRepository.save(mapped)
    }

    private fun handleMembership(kafkaDeviceGroupMembership: KafkaDeviceGroupMembership) {
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

        deviceGroupMembershipRepository.save(mapped)
    }
}