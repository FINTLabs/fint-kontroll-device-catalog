package no.novari.fintkontrolldevicecatalog.kafka

import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDevice
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDeviceGroup
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaDeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.kaftaentity.KafkaEntity
import no.novari.fintkontrolldevicecatalog.service.EntityPersistenceService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import kotlin.reflect.KClass

@Configuration
class DeviceConsumerConfiguration(
    private val entityPersistenceService: EntityPersistenceService,
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService,
    private val kafkaCommonErrorHandler: CommonErrorHandler
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
                entityPersistenceService.handle(consumerRecord.value())

            },
            listenerConfiguration(),
            kafkaCommonErrorHandler

        )

        return factory.createContainer(nameParameters).apply { isAutoStartup = true }
    }

    private fun entityTopicNameParameters(topicName: String) =
        EntityTopicNameParameters.builder()
            .resourceName(topicName)
            .topicNamePrefixParameters(topicParams())
            .build()

    private fun topicParams() = TopicNamePrefixParameters
        .stepBuilder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()


    private fun listenerConfiguration() = ListenerConfiguration.builder()
        .seekingOffsetResetOnAssignment(false)
        .maxPollRecords(10)
        .build()

}