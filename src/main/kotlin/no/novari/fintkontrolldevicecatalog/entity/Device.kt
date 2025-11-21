package no.novari.fintkontrolldevicecatalog.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "device")
data class Device (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val sourceId: String,
    val serialNumber: String,
    val dataObjectId: String?,
    val name: String?,
    val isPrivateProperty: Boolean?,
    val isShared: Boolean?,
    val status: String?,
    val statusChanged: Date?,
    val deviceType: String,
    val platform: String,
    val administratorOrgUnitId: String?,
    val ownerOrgUnitId: String?,

    @JsonIgnore
    @Transient
    @OneToMany(mappedBy = "device", cascade = [CascadeType.MERGE], fetch = FetchType.LAZY)
    val memberships: MutableSet<DeviceGroupMembership>? = mutableSetOf()
)