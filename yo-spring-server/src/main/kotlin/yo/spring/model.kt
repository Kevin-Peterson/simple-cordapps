package yo.spring

import io.swagger.annotations.ApiModelProperty
import net.corda.core.identity.CordaX500Name

data class X500Name(@ApiModelProperty(notes = "name of the organization") val organization: String,
                    @ApiModelProperty(notes = "name of the locality") val locality: String,
                    @ApiModelProperty(notes = "name of the Country") val country: String) {
    constructor() : this("", "", "")

    companion object {
        fun getX500Name(x500Name: String): X500Name {
            val (_, _, organization, locality, _, country) = CordaX500Name.parse(x500Name)
            return X500Name(organization, locality, country)
        }
    }

    override fun toString(): String {
        return "O=$organization, L=$locality, C=$country"
    }

}