package yo.spring

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import java.util.Currency
import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.NullKeys
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.toBase58String
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.utilities.getOrThrow
import net.corda.obligation.flows.IssueObligation
import net.corda.obligation.flows.SettleObligation
import net.corda.obligation.flows.TransferObligation
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.getCashBalances
import net.corda.finance.flows.CashIssueFlow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class Obligation(val amount: Amount<Currency>,
                      val lender: AbstractParty,
                      val borrower: AbstractParty,
                      val paid: Amount<Currency> = Amount(0, amount.token),
                      override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    override val participants: List<AbstractParty> get() = listOf(lender, borrower)

    fun pay(amountToPay: Amount<Currency>) = copy(paid = paid + amountToPay)
    fun withNewLender(newLender: AbstractParty) = copy(lender = newLender)
    fun withoutLender() = copy(lender = NullKeys.NULL_PARTY)

    override fun toString(): String {
        val lenderString = (lender as? Party)?.name?.organisation ?: lender.owningKey.toBase58String()
        val borrowerString = (borrower as? Party)?.name?.organisation ?: borrower.owningKey.toBase58String()
        return "Obligation($linearId): $borrowerString owes $lenderString $amount and has paid $paid so far."
    }
}

@RestController
@Api(value = "Obligation Controller Resource", description = "shows Obligation corda operations")
@RequestMapping("/api/obligation")
class ObligController(private val service: YORPCService) {

    private val myIdentity = service.proxy.nodeInfo().legalIdentities.first()


    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("me", produces = arrayOf("application/json"))
    @ApiOperation(value = "me")
    fun me() = mapOf("me" to service.proxy.nodeInfo().legalIdentities.first().name)

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("peers", produces = arrayOf("application/json"))
    @ApiOperation(value = "get peers")
    fun peers() = mapOf("peers" to service.proxy.networkMapSnapshot().map { it.legalIdentities.first().name })

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("owed-per-currency", produces = arrayOf("application/json"))
    @ApiOperation(value = "get currency owned")
    fun owedPerCurrency() = service.proxy.vaultQuery(Obligation::class.java).states
            .filter { (state) -> state.data.lender != myIdentity }
            .map { (state) -> state.data.amount }
            .groupBy({ amount -> amount.token }, { (quantity) -> quantity })
            .mapValues { it.value.sum() }

    @CrossOrigin(origins = arrayOf("http://localhost:4200"))
    @GetMapping("issue-obligation", produces = arrayOf("text/plain"))
    @ApiOperation(value = "Send Obligation")
    fun issueObligation(@RequestParam(value = "amount") amount: Int,
                        @RequestParam(value = "currency") currency: String,
                        @RequestParam(value = "party") party: String): ResponseEntity<String?>
    {
        // 1. Get party objects for the counterparty.
        val lenderIdentity = service.proxy.partiesFromName(party, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity for $party.")

        // 2. Create an amount object.
        val issueAmount = Amount(amount.toLong() * 100, Currency.getInstance(currency))

        // 3. Start the IssueObligation flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = service.proxy.startFlowDynamic(
                    IssueObligation.Initiator::class.java,
                    issueAmount,
                    lenderIdentity,
                    true
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // 4. Return the result.
        // return Response.status(status).entity(message).build()
        return if (status == HttpStatus.BAD_REQUEST) {
            ResponseEntity.badRequest().body(message.toString())
        } else {
            ResponseEntity.ok().body(message.toString())
        }
    }

/*
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
        val result = service.proxy.startFlowDynamic(YoFlow::class.java, to).returnValue.getOrThrow()
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
    */

@CrossOrigin(origins = arrayOf("http://localhost:4200"))
@GetMapping("obligations", produces = arrayOf("application/json"))
@ApiOperation(value = "get Obligations")
fun obligations(): MutableList<Obligation> {
    val statesAndRefs = service.proxy.vaultQuery(Obligation::class.java).states
    val listObligs = mutableListOf<Obligation>()
    statesAndRefs.map {
        listObligs.add(Obligation(it.state.data.amount,
                it.state.data.lender,
                it.state.data.borrower,
                it.state.data.paid,
                it.state.data.linearId))
    }
    return listObligs
    /*
    return statesAndRefs
            .map { stateAndRef -> stateAndRef.state.data }
            .map { state ->
                // We map the anonymous lender and borrower to well-known identities if possible.
                val possiblyWellKnownLender = rpcOps.wellKnownPartyFromAnonymous(state.lender) ?: state.lender
                val possiblyWellKnownBorrower = rpcOps.wellKnownPartyFromAnonymous(state.borrower) ?: state.borrower

                Obligation(state.amount,
                        possiblyWellKnownLender,
                        possiblyWellKnownBorrower,
                        state.paid,
                        state.linearId)
            }
            */
}
}