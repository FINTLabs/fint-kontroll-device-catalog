package no.novari.fintkontrolldevicecatalog.kafka

import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroup
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import no.novari.kafka.producing.ParameterizedProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = LoggerFactory.getLogger(KontrollDeviceGroupPublishingComponent::class.java)

@Component
class KontrollDeviceGroupPublishingComponent(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService
) {
    private val parameterizedTemplate: ParameterizedTemplate<KontrollDeviceGroup> =
        parameterizedTemplateFactory.createTemplate(KontrollDeviceGroup::class.java)

    private val entityTopicNameParameters : EntityTopicNameParameters = EntityTopicNameParameters.builder()
        .resourceName("kontroll-device-group")
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


    init {
        entityTopicService.createOrModifyTopic(
            entityTopicNameParameters,
            entityTopicConfiguration()

        )
    }

    fun publishAll(devicegroups: List<KontrollDeviceGroup>) {
        devicegroups.forEach {
            //TODO: all logic for status or put it in publishOne
            publishOne(it)
        }
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