package no.novari.fintkontrolldevicecatalog.kaftaentity

sealed interface KafkaEntity

data class KafkaDevice(
    val systemId: String,
    val serialNumber: String,
    val dataObjectId: String?,
    val name: String?,
    val isPrivateProperty: Boolean?,
    val isShared: Boolean?,
    val status: String?,
    val deviceType: String,
    val platform: String,
    val administratorOrgUnitId: String?,
    val ownerOrgUnitId: String?,
) : KafkaEntity

data class KafkaDeviceGroup(
    val systemId: String,
    val name: String,
    val orgUnitId: String?,
    val platform: String,
    val deviceType: String,
) : KafkaEntity

data class KafkaDeviceGroupMembership(
    val groupId: String,
    val deviceId: String
) : KafkaEntity

