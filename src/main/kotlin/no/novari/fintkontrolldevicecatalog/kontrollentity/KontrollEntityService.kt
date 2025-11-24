package no.novari.fintkontrolldevicecatalog.kontrollentity

import no.novari.fintkontrolldevicecatalog.entity.DeviceGroup
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupRepository
import no.novari.fintkontrolldevicecatalog.service.CacheService
import no.novari.fintkontrolldevicecatalog.service.KontrollDeviceMappingService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class KontrollEntityService(
    private val cacheService: CacheService,
    private val deviceGroupRepository: DeviceGroupRepository,
    private val kontrollDeviceMappingService: KontrollDeviceMappingService,
) {
    fun <T : KontrollEntity> saveToCache(entity: T) {

        when (entity) {
            is KontrollDevice -> saveKontrollDeviceToCache(entity)
            is KontrollDeviceGroup -> saveKontrollDeviceGroupToCache(entity)
            is KontrollDeviceGroupMembership -> saveKontrollDeviceGroupMembershipToCache(entity)
        }
    }

    private fun saveKontrollDeviceToCache(entity: KontrollDevice) {
        cacheService.put(entity.id.toString(), entity, KontrollDevice::class)
    }

    private fun saveKontrollDeviceGroupToCache(entity: KontrollDeviceGroup) {
        cacheService.put(entity.id.toString(), entity, KontrollDeviceGroup::class)
    }

    private fun saveKontrollDeviceGroupMembershipToCache(entity: KontrollDeviceGroupMembership) {
        cacheService.put("${entity.deviceGroupId}_${entity.deviceId}", entity, KontrollDeviceGroupMembership::class)
    }

    fun findAllGroups(): List<KontrollDeviceGroup> {
        val deviceGroups = deviceGroupRepository.findAll()

        return deviceGroups.mapNotNull { deviceGroup ->
            kontrollDeviceMappingService.mapDeviceGroupToKontrollDeviceGroup(deviceGroup)
        }.toList()
    }

    fun findDeviceGroupByID(id: Long): KontrollDeviceGroup? {
        val deviceGroup = deviceGroupRepository.findByIdOrNull(id)
        return if (deviceGroup != null) {
            kontrollDeviceMappingService.mapDeviceGroupToKontrollDeviceGroup(deviceGroup)
        } else {
            null
        }
    }
}
