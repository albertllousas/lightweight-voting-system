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
import voting.fixtures.TestBuilders
import java.util.UUID

class VoteServiceTest {

    private val repository: VotingSessionRepository = mockk(relaxed = true)

    private val eventPublisher: EventPublisher = mockk(relaxed = true)

    private val vote = VoteService(repository, eventPublisher)

    @Test
    fun `should orchestrate the voting process`() {
        val votingSession = TestBuilders.buildVotingSession()
        every { repository.find(votingSession.id) } returns votingSession

        val result = vote(votingSession.id, votingSession.candidates[0], UUID.randomUUID())

        result shouldBe Unit.right()
        verify {
            repository.save(any())
            eventPublisher.publish(any<VotingSessionEvent.Voted>())
        }
    }

    @Test
    fun `should fail orchestrating the voting process when the voting fails`() {
        val votingSession = TestBuilders.buildVotingSession()
        every { repository.find(votingSession.id) } returns votingSession

        val result = vote(votingSession.id, UUID.randomUUID(), UUID.randomUUID())

        result.isLeft() shouldBe true
    }
}