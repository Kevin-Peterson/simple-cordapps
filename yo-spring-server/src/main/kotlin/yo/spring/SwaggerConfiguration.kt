package yo.spring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.documentation.swagger.web.ApiKeyVehicle
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.service.*
import springfox.documentation.spi.service.contexts.SecurityContext
import java.util.ArrayList


//http://localhost:20010/swagger-ui.html
@Configuration
@EnableSwagger2
open class SwaggerConfiguration {
    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("yo.spring"))
            .paths(regex("/api.*"))
//            .apis(RequestHandlerSelectors.basePackage("aurum.spring_server.security"))
//            .paths(regex("/login.*"))

            .build()
            .apiInfo(metaInfo())
            .securityContexts(listOf(securityContext()))
            .securitySchemes(listOf(apiKey()))

    private fun metaInfo(): ApiInfo {

        return ApiInfo(
                "YO Swagger API",
                "YO Swagger API for execute Corda/DLT functions",
                "1.0",
                "Terms of Service",
                Contact("Distributed Ledger Tech Development", "www.wellsfargo.com", "blockchain@wellsfargo.com"),
                "Apache License Version 2.0",
                "https://www.apache.org/licesen.html",
                ArrayList()
        )
    }


    private fun apiKey(): ApiKey {
        return ApiKey("Authorization", "Authorization", "header")
    }

    @Bean
    fun security(): SecurityConfiguration {
        return SecurityConfiguration(// apiKeyName
                null, null, null, null, // appName Needed for authenticate button to work
                "  ", // apiKeyValue
                ApiKeyVehicle.HEADER, "Authorization", null)// realm Needed for authenticate button to work
    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/anyPath.*"))
                .build()
    }

    fun defaultAuth(): List<SecurityReference> {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return listOf(
                SecurityReference("AUTHORIZATION", authorizationScopes))
    }
}