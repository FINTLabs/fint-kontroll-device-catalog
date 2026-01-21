package no.novari.fintkontrolldevicecatalog.kafka

import jakarta.annotation.PostConstruct
import no.novari.cache.FintCache
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import no.novari.kafka.producing.ParameterizedProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration


private val logger = LoggerFactory.getLogger(KontrollDeviceGroupMembershipPublishingComponent::class.java)

@Component
class KontrollDeviceGroupMembershipPublishingComponent(
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService
) {
    private val parameterizedTemplate: ParameterizedTemplate<KontrollDeviceGroupMembership> =
        parameterizedTemplateFactory.createTemplate(KontrollDeviceGroupMembership::class.java)

    private val entityTopicNameParameters: EntityTopicNameParameters = EntityTopicNameParameters.builder()
        .resourceName("kontroll-device-group-membership")
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

    fun publishAll(deviceGroupMembership: List<KontrollDeviceGroupMembership>) {
        deviceGroupMembership.forEach {
            //TODO: add logic for status or put it in publishOne
            publishOne(it)
        }

    }

    fun publishOne(kontrollDeviceGroupMembership: KontrollDeviceGroupMembership) {
        val produserRecord = ParameterizedProducerRecord.builder<KontrollDeviceGroupMembership>()
            .topicNameParameters(entityTopicNameParameters)
            .key("${kontrollDeviceGroupMembership.deviceId}_${kontrollDeviceGroupMembership.deviceGroupId}")
            .value(kontrollDeviceGroupMembership)
            .build()

        parameterizedTemplate.send(produserRecord)
        logger.info("Published kontrolldevicemembership with sourceId: ${kontrollDeviceGroupMembership.deviceId}_${kontrollDeviceGroupMembership.deviceGroupId}")
    }




}