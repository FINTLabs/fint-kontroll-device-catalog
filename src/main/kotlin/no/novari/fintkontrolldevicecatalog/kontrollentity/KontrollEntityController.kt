package no.novari.fintkontrolldevicecatalog.kontrollentity


import no.fintlabs.util.OnlyDevelopers
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupMembershipPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(KontrollEntityController::class.java)

@RestController
@RequestMapping("/api")
class KontrollEntityController(
    val kontrollEntityService: KontrollEntityService,
    val kontrollDeviceGroupPublishingComponent: KontrollDeviceGroupPublishingComponent,
    val kontrollDevicePublishingComponent: KontrollDevicePublishingComponent,
    val kontrollDeviceGroupMembershipPublishingComponent: KontrollDeviceGroupMembershipPublishingComponent
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

    @OnlyDevelopers
    @PostMapping("/devicegroups/publishAllDeviceGroupsDevicesMembership")
    fun publishAll(): ResponseEntity<HttpStatus>{
        logger.info("Start publishing all devicegroups, devices and membership")
        val deviceGroups = kontrollEntityService.findAllGroups()
        logger.info("Publishing ${deviceGroups.size} devicegroups")
        kontrollDeviceGroupPublishingComponent.publishAll(deviceGroups)
        val devices = kontrollEntityService.findAllDevices()
        logger.info("Publishing ${devices.size} devices")
        kontrollDevicePublishingComponent.publishAll(devices)
        val deviceGroupMembership = kontrollEntityService.findAllMemberships()
        logger.info("Publishing ${deviceGroupMembership.size} devicegroupmembership")
        kontrollDeviceGroupMembershipPublishingComponent.publishAll(deviceGroupMembership)

        return ResponseEntity.ok().build()
    }


    private fun KontrollDevice?.toResponseEntity(): ResponseEntity<KontrollDevice> =
        this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()


    private fun KontrollDeviceGroup?.toResponseEntity(): ResponseEntity<KontrollDeviceGroup> =
        this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    private fun KontrollDeviceGroupMembership?.toResponseEntity(): ResponseEntity<KontrollDeviceGroupMembership> =
        this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

}