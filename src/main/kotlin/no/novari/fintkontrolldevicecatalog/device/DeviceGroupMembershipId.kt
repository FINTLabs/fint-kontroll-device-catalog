package no.novari.fintkontrolldevicecatalog.device

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class DeviceGroupMembershipId(
    @Column(name = "device_group_id")
    var deviceGroupId: Long = 0,

    @Column(name = "device_id")
    var deviceId: Long = 0
) : Serializable