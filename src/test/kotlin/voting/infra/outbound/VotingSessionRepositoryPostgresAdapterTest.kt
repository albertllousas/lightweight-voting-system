package voting.infra.outbound

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import voting.fixtures.Postgres
import voting.fixtures.TestBuilders
import java.util.UUID

@Tag("integration")
class VotingSessionRepositoryPostgresAdapterTest {

    private val postgres = Postgres()

    private val repository = VotingSessionRepositoryPostgresAdapter(postgres.jdbcTemplate)

    @Test
    fun `should save and find a voting session`() {
        val votingSession = TestBuilders.buildVotingSession()

        repository.save(votingSession)
        val result = repository.find(votingSession.id)

        result shouldBe votingSession.copy(version = 1)
    }

    @Test
    fun `should not find a voting session when it does not exists`() {
        shouldThrow<VotingSessionNotFoundException> { repository.find(UUID.randomUUID()) }
    }

    @Test
    fun `should update voting session`() {
        val votingSession = TestBuilders.buildVotingSession().also { repository.save(it) }.let { repository.find(it.id)!! }
        val updatedVotingSession = votingSession.copy(name = "New Name", )

        repository.save(updatedVotingSession)
        val result = repository.find(votingSession.id)

        result shouldBe updatedVotingSession.copy(version = 2)
    }

    @Test
    fun `should throw OptimisticLockingException when updating voting session with wrong version`() {
        val votingSession = TestBuilders.buildVotingSession().also { repository.save(it) }.let { repository.find(it.id)!! }
        val updatedVotingSession = votingSession.copy(name = "New Name", version = 3)

        shouldThrow<OptimisticLockingException> { repository.save(updatedVotingSession) }
    }
}