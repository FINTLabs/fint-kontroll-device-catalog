package no.novari.fintkontrolldevicecatalog.kontrollentity


import no.fintlabs.util.OnlyDevelopers
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupMembershipPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollEntityControllerResponse.pageResponse
import no.novari.fintkontrolldevicecatalog.kontrollentity.KontrollEntityControllerResponse.toPage
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.Int


private val logger = LoggerFactory.getLogger(KontrollEntityController::class.java)

@RestController
@RequestMapping("/api")
class KontrollEntityController(
    val kontrollEntityService: KontrollEntityService,
    val kontrollDeviceGroupPublishingComponent: KontrollDeviceGroupPublishingComponent,
    val kontrollDevicePublishingComponent: KontrollDevicePublishingComponent,
    val kontrollDeviceGroupMembershipPublishingComponent: KontrollDeviceGroupMembershipPublishingComponent
) {
    private val responseUtils = KontrollEntityControllerResponse


    @GetMapping("/devicegroups")
    fun getKontrollDeviceGroups(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam (defaultValue = "20") size: Int,
    ): ResponseEntity<Map<String, Any?>> {
        val allGroups: List<KontrollDeviceGroup> = kontrollEntityService.findAllGroups()

        return responseUtils.pageResponse(toPage(allGroups, PageRequest.of(
            page,size)), itemKey = "deviceGroups")
    }

    @GetMapping("/devicegroups/{id}")
    fun getDeviceGroupByID(@PathVariable id: Long): ResponseEntity<KontrollDeviceGroup> =
        responseUtils.toResponseEntity( kontrollEntityService.findDeviceGroupByID(id))


    @GetMapping("/devices")
    fun getKontrollDevices(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ) : ResponseEntity<Map<String, Any?>> {
        val allDevices: List<KontrollDevice> = kontrollEntityService.findAllDevices()

        return pageResponse(toPage(allDevices, PageRequest.of(page, size)), itemKey = "devices")
    }


    @GetMapping("/devices/{id}")
    fun getDeviceById(@PathVariable id: Long): ResponseEntity<KontrollDevice> =
        responseUtils.toResponseEntity(kontrollEntityService.findDeviceById(id))

    @GetMapping("/devicegroups/{id}/members")
    fun getDeviceGroupMembershipsByDeviceGroupId(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Map<String, Any?>> {
        val allMembers: List<KontrollDevice> = kontrollEntityService.findDevicesInDeviceGroupByDeviceGroupId(id)

        return pageResponse(toPage(allMembers, PageRequest.of(page, size)), itemKey = "members")
    }

    @OnlyDevelopers
    @PostMapping("/devicegroups/publishAllDeviceGroupsDevicesMembership")
    fun publishAll(): ResponseEntity<HttpStatus> {
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
}