package no.novari.fintkontrolldevicecatalog.service

import jdk.vm.ci.riscv64.RISCV64
import no.novari.fintkontrolldevicecatalog.kaftadevice.KafkaDevice
import org.springframework.stereotype.Service

@Service
class DeviceMappingService {

    fun mapToKontrollDevice(device: KafkaDevice) {
        val sourceID = device.systemId
        val platform = device.platform

        println("Mapping device: sourceID::$sourceID and platform::$platform")


    }

    fun <T> mapKafkaToKontroll(consumerClass: T): V {

        return V()
    }





}