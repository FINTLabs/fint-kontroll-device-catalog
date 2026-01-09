package no.novari.fintkontrolldevicecatalog.kontrollentity


import no.fintlabs.util.OnlyDevelopers
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupMembershipPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDeviceGroupPublishingComponent
import no.novari.fintkontrolldevicecatalog.kafka.KontrollDevicePublishingComponent
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.Int
import kotlin.collections.drop
import kotlin.collections.take


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
    fun getKontrollDeviceGroups(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam (defaultValue = "20") size: Int,
    ): ResponseEntity<Map<String, Any?>> {
        val allGroups: List<KontrollDeviceGroup> = kontrollEntityService.findAllGroups()

        return pageResponse(toPage(allGroups, PageRequest.of(
            page,size)), itemKey = "deviceGroups")
    }

    @GetMapping("/devicegroups/{id}")
    fun getDeviceGroupByID(@PathVariable id: Long): ResponseEntity<KontrollDeviceGroup> =
        kontrollEntityService.findDeviceGroupByID(id).toResponseEntity()

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
        kontrollEntityService.findDeviceById(id).toResponseEntity()

    @GetMapping("/devicegroups/{id}/members")
    fun getDeviceGroupMembershipsByDeviceGroupId(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Map<String, Any?>> {
        val allMembers: List<KontrollDevice> = kontrollEntityService.findDevicesInDeviceGroupByDeviceGroupId(id)

        return pageResponse(toPage(allMembers, PageRequest.of(page, size)), itemKey = "members")
    }


    private fun <T> toPage(response: List<T>, paging: Pageable): Page<T> =
        PageImpl(response.drop(paging.offset.toInt()).take(paging.pageSize),
            paging,response.size.toLong())

    private fun <T> pageResponse(
        page: Page<T>,
        itemKey: String
    ) : ResponseEntity<Map<String, Any?>> =
        ResponseEntity.ok(
            mapOf(
                itemKey to page.content,
                "currentPage" to page.number,
                "totalPages" to page.totalPages,
                "totalItems" to page.totalElements,
                "itemsInPage" to page.numberOfElements
            )
        )

    private fun <T : Any> T?.toResponseEntity(): ResponseEntity<T> =
        this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()


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