package no.novari.fintkontrolldevicecatalog.service

import no.novari.fintkontrolldevicecatalog.entity.*
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupMembershipPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDevice
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDeviceGroup
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaEntity
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroup
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger("EntityPersistenceService")

@Service
class EntityPersistenceService(
    private val deviceRepository: DeviceRepository,
    private val deviceGroupRepository: DeviceGroupRepository,
    private val deviceGroupMembershipRepository: DeviceGroupMembershipRepository,
    private val entityMappingService: EntityMappingService,
    private val deviceGroupMembershipRetryBuffer: DeviceGroupMembershipRetryBuffer,
    private val kontrollEntityMappingService: KontrollEntityMappingService,
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

    private fun handleDevice(kafkaDevice: KafkaDevice) {
        val existingDevice = deviceRepository.findBySourceId(kafkaDevice.systemId)
        val mappedDevice = entityMappingService.mapKafkaDeviceToDevice(kafkaDevice, existingDevice)
        val savedDevice: Device = deviceRepository.save(mappedDevice)
        val kontrollDevice: KontrollDevice = kontrollEntityMappingService.mapDeviceToKontrollDevice(savedDevice)
        kontrollDevicePublishingComponent.publishOne(kontrollDevice)
    }

    private fun handleDeviceGroup(kafka: KafkaDeviceGroup) {
        val existing = deviceGroupRepository.findBySourceId(kafka.systemId)
        val mapped = entityMappingService.mapKafkaDeviceGroupToDeviceGroup(kafka, existing)
        val savedDeviceGroup: DeviceGroup = deviceGroupRepository.save(mapped)
        publishDeviceGroup(savedDeviceGroup)
    }

    private fun publishDeviceGroup(deviceGroup: DeviceGroup) {
        val kontrollDeviceGroup: KontrollDeviceGroup =
            kontrollEntityMappingService.mapDeviceGroupToKontrollDeviceGroup(deviceGroup)
        deviceGroupRepository.syncNoOfMembers(kontrollDeviceGroup.id)
        kontrollDeviceGroupPublishingComponent.publishOne(kontrollDeviceGroup)
    }

    private fun handleDeviceGroupMembership(kafkaDeviceGroupMembership: KafkaDeviceGroupMembership) {
        logger.debug("Handling membership for ${kafkaDeviceGroupMembership.deviceId}_${kafkaDeviceGroupMembership.groupId}")
        val group = deviceGroupRepository.findBySourceId(kafkaDeviceGroupMembership.groupId)
        val device = deviceRepository.findBySourceId(kafkaDeviceGroupMembership.deviceId)

        if (group == null || device == null) {
            deviceGroupMembershipRetryBuffer.add(kafkaDeviceGroupMembership)
            return
        }

        val id = DeviceGroupMembershipId(
            deviceGroupId = group.id!!,
            deviceId = device.id!!
        )

        val existing = deviceGroupMembershipRepository.findById(id).orElse(null)

        val mapped = entityMappingService.mapKafkaDeviceGroupMembershipToDeviceGroupMembership(
            kafkaDeviceGroupMembership = kafkaDeviceGroupMembership,
            device = device,
            group = group,
            existing = existing
        )

        val savedDeviceGroupMembership = deviceGroupMembershipRepository.save(mapped)
        val kontrollDeviceGroupMembership: KontrollDeviceGroupMembership =
            kontrollEntityMappingService.mapDeviceGroupMembershipToKontrollDeviceGroupMembership(
                savedDeviceGroupMembership
            )
        kontrollDeviceGroupMembershipPublishingComponent.publishOne(kontrollDeviceGroupMembership)
        publishDeviceGroup(group)
    }
}