package yo.spring


import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.utilities.loggerFor
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val CONTROLLER_NAME = "config.rootcontroller.name"

@RestController
@RequestMapping("/api")
@Api(value = "Root Controller Resource", description = "shows basic corda operations")
class RootController(
        private val service: YORPCService)  {

    //        },
//        private val controllerName: String = "RootController"){
//        @Value("\${$CONTROLLER_NAME}") private val controllerName: String) {
    companion object {
        val logger = loggerFor<RootController>()
        private val controllerName: String = "RootController"
    }


    @ApiOperation(value = "Returns http 200 OK")
    @GetMapping("/status", produces = arrayOf("text/plain"))
    private fun getStatus() =
            ResponseEntity.ok().build<String>()

    @ApiOperation(value = "Returns Server Time")
    @GetMapping( "/servertime", produces = arrayOf("text/plain"))
    private fun serverTime() = service.serverTime()

    @ApiOperation(value = "Returns ipv6 address")
    @GetMapping( "/addresses", produces = arrayOf("text/plain"))
    private fun addresses() = service.addresses()

    @ApiOperation(value = "Who am I?")
    @GetMapping( "/identities", produces = arrayOf("text/plain"))
    private fun identities() = service.identities()

    @ApiOperation(value = "Returns current corda platform version")
    @GetMapping( "/platformversion", produces = arrayOf("text/plain"))
    private fun platformVersion() = service.platformVersion()

    @ApiOperation(value = "Returns my node peers")
    @GetMapping( "/peers", produces = arrayOf("application/json"))
    private fun getPeers(): ResponseEntity<Map<String, List<X500Name>>>? {
        val peers = service.getPeers()
        return ResponseEntity.ok().body(
                peers)
    }

    @ApiOperation(value = "Returns registered notaries")
    @GetMapping( "/notaries", produces = arrayOf("text/plain"))
    private fun notaries() = service.notaries()

    @ApiOperation(value = "Returns registered flows")
    @GetMapping( "/flows", produces = arrayOf("text/plain"))
    private fun flows() = service.flows()

    @ApiOperation(value = "Returns states")
    @GetMapping( "/states", produces = arrayOf("text/plain"))
    private fun states() = service.states()

    @ApiOperation(value = "Returns timezones")
    @GetMapping( "/timezones", produces = arrayOf("application/json"))
    private fun timezones(): ResponseEntity<Any> {
        val timezones = service.getTimeZones()
        return ResponseEntity.ok().body(timezones)
    }

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("currencies", produces = arrayOf("application/json"))
    @ApiOperation(value = "List of all valid currencies")
    fun getCurrencies(): ResponseEntity<Any> {
        var listCurrency = mutableListOf<CurrencyDTO>()
        val currencies = service.getCurrencies()
        currencies.forEach { c ->
            val dto = CurrencyDTO(c.currencyCode, c.displayName, c.defaultFractionDigits, c.numericCode, c.symbol)
            listCurrency.add(dto)
        }
        return ResponseEntity.ok(listCurrency)
    }

    @ApiOperation(value = "Returns node connections")
    @GetMapping( "/nodes", produces = arrayOf("application/json"))
    private fun nodes(): ResponseEntity<List<String>> {
        val nodes = service.getNodeConnections()
        return ResponseEntity.ok().body(nodes)
    }

    data class CurrencyDTO(val currencyCode: String, val displayName: String, val defaultFractionDigits: Int, val numericCode: Int, val symbol: String)

}