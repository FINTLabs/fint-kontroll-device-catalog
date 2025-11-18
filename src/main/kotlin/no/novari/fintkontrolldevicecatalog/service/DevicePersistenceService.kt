package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.device.*
import no.novari.fintkontrolldevicecatalog.kaftadevice.*
import org.springframework.stereotype.Service

@Service
class DevicePersistenceService(
    private val deviceRepository: DeviceRepository,
    private val deviceGroupRepository: DeviceGroupRepository,
    private val deviceGroupMembershipRepository: DeviceGroupMembershipRepository,
    private val deviceMappingService: DeviceMappingService,
    private val membershipRetryBuffer: MembershipRetryBuffer
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
        val mapped = deviceMappingService.mapKafkaDevice(kafka, existing)
        deviceRepository.save(mapped)
    }

    private fun handleDeviceGroup(kafka: KafkaDeviceGroup) {
        val existing = deviceGroupRepository.findBySourceId(kafka.systemId)
        val mapped = deviceMappingService.mapKafkaDeviceGroup(kafka, existing)
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

        val mapped = deviceMappingService.mapKafkaDeviceGroupMembership(
            kafkaDeviceGroupMembership = kafkaDeviceGroupMembership,
            device = device,
            group = group,
            existing = existing
        )

        deviceGroupMembershipRepository.save(mapped)
    }
}