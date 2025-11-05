package no.novari.fintkontrolldevicecatalog.device

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table


@Entity
@Table(name = "device_groups")
data class DeviceGroup (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    val sourceId: String,
    val name: String,
    val orgUnitId: String?,
    val platform: String,
    val deviceType: String,

    @OneToMany(mappedBy = "device_group", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    val memberships: MutableSet<DeviceGroupMembership>

)