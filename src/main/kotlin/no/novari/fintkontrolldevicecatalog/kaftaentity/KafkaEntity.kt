package no.novari.fintkontrolldevicecatalog.kaftaentity

sealed interface KafkaEntity

data class KafkaDevice(
    val sourceId: String,
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
    val sourceId: String,
    val name: String,
    val orgUnitId: String?,
    val orgUnitName: String?,
    val platform: String,
    val deviceType: String,
) : KafkaEntity

data class KafkaDeviceGroupMembership(
    val sourceId: String,
    val deviceGroupId: String,
    val deviceId: String
) : KafkaEntity

