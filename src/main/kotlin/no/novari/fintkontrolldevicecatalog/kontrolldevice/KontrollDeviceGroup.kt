package no.novari.fintkontrolldevicecatalog.kontrolldevice



data class KontrollDeviceGroup(
    val id: Long,
    val sourceId: String,
    val name: String,
    val orgUnitId: String?,
    val platform: String,
    val deviceType: String,
)