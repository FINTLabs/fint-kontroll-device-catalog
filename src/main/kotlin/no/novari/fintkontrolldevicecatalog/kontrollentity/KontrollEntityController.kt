package no.novari.fintkontrolldevicecatalog.kontrollentity


import no.fintlabs.util.OnlyDevelopers
import no.novari.fintkontrolldevicecatalog.entity.DeviceGroup
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupMembershipPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*



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
        @ParameterObject @PageableDefault(size = 20) pageRequest: Pageable
    ): ResponseEntity<Map<String, Any?>> {
        val allGroupsPaged : Page<KontrollDeviceGroup> = kontrollEntityService.findAllGroupsPaged(pageRequest)

        return responseUtils.pageResponse(allGroupsPaged, itemKey = "deviceGroups")
    }

    @GetMapping("/devicegroups/{id}")
    fun getDeviceGroupByID(@PathVariable id: Long): ResponseEntity<KontrollDeviceGroup> =
        responseUtils.toResponseEntity( kontrollEntityService.findDeviceGroupByID(id))


    @GetMapping("/devices")
    fun getKontrollDevices(
        @ParameterObject @PageableDefault(size = 20) pageRequest: Pageable
    ) : ResponseEntity<Map<String, Any?>> {
        val allDevicespaged: Page<KontrollDevice> = kontrollEntityService.findAllDevicesPaged(pageRequest)

        return responseUtils.pageResponse(allDevicespaged, itemKey = "devices")
    }


    @GetMapping("/devices/{id}")
    fun getDeviceById(@PathVariable id: Long): ResponseEntity<KontrollDevice> =
        responseUtils.toResponseEntity(kontrollEntityService.findDeviceById(id))


    @GetMapping("/devicegroups/{id}/members")
    fun getDeviceGroupMembershipsByDeviceGroupId(
        @PathVariable id: Long,
        @ParameterObject @PageableDefault(size = 20) pageRequest: Pageable
    ): ResponseEntity<Map<String, Any?>> {
        val allMembersPaged: Page<KontrollDevice> = kontrollEntityService.findDevicesInDeviceGroupByDeviceGroupId(id, pageRequest)

        return responseUtils.pageResponse(allMembersPaged, itemKey = "members")
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