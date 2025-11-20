package no.novari.fintkontrolldevicecatalog.kontrolldevice

import java.util.Date

sealed interface KontrollEntity

data class KontrollDevice(
    val id: String,
    val sourceId: String,
    val serialNumber: String,
    val dataObjectId: String?,
    val name: String,
    val isPrivateProperty: Boolean?,
    val isShared: Boolean?,
    val status: String,
    val deviceType: String,
    val platform: String,
    val administratorOrgUnitId: String,
    val ownerOrgUnitId: String?,
) : KontrollEntity

data class KontrollDeviceGroup(
    val id: Long,
    val sourceId: String,
    val name: String,
    val orgUnitId: String?,
    val platform: String,
    val deviceType: String,
) : KontrollEntity

data class KontrollDeviceGroupMembership(
    val deviceGroupId: Long,
    val deviceId: Long,
    val membershipStatus: String,
    val membershipStatusChanged: Date
) : KontrollEntity

