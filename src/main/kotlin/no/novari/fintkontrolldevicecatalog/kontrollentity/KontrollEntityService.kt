package no.novari.fintkontrolldevicecatalog.kontrollentity

import no.novari.fintkontrolldevicecatalog.service.CacheService
import org.springframework.stereotype.Service

@Service
class KontrollEntityService(
    private val cacheService: CacheService
) {
    fun <T : KontrollEntity>saveToCache(entity: T) {

        when (entity) {
            is KontrollDevice -> saveKontrollDeviceToCache(entity)
            is KontrollDeviceGroup -> saveKontrollDeviceGroupToCache(entity)
            is KontrollDeviceGroupMembership -> saveKontrollDeviceGroupMembershipToCache(entity)
        }
    }

    private fun saveKontrollDeviceToCache(entity: KontrollDevice) {
        cacheService.put(entity.id.toString(),entity, KontrollDevice::class)
    }

    private fun saveKontrollDeviceGroupToCache(entity: KontrollDeviceGroup) {
        cacheService.put(entity.id.toString(),entity, KontrollDeviceGroup::class)
    }

    private fun saveKontrollDeviceGroupMembershipToCache(entity: KontrollDeviceGroupMembership) {
        cacheService.put("${entity.deviceGroupId}_${entity.deviceId}",entity, KontrollDeviceGroupMembership::class)
    }


}
