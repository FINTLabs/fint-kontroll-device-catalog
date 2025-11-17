package no.novari.fintkontrolldevicecatalog.device

import jakarta.persistence.*

@Entity
@Table(name = "device_groups")
data class DeviceGroup (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val sourceId: String,
    val name: String,
    val orgUnitId: String?,
    val platform: String,
    val deviceType: String,

    @OneToMany(mappedBy = "deviceGroup", cascade = [CascadeType.MERGE], fetch = FetchType.LAZY)
    val memberships: MutableSet<DeviceGroupMembership> = mutableSetOf()
)