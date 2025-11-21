package no.novari.fintkontrolldevicecatalog.kafka

import no.fintlabs.kafka.consuming.ListenerConfiguration
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroup
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceService
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
    private val kontrollDeviceService: KontrollDeviceService,
    private val kafkaCommonErrorHandler: CommonErrorHandler
) {
    @Bean
    fun kontrollDeviceConsumer() = createListener("kontrolldevice", KontrollDevice::class)

    @Bean
    fun kontrollDeviceGroupConsumer() = createListener("kontrolldevicegroup", KontrollDeviceGroup::class)

    @Bean
    fun kontrollDeviceGroupMembershipConsumer() = createListener("kontrolldevicegroupmembership",
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
                kontrollDeviceService.saveToCache(consumerRecord.value())

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

    private fun topicParams() = TopicNamePrefixParameters.builder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()


    private fun listenerConfiguration() = ListenerConfiguration.builder()
        .seekingOffsetResetOnAssignment(false)
        .maxPollRecords(10)
        .build()

}
