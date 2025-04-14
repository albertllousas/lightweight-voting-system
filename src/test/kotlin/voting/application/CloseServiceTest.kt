package voting.application

import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import voting.domain.EventPublisher
import voting.domain.VotingSessionEvent
import voting.domain.VotingSessionRepository
import voting.domain.VotingStatus.CLOSED
import votingsession.fixtures.TestBuilders

class CloseServiceTest {

    private val repository: VotingSessionRepository = mockk(relaxed = true)

    private val eventPublisher: EventPublisher = mockk(relaxed = true)

    private val close = CloseService(repository, eventPublisher)

    @Test
    fun `should orchestrate the closing process`() {
        val votingSession = TestBuilders.buildVotingSession()
        every { repository.find(votingSession.id) } returns votingSession

        val result = close(votingSession.id)

        result shouldBe Unit.right()
        verify {
            repository.save(any())
            eventPublisher.publish(any<VotingSessionEvent.Closed>())
        }
    }

    @Test
    fun `should fail orchestrating the closing process when the closing fails`() {
        val votingSession = TestBuilders.buildVotingSession(status = CLOSED)
        every { repository.find(votingSession.id) } returns votingSession

        val result = close(votingSession.id)

        result.isLeft() shouldBe true
    }
}