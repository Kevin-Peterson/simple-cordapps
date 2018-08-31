package yo.spring

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.yo.YoFlow
import net.corda.yo.YoState
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class YO(val origin: String,
                   val target: String,
                   val yo: String = "Yo!")
@RestController
@Api(value = "YO Controller Resource", description = "shows YO corda operations")
@RequestMapping("/api/yo")
class YOController(private val service: YORPCService) {

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("yo", produces = arrayOf("text/plain"))
    @ApiOperation(value = "Send YO")
    fun yo(@RequestParam(value = "target") target: String): ResponseEntity<String?> {

         val (status, message) = try {
            // Look-up the 'target'.
            val matches = service.proxy.partiesFromName(target, exactMatch = true)

            // We only want one result!
            val to: Party = when {
                matches.isEmpty() -> throw IllegalArgumentException("Target string doesn't match any nodes on the network.")
                matches.size > 1 -> throw IllegalArgumentException("Target string matches multiple nodes on the network.")
                else -> matches.single()
            }

            // Start the flow, block and wait for the response.
            // val result = service.proxy.startFlowDynamic(YoFlow::class.java, to).returnValue.getOrThrow() val result = service.proxy.startFlowDynamic(YoFlow::class.java, to).returnValue.getOrThrow()
            service.proxy.startFlowDynamic(YoFlow::class.java, to).returnValue.getOrThrow()
            // Return the response.
             HttpStatus.OK to "You just sent a Yo! to ${to.name} "
        } catch (e: Exception) {
            //Response.Status.BAD_REQUEST to e.message
            HttpStatus.BAD_REQUEST to e.message
        }
        return if (status == HttpStatus.OK) {
            ResponseEntity.ok().body(message.toString())
        } else {
            ResponseEntity.badRequest().body(message.toString())
        }
    }


    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("yos", produces = arrayOf("application/json"))
    @ApiOperation(value = "get YOs")
    fun yos(): MutableList<YO> {
        val states =service.proxy.vaultQuery(YoState::class.java).states
        val listYOs = mutableListOf<YO>()
        states.map {
            listYOs.add(YO(it.state.data.origin.name.toString(),
                    it.state.data.target.name.toString(),
                    it.state.data.yo))
        }
        return listYOs
    }

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("me", produces = arrayOf("application/json"))
    @ApiOperation(value = "me")
    fun me() = mapOf("me" to service.proxy.nodeInfo().legalIdentities.first().name)

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("peers", produces = arrayOf("application/json"))
    @ApiOperation(value = "get peers")
    fun peers() = mapOf("peers" to service.proxy.networkMapSnapshot().map { it.legalIdentities.first().name })
}