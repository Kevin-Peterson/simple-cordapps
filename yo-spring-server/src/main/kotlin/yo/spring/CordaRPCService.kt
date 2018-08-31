package yo.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "cordarpc")
@Configuration
class CordaRPCConfig {
    var rpcConfigs: MutableList<CordaRPC> = mutableListOf()
}

class CordaRPC {
    lateinit var host:String
    lateinit var port:String
    lateinit var username:String
    lateinit var password:String
    lateinit var x500Name: String
    override fun toString(): String {
        return "$host:$port user: $username password: $password x500Name: $x500Name "
    }
}

@Component
class CordaRPCService(private var cordaRPCConfig: CordaRPCConfig){
    val rpcConfigs: MutableList<CordaRPC>
        get() = this.cordaRPCConfig.rpcConfigs
}