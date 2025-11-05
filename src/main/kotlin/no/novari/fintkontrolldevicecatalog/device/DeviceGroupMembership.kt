package no.novari.fintkontrolldevicecatalog.device

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table

@Entity
@Table(name = "device_group_membership")
data class DeviceGroupMembership (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val groupId: String,
    val deviceId: String,

    @ManyToOne
    @MapsId("deviceGroupId")
    @JoinColumn(name = "device_group_id")
    val deviceGroup: DeviceGroup? = null,

    @ManyToOne
    @MapsId("deviceId")
    @JoinColumn(name = "device_id")
    val device: Device? = null
)