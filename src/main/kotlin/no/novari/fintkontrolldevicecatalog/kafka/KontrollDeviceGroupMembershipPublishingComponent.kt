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
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDeviceGroupMembership
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration


private val logger = LoggerFactory.getLogger(KontrollDeviceGroupMembershipPublishingComponent::class.java)

@Component
class KontrollDeviceGroupMembershipPublishingComponent(
    private val deviceCache: FintCache<String, KontrollDeviceGroupMembership>,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    private val entityTopicService: EntityTopicService
) {
    private val parameterizedTemplate: ParameterizedTemplate<KontrollDeviceGroupMembership> =
        parameterizedTemplateFactory.createTemplate(KontrollDeviceGroupMembership::class.java)

    private val entityTopicNameParameters : EntityTopicNameParameters = EntityTopicNameParameters.builder()
        .resourceName("kontroll-device-group-membership")
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


    @PostConstruct
    fun initTopic() {
        entityTopicService.createOrModifyTopic(
            entityTopicNameParameters,
            entityTopicConfiguration()

        )
    }

    fun publishOne(kontrollDeviceGroupMembership: KontrollDeviceGroupMembership) {
        val produserRecord = ParameterizedProducerRecord.builder<KontrollDeviceGroupMembership>()
            .topicNameParameters(entityTopicNameParameters)
            .key("${kontrollDeviceGroupMembership.deviceGroupId}_${kontrollDeviceGroupMembership.deviceGroupId}")
            .value(kontrollDeviceGroupMembership)
            .build()

        parameterizedTemplate.send(produserRecord)
        logger.info("Published kontrolldevice with sourceId: ${kontrollDeviceGroupMembership.deviceGroupId}_${kontrollDeviceGroupMembership.deviceGroupId}")
    }


}