package no.novari.fintkontrolldevicecatalog.kaftadevice

data class KafkaDeviceGroup(
    val systemId: String,
    val name: String,
    val orgUnitId: String?,
    val platform: String,
    val deviceType: String,
)
