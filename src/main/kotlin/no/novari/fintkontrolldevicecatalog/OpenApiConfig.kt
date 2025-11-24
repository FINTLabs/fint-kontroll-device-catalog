package no.novari.fintkontrolldevicecatalog

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

import io.swagger.v3.oas.annotations.info.License
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "Kontroll device-catalog",
        version = "0.0.1",
        description = "REST API for device-catalog",
        license = License(name = "MIT")
    )
)
@Configuration
class OpenApiConfig