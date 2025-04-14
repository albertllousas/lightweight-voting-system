package voting.domain

import java.util.UUID

interface VotingSessionRepository {
    fun find(id: UUID): VotingSession
    fun save(votingSession: VotingSession)
}

interface EventPublisher {
    fun publish(event: VotingSessionEvent)
}