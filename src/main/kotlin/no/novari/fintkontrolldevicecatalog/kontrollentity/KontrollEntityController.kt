package no.novari.fintkontrolldevicecatalog.kontrollentity

import no.fintlabs.opa.AuthorizationClient
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroup
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(KontrollEntityController::class.java)

@RestController
@RequestMapping("/api")
class KontrollEntityController(
    val kontrollEntityService: KontrollEntityService,
) {

    @GetMapping("/devicegroups")
    fun getKontrollDeviceGroups(): List<KontrollDeviceGroup> = kontrollEntityService.findAllGroups()



    @GetMapping("/devicegroups/{id}")
    fun getDeviceGroupByID(@PathVariable id: Long): ResponseEntity<KontrollDeviceGroup> =
        kontrollEntityService.findDeviceGroupByID(id).toResponseEntity()

    @GetMapping("/devices")
    fun getKontrollDevices() = kontrollEntityService.findAllDevices()

    @GetMapping("/devices/{id}")
    fun getDeviceById(@PathVariable id: Long): ResponseEntity<KontrollDevice> =
        kontrollEntityService.findDeviceById(id).toResponseEntity()

    @GetMapping("/devicegroups/{id}/members")
    fun getDeviceGroupMembershipsByDeviceGroupId(@PathVariable id: Long): List<KontrollDevice>  =
        kontrollEntityService.findDevicesInDeviceGroupByDeviceGroupId(id)






    private fun KontrollDevice?.toResponseEntity(): ResponseEntity<KontrollDevice> =
        this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()


    private fun KontrollDeviceGroup?.toResponseEntity(): ResponseEntity<KontrollDeviceGroup> =
        this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    private fun KontrollDeviceGroupMembership?.toResponseEntity(): ResponseEntity<KontrollDeviceGroupMembership> =
        this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

}