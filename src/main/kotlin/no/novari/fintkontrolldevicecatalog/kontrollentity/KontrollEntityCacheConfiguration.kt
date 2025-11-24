package no.novari.fintkontrolldevicecatalog.kontrollentity

import no.fintlabs.cache.FintCache
import no.fintlabs.cache.FintCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Locale
import kotlin.jvm.java
import kotlin.reflect.KClass
import kotlin.to

@Configuration
class KontrollEntityCacheConfiguration(
    private val fintCacheManager: FintCacheManager
) {
    @Bean
    fun kontrollDeviceCache(): FintCache<String, KontrollDevice> {
        return createCache(KontrollDevice::class)
    }

    @Bean
    fun kontrollDeviceGroupCache(): FintCache<String, KontrollDeviceGroup> {
        return createCache(KontrollDeviceGroup::class)
    }

    @Bean
    fun kontrollDeviceGroupMembershipCache(): FintCache<String, KontrollDeviceGroupMembership> {
        return createCache(KontrollDeviceGroupMembership::class)
    }


    @Bean
    fun cacheMap(
        kontrollDeviceCache: FintCache<String, KontrollDevice>,
        kontrollDeviceGroupCache: FintCache<String, KontrollDeviceGroup>,
        kontrollDeviceGroupMembershipCache: FintCache<String, KontrollDeviceGroupMembership>,
    ): Map<KClass<*>, FintCache<String, *>> =
        mapOf(
            KontrollDevice::class to kontrollDeviceCache,
            KontrollDeviceGroup::class to kontrollDeviceGroupCache,
            KontrollDeviceGroupMembership::class to kontrollDeviceGroupMembershipCache
        )

    private fun <V : Any> createCache(resourceClass: KClass<V>): FintCache<String, V> =
        fintCacheManager.createCache(
            resourceClass.simpleName?.lowercase(Locale.getDefault()) ?: "unknown",
            String::class.java,
            resourceClass.java
        )
}