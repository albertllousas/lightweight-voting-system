package voting.infra

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication(scanBasePackages = ["voting"])
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@Configuration
class FrameworkConfig {

    @Bean
    fun meterRegistry() = SimpleMeterRegistry()
}