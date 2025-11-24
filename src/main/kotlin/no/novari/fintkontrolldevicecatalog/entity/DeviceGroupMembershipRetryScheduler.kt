package no.novari.fintkontrolldevicecatalog.entity

import no.novari.fintkontrolldevicecatalog.service.DevicePersistenceService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DeviceGroupMembershipRetryScheduler(
    private val deviceGroupMembershipRetryBuffer: DeviceGroupMembershipRetryBuffer,
    private val devicePersistenceService: DevicePersistenceService
) {

    @Scheduled(fixedDelay = 10000)
    fun retryBufferedMemberships() {
        val membershipsToRetry = deviceGroupMembershipRetryBuffer.drain()

        if (membershipsToRetry.isNotEmpty()) {
            println("Retrying ${membershipsToRetry.size} buffered memberships...")
        }

        membershipsToRetry.forEach { membership ->
            try {
                devicePersistenceService.handle(membership)
            } catch (e: Exception) {
                println("Retry failed for ${membership.deviceId}_${membership.groupId}: ${e.message}")
                deviceGroupMembershipRetryBuffer.add(membership)
            }
        }
    }
}