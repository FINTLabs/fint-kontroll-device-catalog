package no.novari.fintkontrolldevicecatalog.entity

import no.novari.fintkontrolldevicecatalog.service.EntityPersistenceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger("DeviceGroupMembershipRetryScheduler")

@Component
class DeviceGroupMembershipRetryScheduler(
    private val deviceGroupMembershipRetryBuffer: DeviceGroupMembershipRetryBuffer,
    private val entityPersistenceService: EntityPersistenceService
) {

    @Scheduled(fixedDelay = 10000)
    fun retryBufferedMemberships() {
        val membershipsToRetry = deviceGroupMembershipRetryBuffer.drain()

        if (membershipsToRetry.isNotEmpty()) {
            logger.info("Retrying ${membershipsToRetry.size} buffered memberships...")
        }

        membershipsToRetry.forEach { membership ->
            try {
                entityPersistenceService.handle(membership)
            } catch (e: Exception) {
                logger.info("Retry failed for ${membership.deviceId}_${membership.groupId}: ${e.message}")
                deviceGroupMembershipRetryBuffer.add(membership)
            }
        }
    }
}