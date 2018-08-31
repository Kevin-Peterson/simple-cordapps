package yo.spring

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
open class NodeRPCConnection(private val service: CordaRPCService): AutoCloseable{

    val proxy: CordaRPCOps//? = null
        get() {
            val tempProxy = this.mapCordaConnection.entries.first().value.proxy
            return tempProxy
        }

    var mapCordaConnection:HashMap<String, CordaRPCConnection> = HashMap()

    //private set

    @PostConstruct
    fun initialiseNodeRPCConnection() {
        service.rpcConfigs.forEach {
            val rpcAddress = NetworkHostAndPort(it.host, it.port.toInt())
            val rpcClient = CordaRPCClient(rpcAddress)
            val rpcConnection = rpcClient.start(it.username, it.password)
            //mapCordaConnection[it.x500Name.replace(" ","")]= rpcConnection
            mapCordaConnection[it.x500Name]= rpcConnection
        }
    }

    @PreDestroy
    override fun close() {
        mapCordaConnection.forEach{it.value.notifyServerAndClose()}
    }
}