package no.novari.fintkontrolldevicecatalog.kafka

import no.fintlabs.kafka.consuming.ListenerConfiguration
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.device.DeviceGroup
import no.novari.fintkontrolldevicecatalog.device.DeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.kaftadevice.KafkaDevice
import no.novari.fintkontrolldevicecatalog.service.CacheService
import no.novari.fintkontrolldevicecatalog.service.DeviceMappingService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.AbstractMessageListenerContainer
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.DefaultErrorHandler
import kotlin.reflect.KClass

@Configuration
class DeviceConsumerConfiguration(
    private val cacheService: CacheService,
    private val deviceMappingService: DeviceMappingService,
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService,
) {

    @Bean
    fun deviceConsumer() = createListener("device", KafkaDevice::class)

    @Bean
    fun deviceGroupConsumer() = createListener("device-group", DeviceGroup::class)

    @Bean
    fun deviceGroupMembershipConsumer() = createListener("device-group-membership", DeviceGroupMembership::class)



    private fun <T:Any>createListener(topicName: String,mappedClass: KClass<T>)
    : ConcurrentMessageListenerContainer<String, T> {
        val nameParameters = entityTopicNameParameters(topicName)

        val factory = parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
            mappedClass.java,
            {consumerRecord: ConsumerRecord<String, T> ->
                deviceMappingService.mapToKontrollDevice(consumerRecord.value() as KafkaDevice)
                //cacheService.put(consumerRecord.key(), consumerRecord.value(), type = mappedClass)
            },
            listenerConfiguration(),
            kafkaCommonErrorHandler()

        )

        return factory.createContainer(nameParameters).apply { isAutoStartup = true}
    }

    private fun entityTopicNameParameters(topicName: String) =
        EntityTopicNameParameters.builder()
            .resourceName(topicName)
            .topicNamePrefixParameters(topicParams())
            .build()

    private fun topicParams() = TopicNamePrefixParameters.builder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()


    private fun listenerConfiguration() = ListenerConfiguration.builder()
        .seekingOffsetResetOnAssignment(false)
        .maxPollRecords(10)
        .build()

    @Bean
    fun kafkaCommonErrorHandler(): CommonErrorHandler = DefaultErrorHandler()

}