package no.novari.fintkontrolldevicecatalog.entity

import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "device_group_membership")
data class DeviceGroupMembership(

    @EmbeddedId
    var id: DeviceGroupMembershipId = DeviceGroupMembershipId(),

    @ManyToOne
    @MapsId("deviceGroupId")
    @JoinColumn(name = "device_group_id", nullable = false)
    val deviceGroup: DeviceGroup,

    @ManyToOne
    @MapsId("deviceId")
    @JoinColumn(name = "device_id", nullable = false)
    val device: Device,

    val membershipStatus: String? = null,
    val membershipStatusChanged: Date? = null
)