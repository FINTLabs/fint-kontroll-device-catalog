package no.novari.fintkontrolldevicecatalog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["no.novari","no.fintlabs"]
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
