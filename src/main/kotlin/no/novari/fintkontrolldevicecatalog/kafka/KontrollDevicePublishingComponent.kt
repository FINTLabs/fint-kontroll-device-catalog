package no.novari.fintkontrolldevicecatalog.kafka

import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.kafka.producing.ParameterizedProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = LoggerFactory.getLogger(KontrollDevicePublishingComponent::class.java)

@Component
class KontrollDevicePublishingComponent(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService
) {
    private val parameterizedTemplate: ParameterizedTemplate<KontrollDevice> =
        parameterizedTemplateFactory.createTemplate(KontrollDevice::class.java)

    private val entityTopicNameParameters : EntityTopicNameParameters = EntityTopicNameParameters.builder()
        .resourceName("kontroll-device")
        .topicNamePrefixParameters(topicNameParameters())
        .build()


    private fun topicNameParameters() = TopicNamePrefixParameters
        .stepBuilder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()

    fun entityTopicConfiguration() = EntityTopicConfiguration
        .stepBuilder()
        .partitions(1)
        .lastValueRetentionTime(Duration.ofDays(10))
        .nullValueRetentionTime(Duration.ZERO)
        .cleanupFrequency(EntityCleanupFrequency.NORMAL)
        .build()


     init{
        entityTopicService.createOrModifyTopic(
            entityTopicNameParameters,
            entityTopicConfiguration()

        )
    }

    fun publishAll(devices: List<KontrollDevice>) {
        devices.forEach {
            //TODO: add logic for status or put in in publishOne
            publishOne(it)
        }
    }

    fun publishOne(kontrollDevice: KontrollDevice) {
        val produserRecord = ParameterizedProducerRecord.builder<KontrollDevice>()
            .topicNameParameters(entityTopicNameParameters)
            .key(kontrollDevice.sourceId)
            .value(kontrollDevice)
        .build()

        parameterizedTemplate.send(produserRecord)
        logger.info("Published kontrolldevice with sourceId: ${kontrollDevice.sourceId} and name: ${kontrollDevice.name}")
    }

}



