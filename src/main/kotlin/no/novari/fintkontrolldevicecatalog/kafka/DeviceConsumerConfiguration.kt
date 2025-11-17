package no.novari.fintkontrolldevicecatalog.kafka

import no.fintlabs.kafka.consuming.ListenerConfiguration
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kaftadevice.KafkaDevice
import no.novari.fintkontrolldevicecatalog.kaftadevice.KafkaDeviceGroup
import no.novari.fintkontrolldevicecatalog.kaftadevice.KafkaDeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.kaftadevice.KafkaEntity
import no.novari.fintkontrolldevicecatalog.service.DevicePersistenceService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.DefaultErrorHandler
import kotlin.reflect.KClass

@Configuration
class DeviceConsumerConfiguration(
    private val devicePersistenceService: DevicePersistenceService,
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService,
) {

    @Bean
    fun deviceConsumer() = createListener("device", KafkaDevice::class)

    @Bean
    fun deviceGroupConsumer() = createListener("device-group", KafkaDeviceGroup::class)

    @Bean
    fun deviceGroupMembershipConsumer() = createListener("device-group-membership", KafkaDeviceGroupMembership::class)


    private fun <T : KafkaEntity> createListener(
        topicName: String,
        mappedClass: KClass<T>
    )
            : ConcurrentMessageListenerContainer<String, T> {
        val nameParameters = entityTopicNameParameters(topicName)

        val factory = parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
            mappedClass.java,
            { consumerRecord: ConsumerRecord<String, T> ->
                devicePersistenceService.handle(consumerRecord.value())

            },
            listenerConfiguration(),
            kafkaCommonErrorHandler()

        )

        return factory.createContainer(nameParameters).apply { isAutoStartup = true }
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