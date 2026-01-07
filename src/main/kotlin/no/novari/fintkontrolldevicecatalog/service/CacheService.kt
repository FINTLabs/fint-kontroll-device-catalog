package no.novari.fintkontrolldevicecatalog.service

import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import no.novari.cache.FintCache


@Service
class CacheService(
    private val cacheMap: Map<KClass<*>, FintCache<String, *>>
) {
    fun <T : Any> put(key: String?, value: T, type: KClass<T>) {
        if (key == null) return

        val cache = cacheMap[type] as? FintCache<String, T>
            ?: throw IllegalArgumentException("No cache configured for type: ${type.simpleName}")

        cache.put(key, value)
    }
}