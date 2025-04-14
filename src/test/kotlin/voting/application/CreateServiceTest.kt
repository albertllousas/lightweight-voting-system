package voting.application

import arrow.core.right
import io.kotest.assertions.any
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.com.google.common.base.Verify.verify
import voting.domain.EventPublisher
import voting.domain.VotingSessionEvent
import voting.domain.VotingSessionRepository
import java.util.UUID

class CreateServiceTest {

    private val repository: VotingSessionRepository = mockk(relaxed = true)

    private val eventPublisher: EventPublisher = mockk(relaxed = true)

    private val uuid = UUID.randomUUID()

    private val create = CreateService(repository, eventPublisher, { uuid })

    @Test
    fun `should orchestrate the creating process`() {
        val candidates = listOf(UUID.randomUUID(), UUID.randomUUID())

        val result = create("Who made the best dish? Vote now!", candidates)

        result shouldBe uuid.right()
        verify {
            repository.save(any())
            eventPublisher.publish(any<VotingSessionEvent.Created>())
        }
    }

    @Test
    fun `should fail orchestrating the creating process when creating the session fails`() {
        val candidates = listOf(UUID.randomUUID())

        val result = create("", candidates)

        result.isLeft() shouldBe true
    }
}