package votingsession.fixtures

import voting.domain.VotingSession
import voting.domain.VotingStatus
import java.time.LocalDateTime
import java.util.UUID

object TestBuilders {
    fun buildVotingSession(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Voting Session",
        candidates: List<UUID> = listOf(UUID.randomUUID(), UUID.randomUUID()),
        status: VotingStatus = VotingStatus.OPEN,
        createdAt: LocalDateTime = LocalDateTime.now(),
        closedAt: LocalDateTime? = null,
        version: Long = 0,
        votes: Map<UUID, List<UUID>> = emptyMap()
    ) = VotingSession(id, name, candidates, status, createdAt, closedAt, version, votes)
}
