package no.novari.fintkontrolldevicecatalog.kontrollentity

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity

object KontrollEntityControllerResponse {

    fun <T> toPage(list: List<T>, paging: Pageable): Page<T> =
        PageImpl(
            list.drop(paging.offset.toInt()).take(paging.pageSize),
            paging,
            list.size.toLong()
        )

    fun <T> pageResponse(
        page: Page<T>,
        itemKey: String
    ): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.ok(
            mapOf(
                itemKey to page.content,
                "currentPage" to page.number,
                "totalPages" to page.totalPages,
                "totalItems" to page.totalElements,
                "itemsInPage" to page.numberOfElements
            )
        )

    fun <T : Any> toResponseEntity(entity: T?): ResponseEntity<T> =
        entity?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}