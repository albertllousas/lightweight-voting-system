package voting.acceptance

import io.restassured.RestAssured
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import voting.acceptance.BaseAcceptanceTest.Initializer
import voting.infra.App
import voting.fixtures.Postgres

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = [Initializer::class], classes = [App::class])
@DirtiesContext(classMode = AFTER_CLASS)
abstract class BaseAcceptanceTest {

    init {
        RestAssured.defaultParser = io.restassured.parsing.Parser.JSON
    }

    @LocalServerPort
    protected val servicePort: Int = 0

    companion object {
        val postgres: Postgres = Postgres()
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=" + postgres.container.jdbcUrl,
                "spring.datasource.password=" + postgres.container.password,
                "spring.datasource.username=" + postgres.container.username,
                "spring.flyway.url=" + postgres.container.jdbcUrl,
                "spring.flyway.password=" + postgres.container.password,
                "spring.flyway.user=" + postgres.container.username,
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
