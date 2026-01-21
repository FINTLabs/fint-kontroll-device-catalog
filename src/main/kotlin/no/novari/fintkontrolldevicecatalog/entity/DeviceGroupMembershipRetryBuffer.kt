package no.novari.fintkontrolldevicecatalog.entity

import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDeviceGroupMembership
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = LoggerFactory.getLogger("DeviceGroupMembershipRetryBuffer")

@Component
class DeviceGroupMembershipRetryBuffer {
    private val buffer: Queue<KafkaDeviceGroupMembership> = ConcurrentLinkedQueue()

    fun add(membership: KafkaDeviceGroupMembership) {
        logger.info("Buffered membership for retry: ${membership.deviceId}_${membership.deviceGroupId}")
        buffer.add(membership)
    }

    fun drain(): List<KafkaDeviceGroupMembership> {
        val drained = mutableListOf<KafkaDeviceGroupMembership>()
        while (true) {
            val item = buffer.poll() ?: break
            drained.add(item)
        }
        return drained
    }

    fun size(): Int = buffer.size

}