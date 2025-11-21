package no.novari.fintkontrolldevicecatalog.entity

import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDeviceGroupMembership
import org.springframework.stereotype.Component
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class MembershipRetryBuffer {
    private val buffer: Queue<KafkaDeviceGroupMembership> = ConcurrentLinkedQueue()

    fun add(membership: KafkaDeviceGroupMembership) {
        println("Buffered membership for retry: ${membership.deviceId}_${membership.groupId}")
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