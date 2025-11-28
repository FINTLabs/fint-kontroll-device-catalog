package no.novari.fintkontrolldevicecatalog.kafka

import jakarta.annotation.PostConstruct
import no.fintlabs.cache.FintCache
import no.fintlabs.kafka.model.ParameterizedProducerRecord
import no.fintlabs.kafka.producing.ParameterizedTemplate
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory
import no.fintlabs.kafka.topic.EntityTopicService
import no.fintlabs.kafka.topic.configuration.EntityCleanupFrequency
import no.fintlabs.kafka.topic.configuration.EntityTopicConfiguration
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = LoggerFactory.getLogger(KontrollDeviceGroupPublishingComponent::class.java)

@Component
class KontrollDeviceGroupPublishingComponent(
    private val deviceCache: FintCache<String, KontrollDeviceGroup>,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    private val entityTopicService: EntityTopicService
) {
    private val parameterizedTemplate: ParameterizedTemplate<KontrollDeviceGroup> =
        parameterizedTemplateFactory.createTemplate(KontrollDeviceGroup::class.java)

    private val entityTopicNameParameters : EntityTopicNameParameters = EntityTopicNameParameters.builder()
        .resourceName("kontroll-device-group")
        .topicNamePrefixParameters(topicNameParameters())
        .build()


    private fun topicNameParameters() = TopicNamePrefixParameters.builder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()

    fun entityTopicConfiguration() = EntityTopicConfiguration.builder()
        .partitions(1)
        .lastValueRetentionTime(Duration.ofDays(10))
        .nullValueRetentionTime(Duration.ZERO)
        .cleanupFrequency(EntityCleanupFrequency.NORMAL)
        .build()


    init {
        entityTopicService.createOrModifyTopic(
            entityTopicNameParameters,
            entityTopicConfiguration()

        )
    }

    fun publishOne(kontrollDeviceGroup: KontrollDeviceGroup) {
        val produserRecord = ParameterizedProducerRecord.builder<KontrollDeviceGroup>()
            .topicNameParameters(entityTopicNameParameters)
            .key(kontrollDeviceGroup.sourceId)
            .value(kontrollDeviceGroup)
            .build()

        parameterizedTemplate.send(produserRecord)
        logger.info("Published kontrolldevicegroup with sourceId: ${kontrollDeviceGroup.sourceId} and name: ${kontrollDeviceGroup.name}")
    }


}