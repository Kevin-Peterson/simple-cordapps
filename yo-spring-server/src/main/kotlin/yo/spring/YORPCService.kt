package yo.spring

import net.corda.core.contracts.ContractState
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.NodeInfo
import net.corda.core.utilities.loggerFor
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class YORPCService(private val rpc: NodeRPCConnection,
                   private val config: CordaRPCService){
    val proxy = rpc.proxy
    companion object {
        val logger = loggerFor<YORPCService>()
    }
    private fun isNotary(nodeInfo: NodeInfo) = proxy.notaryIdentities().any { nodeInfo.isLegalIdentity(it) }
    private fun isMe(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.first().name == proxy.nodeInfo().legalIdentities[0].name
    private fun isNetworkMap(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.single().name.organisation == "Network Map Service"
    fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()
    fun addresses() = proxy.nodeInfo().addresses.toString()
    fun flows() = proxy.registeredFlows().toString()
    fun notaries() = proxy.notaryIdentities().toString()
    fun platformVersion() = proxy.nodeInfo().platformVersion.toString()
    fun identities() = proxy.nodeInfo().legalIdentities.toString()
    fun states() = proxy.vaultQueryBy<ContractState>().states.toString()
    fun getPeers(): Map<String, List<X500Name>> {
        val (nodeInfo, _) = proxy.networkMapFeed()
        return mapOf("peers" to nodeInfo
                .filter { isNotary(it).not() && isMe(it).not() && isNetworkMap(it).not() }
                //.map { it.legalIdentities.first().name.x500Principal.name })
                .map { X500Name.getX500Name(it.legalIdentities.first().name.x500Principal.name) })

    }


    fun getCurrencies(): List<Currency> {
        return Currency.getAvailableCurrencies().sortedBy { it.currencyCode }
    }
    fun getTimeZones(): List<String> {
        return TimeZone.getAvailableIDs().toList()
    }
    fun getNodeConnections(): List<String> {
        return this.config.rpcConfigs.map { it.x500Name }
    }
    fun getProxy(party: String): CordaRPCOps {
        val x500Name = X500Name.getX500Name(party)
        if (!rpc.mapCordaConnection.containsKey(x500Name.toString())) {
            logger.error("party $party not found")
            rpc.mapCordaConnection.forEach { logger.info(it.key) }
        }
        return rpc.mapCordaConnection[x500Name.toString()]!!.proxy
    }
}