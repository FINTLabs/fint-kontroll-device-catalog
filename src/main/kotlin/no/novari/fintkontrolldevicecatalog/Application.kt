package no.novari.fintkontrolldevicecatalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication(
    scanBasePackages = ["no.novari","no.fintlabs"]
)
@EnableScheduling
@EnableWebSecurity
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
