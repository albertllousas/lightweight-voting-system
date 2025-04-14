package voting.application

import arrow.core.Either
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voting.domain.EventPublisher
import voting.domain.VotingSession
import voting.domain.VotingSessionError
import voting.domain.VotingSessionRepository
import java.util.UUID

@Service
class CreateService(
    private val repository: VotingSessionRepository,
    private val publisher: EventPublisher,
    private val generateId: () -> UUID = { UUID.randomUUID() }
) {

    @Transactional
    operator fun invoke(name: String, candidates: List<UUID>): Either<VotingSessionError, UUID> =
        VotingSession.create(name, candidates, generateId)
            .onRight { repository.save(it.votingSession) }
            .onRight { publisher.publish(it) }
            .map { it.votingSession.id }
}

@Service
class VoteService(private val repository: VotingSessionRepository, private val publisher: EventPublisher) {

    @Transactional
    operator fun invoke(votingSessionId: UUID, candidateId: UUID, voterId: UUID): Either<VotingSessionError, Unit> =
        repository.find(votingSessionId)
            .voteFor(candidateId, voterId)
            .onRight { repository.save(it.votingSession) }
            .map { publisher.publish(it) }
}

@Service
class CloseService(private val repository: VotingSessionRepository, private val publisher: EventPublisher) {

    @Transactional
    operator fun invoke(votingSessionId: UUID): Either<VotingSessionError, Unit> =
        repository.find(votingSessionId)
            .close()
            .onRight { repository.save(it.votingSession) }
            .map { publisher.publish(it) }
}
