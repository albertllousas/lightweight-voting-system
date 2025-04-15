package voting.infra.outbound

import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.helpers.NOPLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import voting.domain.VotingSessionEvent
import voting.fixtures.TestBuilders

@Tag("integration")
@SpringBootTest(
    classes = [
        SpringEventPublisherAdapter::class,
        VotingSessionEventLoggingHandler::class,
        VotingSessionMetricsHandler::class,
        TestConfig::class
    ]
)
class SpringEventPublisherAdapterTest {

    @Autowired
    private lateinit var eventPublisherAdapter: SpringEventPublisherAdapter

    @Autowired
    private lateinit var logger: Logger

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @Test
    fun `should publish an event`() {
        val event = VotingSessionEvent.Created(TestBuilders.buildVotingSession())

        eventPublisherAdapter.publish(event)

        verify { logger.info("event:'Created', voting-session-id:'${event.votingSession.id}'") }
        meterRegistry.get("voting-session-events").tags("event", "Created").counter().count() shouldBe 1.0
    }

}

class TestConfig {
    @Bean
    fun logger(): Logger = spyk(NOPLogger.NOP_LOGGER)

    @Bean
    fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()
}