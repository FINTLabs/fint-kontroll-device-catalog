package no.novari.fintkontrolldevicecatalog.kontrollentity

import no.novari.fintkontrolldevicecatalog.entity.Device
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroup
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupMembershipRepository
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupRepository
import no.novari.fintkontrolldevicecatalog.entity.DeviceRepository
import no.novari.fintkontrolldevicecatalog.service.CacheService
import no.novari.fintkontrolldevicecatalog.service.KontrollEntityMappingService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class KontrollEntityService(
    private val cacheService: CacheService,
    private val deviceRepository: DeviceRepository,
    private val deviceGroupRepository: DeviceGroupRepository,
    private val deviceGroupMembershipRepository: DeviceGroupMembershipRepository,
    private val kontrollEntityMappingService: KontrollEntityMappingService,
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
            kontrollEntityMappingService.mapDeviceGroupToKontrollDeviceGroup(deviceGroup)
        }.toList()
    }

    fun findDeviceGroupByID(id: Long): KontrollDeviceGroup? {
        val deviceGroup = deviceGroupRepository.findByIdOrNull(id)
        return if (deviceGroup != null) {
            kontrollEntityMappingService.mapDeviceGroupToKontrollDeviceGroup(deviceGroup)
        } else {
            null
        }
    }

    fun findAllDevices(): List<KontrollDevice> {
        val devices = deviceRepository.findAll()

        return devices.mapNotNull { device -> kontrollEntityMappingService.mapDeviceToKontrollDevice(device)
        }.toList()
    }

    fun findDeviceById(id: Long): KontrollDevice? {
        val device = deviceRepository.findByIdOrNull(id)

        return device?.let { kontrollEntityMappingService.mapDeviceToKontrollDevice(it) }
    }

    fun findDevicesInDeviceGroupByDeviceGroupId(id: Long): List<KontrollDevice> {
        val devices: List<Device> = deviceGroupMembershipRepository.getDevicesInDeviceGroupByDeviceGroupId(id)

        return devices.map { device ->
            kontrollEntityMappingService.mapDeviceToKontrollDevice(device) }
    }

    fun findAllMemberships(): List<KontrollDeviceGroupMembership> {
        val deviceGroupMemberships = deviceGroupMembershipRepository.findAll()

        return deviceGroupMemberships.mapNotNull { deviceGroupMembership ->
            kontrollEntityMappingService.mapDeviceGroupMembershipToKontrollDeviceGroupMembership(deviceGroupMembership)
        }.toList()
    }

    fun findAllGroupsPaged(pageRequest: Pageable): Page<DeviceGroup> {

        return deviceGroupRepository.findAll(pageRequest)
    }


}
