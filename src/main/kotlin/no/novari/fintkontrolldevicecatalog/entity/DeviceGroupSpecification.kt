package no.novari.fintkontrolldevicecatalog.entity

import org.springframework.data.jpa.domain.Specification

object DeviceGroupSpecification {
    fun hasNameLike(search: String?): Specification<DeviceGroup> =
        Specification { root, _, criteriaBuilder ->
            if (search == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%${search.lowercase()}%",
                )
            }
        }

    fun deviceInOrgUnitValidForUser(orgUnitIds: List<String>?): Specification<DeviceGroup> =
        Specification { root, _, criteriaBuilder ->
            if (orgUnitIds.isNullOrEmpty()) {
                criteriaBuilder.conjunction()
            } else {
                root.get<String>("orgUnitId").`in`(orgUnitIds)
            }
        }

    fun plattformIs(platform: String?): Specification<DeviceGroup> =
        Specification { root, _, criteriaBuilder ->
            if (platform == null) {
                criteriaBuilder.conjunction()
            } else {
                criteriaBuilder.equal(root.get<String>("platform"), platform)
            }
        }
}
