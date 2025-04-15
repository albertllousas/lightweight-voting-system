package voting.domain

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import voting.domain.VotingSessionError.Type.ALREADY_VOTED
import voting.domain.VotingSessionError.Type.AT_LEAST_TWO_CANDIDATES
import voting.domain.VotingSessionError.Type.NOT_PRESENT_CANDIDATE_ID
import voting.domain.VotingSessionError.Type.VOTING_CLOSED
import voting.domain.VotingStatus.CLOSED
import voting.domain.VotingStatus.OPEN
import voting.fixtures.TestBuilders
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class VotingSessionTest {

    private val clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.systemDefault())

    @Nested
    inner class Creation {

        private val id = UUID.randomUUID()

        @Test
        fun `should create a voting session`() {
            val question = "Who made the best dish? Vote now!"
            val candidates = listOf(UUID.randomUUID(), UUID.randomUUID())

            val result = VotingSession.create(question, candidates, { id }, clock)

            result shouldBe VotingSessionEvent.Created(
                VotingSession(
                    id = id,
                    name = question,
                    candidates = candidates,
                    status = OPEN,
                    createdAt = LocalDateTime.now(clock),
                    closedAt = null,
                    version = 0,
                    votes = emptyMap()
                )
            ).right()
        }

        @Test
        fun `should fail creating a voting session when there are less than two candidates`() {
            val result = VotingSession.create("Who made the best dish? Vote now!", listOf(UUID.randomUUID()))

            result shouldBe VotingSessionError(AT_LEAST_TWO_CANDIDATES).left()
        }

        @Test
        fun `should fail creating a voting session when there are repeated candidate IDs`() {
            val candidateId = UUID.randomUUID()

            val result = VotingSession.create("Who made the best dish? Vote now!", listOf(candidateId, candidateId))

            result shouldBe VotingSessionError(VotingSessionError.Type.REPEATED_CANDIDATE_ID).left()
        }
    }

    @Nested
    inner class Voting {

        @Test
        fun `should vote for a candidate`() {
            val candidateId = UUID.randomUUID()
            val voterId = UUID.randomUUID()
            val votingSession = TestBuilders.buildVotingSession(candidates = listOf(candidateId, UUID.randomUUID()))

            val result = votingSession.voteFor(candidateId, voterId)

            result shouldBe VotingSessionEvent.Voted(
                votingSession.copy(votes = mapOf(candidateId to listOf(voterId))),
                candidateId,
                voterId
            ).right()
        }

        @Test
        fun `should fail voting for a candidate when the voting session is closed`() {
            val candidateId = UUID.randomUUID()
            val voterId = UUID.randomUUID()
            val votingSession = TestBuilders.buildVotingSession(
                candidates = listOf(candidateId, UUID.randomUUID()),
                status = CLOSED
            )

            val result = votingSession.voteFor(candidateId, voterId)

            result shouldBe VotingSessionError(VOTING_CLOSED).left()
        }

        @Test
        fun `should fail voting for a candidate when the candidate ID is not present`() {
            val candidateId = UUID.randomUUID()
            val voterId = UUID.randomUUID()
            val votingSession = TestBuilders.buildVotingSession(candidates = listOf(UUID.randomUUID()))

            val result = votingSession.voteFor(candidateId, voterId)

            result shouldBe VotingSessionError(NOT_PRESENT_CANDIDATE_ID).left()
        }

        @Test
        fun `should fail voting for a candidate when the voter has already voted`() {
            val candidateId = UUID.randomUUID()
            val voterId = UUID.randomUUID()
            val votingSession = TestBuilders.buildVotingSession(
                candidates = listOf(candidateId, UUID.randomUUID()),
                votes = mapOf(candidateId to listOf(voterId))
            )

            val result = votingSession.voteFor(candidateId, voterId)

            result shouldBe VotingSessionError(ALREADY_VOTED).left()
        }
    }

    @Nested
    inner class Closing {

        @Test
        fun `should close the voting session`() {
            val votingSession = TestBuilders.buildVotingSession(status = OPEN)

            val result = votingSession.close(clock)

            result shouldBe VotingSessionEvent.Closed(
                votingSession.copy(status = CLOSED, closedAt = LocalDateTime.now(clock))
            ).right()
        }

        @Test
        fun `should fail closing the voting session when it is already closed`() {
            val votingSession = TestBuilders.buildVotingSession(status = CLOSED)

            val result = votingSession.close()

            result shouldBe VotingSessionError(VOTING_CLOSED).left()
        }
    }
}
