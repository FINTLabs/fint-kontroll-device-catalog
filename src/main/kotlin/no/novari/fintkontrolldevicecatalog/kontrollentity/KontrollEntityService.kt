package no.novari.fintkontrolldevicecatalog.kontrollentity

import no.fintlabs.opa.AuthorizationClient
import no.novari.fintkontrolldevicecatalog.OrgUnitType
import no.novari.fintkontrolldevicecatalog.entity.Device
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroup
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupMembershipRepository
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupRepository
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroupSpecification
import no.novari.fintkontrolldevicecatalog.entity.DeviceRepository
import no.novari.fintkontrolldevicecatalog.service.CacheService
import no.novari.fintkontrolldevicecatalog.service.KontrollEntityMappingService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class KontrollEntityService(
    private val authorizationClient: AuthorizationClient,
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

        return deviceGroups
            .mapNotNull { deviceGroup ->
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

        return devices
            .mapNotNull { device ->
                kontrollEntityMappingService.mapDeviceToKontrollDevice(device)
            }.toList()
    }

    fun findDeviceById(id: Long): KontrollDevice? {
        val device = deviceRepository.findByIdOrNull(id)

        return device?.let { kontrollEntityMappingService.mapDeviceToKontrollDevice(it) }
    }

    fun findAllMemberships(): List<KontrollDeviceGroupMembership> {
        val deviceGroupMemberships = deviceGroupMembershipRepository.findAll()

        return deviceGroupMemberships
            .mapNotNull { deviceGroupMembership ->
                kontrollEntityMappingService.mapDeviceGroupMembershipToKontrollDeviceGroupMembership(deviceGroupMembership)
            }.toList()
    }

    fun findAllGroupsPaged(pageRequest: Pageable): Page<KontrollDeviceGroup> {
        val deviceGroupsPaged = deviceGroupRepository.findAll(pageRequest)

        return deviceGroupsPaged.map(kontrollEntityMappingService::mapDeviceGroupToKontrollDeviceGroup)
    }

    fun findAllDevicesPaged(pageRequest: Pageable): Page<KontrollDevice> {
        val allDevices = deviceRepository.findAll(pageRequest)

        return allDevices.map { kontrollEntityMappingService.mapDeviceToKontrollDevice(it) }
    }

    fun findDevicesInDeviceGroupByDeviceGroupId(
        id: Long,
        pageRequest: Pageable,
    ): Page<KontrollDevice> {
        val devicesPage: Page<Device> = deviceGroupMembershipRepository.getDevicesInDeviceGroupByDeviceGroupIdPaged(id, pageRequest)

        return devicesPage.map(kontrollEntityMappingService::mapDeviceToKontrollDevice)
    }

    fun getOrgUnitsInScope(): List<String> {
        val deviceTypeScope: List<String> =
            authorizationClient.userScopesList
                .filter { s -> s.objectType.equals("device") }
                .flatMap { it.orgUnits }
        logger.info("OrgUnits in Device scope: $deviceTypeScope")

        return deviceTypeScope
    }

    fun findAllValidKontrollDeviceGroupsPaged(
        pageRequest: Pageable,
        search: String?,
        orgUnits: List<String>?,
        platform: String?,
    ): Page<KontrollDeviceGroup> {
        val orgUnitsInScope: List<String> = getOrgUnitsInScope()
        var requestedOrgUnitsAndInScope: List<String>? =
            orgUnits?.filter { it in orgUnitsInScope } ?: orgUnitsInScope
        logger.info("OrgUnits requested and in scope: $requestedOrgUnitsAndInScope")

        if (orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name)) {
            logger.info("Has access to all orgUnits")
            requestedOrgUnitsAndInScope = orgUnits
        }

        if (requestedOrgUnitsAndInScope.isNullOrEmpty() and !orgUnits.isNullOrEmpty()) {
            logger.info("Non of the requested orgUnits in scope")
            return Page.empty()
        }
        val deviceGroupSpecification: Specification<DeviceGroup> =
            Specification.allOf(
                DeviceGroupSpecification.hasNameLike(search),
                DeviceGroupSpecification.plattformIs(platform),
                DeviceGroupSpecification.deviceInOrgUnitValidForUser(requestedOrgUnitsAndInScope),
            )

        val allDeviceGroups = deviceGroupRepository.findAll(deviceGroupSpecification, pageRequest)
        return allDeviceGroups.map(kontrollEntityMappingService::mapDeviceGroupToKontrollDeviceGroup)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KontrollEntityService::class.java)
    }
}
