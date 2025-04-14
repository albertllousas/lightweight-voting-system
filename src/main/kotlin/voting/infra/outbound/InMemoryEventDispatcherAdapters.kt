package voting.infra.outbound

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import voting.domain.EventPublisher
import voting.domain.VotingSessionEvent
import java.lang.invoke.MethodHandles

// Event publisher adapter for Spring

@Component
class SpringEventPublisherAdapter(private val publisher: ApplicationEventPublisher) : EventPublisher {

    override fun publish(event: VotingSessionEvent) {
        publisher.publishEvent(event)
    }
}

// Event handlers

/**
 * READ: If we want a reliable way to handle events, we should add here a:
 * TransactionalOutboxEventHandler
 * that will store the events in a database table to be reliable delivered to a message broker later on.
 */


@Component
class VotingSessionEventLoggingHandler(private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())) {

    @EventListener
    fun handle(event: VotingSessionEvent) {
        logger.info("event:'${event::class.java.simpleName}', voting-session-id:'${event.votingSession.id}'")
    }
}

@Component
class VotingSessionMetricsHandler(private val meterRegistry: MeterRegistry) {

    @EventListener
    fun handle(event: VotingSessionEvent) {
        meterRegistry.counter("voting-session-events", "event", event::class.java.simpleName).increment()
    }
}
