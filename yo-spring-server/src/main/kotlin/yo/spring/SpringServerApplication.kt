package yo.spring

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class SpringServerApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(SpringServerApplication::class.java)
    app.setBannerMode(Banner.Mode.OFF)
//    app.isWebEnvironment = true
    app.run(*args)
}