package voting.domain

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import arrow.core.raise.ensure
import voting.domain.VotingSessionError.Type.AT_LEAST_TWO_CANDIDATES
import voting.domain.VotingSessionError.Type.REPEATED_CANDIDATE_ID
import voting.domain.VotingSessionError.Type.ALREADY_VOTED
import voting.domain.VotingSessionError.Type.NOT_PRESENT_CANDIDATE_ID
import voting.domain.VotingSessionError.Type.VOTING_CLOSED
import voting.domain.VotingSessionError.Type.VOTING_STILL_OPEN
import voting.domain.VotingSessionEvent.Closed
import voting.domain.VotingSessionEvent.Created
import voting.domain.VotingSessionEvent.Voted
import voting.domain.VotingStatus.CLOSED
import voting.domain.VotingStatus.OPEN
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

data class VotingSession(
    val id: UUID,
    val name: String,
    val candidates: List<UUID>,
    val status: VotingStatus = OPEN,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val closedAt: LocalDateTime? = null,
    val version: Long = 0,
    val votes: Map<UUID, List<UUID>> = mapOf(),
) {

    companion object {

        private val genId = { UUID.randomUUID() }

        fun create(
            name: String,
            candidates: List<UUID>,
            generateId: () -> UUID = genId,
            clock: Clock = Clock.systemUTC()
        ): Either<VotingSessionError, Created> =
            either {
                ensure(candidates.size >= 2) { VotingSessionError(AT_LEAST_TWO_CANDIDATES) }
                ensure(candidates.distinct().size == candidates.size) { VotingSessionError(REPEATED_CANDIDATE_ID) }

                val newSession = VotingSession(generateId(), name, candidates,OPEN, LocalDateTime.now(clock))
                Created(newSession)
            }
    }

    fun voteFor(candidateId: UUID, voterId: UUID): Either<VotingSessionError, Voted> = either {
        ensure(status == OPEN) { VotingSessionError(VOTING_CLOSED) }
        ensure(candidates.contains(candidateId)) { VotingSessionError(NOT_PRESENT_CANDIDATE_ID) }
        ensure(!votes.values.flatten().contains(voterId)) { VotingSessionError(ALREADY_VOTED) }

        val candidateVotes = votes[candidateId].orEmpty()
        val updatedSession = copy(votes = votes + (candidateId to (candidateVotes + voterId)))
        Voted(updatedSession, candidateId, voterId)
    }

    fun close(clock: Clock = Clock.systemUTC()): Either<VotingSessionError, Closed> = either {
        ensure(status == OPEN) { VotingSessionError(VOTING_CLOSED) }

        Closed(copy(status = CLOSED, closedAt = LocalDateTime.now(clock)))
    }

    fun winners() : Either<VotingSessionError, List<UUID>> = either {
        ensure(status == CLOSED) { VotingSessionError(VOTING_STILL_OPEN) }

        val maxVotes = votes.values.maxOf { it.size }
        votes.filter { (_, voters) -> voters.size == maxVotes }.keys.toList()
    }

}

enum class VotingStatus {
    OPEN, CLOSED
}

data class VotingSessionError(val error: Type) {
    enum class Type {
        VOTING_CLOSED, NOT_PRESENT_CANDIDATE_ID, ALREADY_VOTED, AT_LEAST_TWO_CANDIDATES, REPEATED_CANDIDATE_ID, VOTING_STILL_OPEN
    }
}

sealed class VotingSessionEvent {

    abstract val votingSession: VotingSession

    data class Created(override val votingSession: VotingSession) : VotingSessionEvent()
    data class Closed(override val votingSession: VotingSession) : VotingSessionEvent()
    data class Voted(override val votingSession: VotingSession, val candidateId: UUID, val voterId: UUID) :
        VotingSessionEvent()
}
