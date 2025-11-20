package no.novari.fintkontrolldevicecatalog.kontrolldevice

import java.util.Date

data class KontrollDeviceGroupMembership(
    val deviceGroupId: Long,
    val deviceId: Long,
    val membershipStatus: String,
    val membershipStatusChanged: Date
) {

}