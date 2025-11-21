package no.novari.fintkontrolldevicecatalog.kafka

import no.fintlabs.cache.FintCache
import no.fintlabs.kafka.model.ParameterizedProducerRecord
import no.fintlabs.kafka.producing.ParameterizedTemplate
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory
import no.fintlabs.kafka.topic.EntityTopicService
import no.fintlabs.kafka.topic.configuration.EntityCleanupFrequency
import no.fintlabs.kafka.topic.configuration.EntityTopicConfiguration
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollDevice
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = LoggerFactory.getLogger(KontrollDevicePublishingComponent::class.java)

@Component
class KontrollDevicePublishingComponent(
    private val deviceCache: FintCache<String, KontrollDevice>,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService
) {
    private val parameterizedTemplate: ParameterizedTemplate<KontrollDevice> =
        parameterizedTemplateFactory.createTemplate(KontrollDevice::class.java)

    private val entityTopicNameParameters : EntityTopicNameParameters = EntityTopicNameParameters.builder()
        .resourceName("kontrolldevice")
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

    @Scheduled(
        fixedDelayString = "\${fint.kontroll.publishing.fixed-delay:PT5M}",
        initialDelayString = "\${fint.kontroll.publishing.initial-delay:PT5M}"
    )
    fun publishAll() {
        val allKontrollDevices = deviceCache.getAll() //TODO: move to separate service
        val allKontrollDevicesToPublish = allKontrollDevices
            .mapNotNull { kontrollDevice ->
                val key = kontrollDevice.sourceId
                val cachedKontrollDevice = deviceCache.getOptional(key).orElse(null)
                if (cachedKontrollDevice != null || cachedKontrollDevice != kontrollDevice) kontrollDevice else null
            }
            .toList()

        allKontrollDevicesToPublish.forEach { publishOne(it)}

        logger.info("All Kontroll devices published")
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



