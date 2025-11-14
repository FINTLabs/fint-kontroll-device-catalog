package no.novari.fintkontrolldevicecatalog.kaftadevice

data class KafkaDeviceGroupMembership(
    val id: String,
    val groupId: String,
    val deviceId: String
)
