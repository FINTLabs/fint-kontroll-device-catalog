package no.novari.fintkontrolldevicecatalog.kontrollentity

import no.novari.fintkontrolldevicecatalog.service.CacheService
import org.springframework.stereotype.Service

@Service
class KontrollDeviceService(
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
        TODO("Not yet implemented")
    }

    private fun saveKontrollDeviceGroupMembershipToCache(entity: KontrollDeviceGroupMembership) {
        TODO("Not yet implemented")
    }


}
