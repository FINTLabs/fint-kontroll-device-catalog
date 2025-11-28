package no.novari.fintkontrolldevicecatalog.kafka

import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroup
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollEntityService
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollEntity
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import kotlin.String
import kotlin.reflect.KClass


@Configuration
class KontrollDeviceConsumerConfiguration(
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService,
    private val kontrollEntityService: KontrollEntityService,
    private val kafkaCommonErrorHandler: CommonErrorHandler
) {
    @Bean
    fun kontrollDeviceConsumer() = createListener("kontroll-device", KontrollDevice::class)

    @Bean
    fun kontrollDeviceGroupConsumer() = createListener("kontroll-device-group", KontrollDeviceGroup::class)

    @Bean
    fun kontrollDeviceGroupMembershipConsumer() = createListener("kontroll-device-group-membership",
        KontrollDeviceGroupMembership::class
    )



    private fun <T : KontrollEntity> createListener(
        topicName: String,
        mappedClass: KClass<T>
    )
            : ConcurrentMessageListenerContainer<String, T> {
        val nameParameters = entityTopicNameParameters(topicName)

        val factory = parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
            mappedClass.java,
            { consumerRecord: ConsumerRecord<String, T> ->
                kontrollEntityService.saveToCache(consumerRecord.value())

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
