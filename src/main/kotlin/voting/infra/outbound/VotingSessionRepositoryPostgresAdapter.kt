package voting.infra.outbound

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import voting.domain.VotingSession
import voting.domain.VotingSessionRepository
import voting.domain.VotingStatus
import java.util.*

@Repository
class VotingSessionRepositoryPostgresAdapter(
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: ObjectMapper = jacksonObjectMapper()
) : VotingSessionRepository {

    @Transactional(propagation = MANDATORY)
    override fun save(votingSession: VotingSession) {
        with(votingSession) {
            val candidates: Array<String> = candidates.map { it.toString() }.toTypedArray()
            if (votingSession.version == 0L)
                jdbcTemplate.update(
                    """
                INSERT INTO voting_sessions (id, name, candidates, status, votes, created_at, closed_at, version) 
                VALUES (?,?,?,?,?::jsonb,?,?,?) 
                """, id, name, candidates, status.toString(), mapper.writeValueAsString(votes), createdAt, closedAt, 1
                )
            else
                jdbcTemplate.queryForObject(
                    """
                UPDATE voting_sessions 
                SET name = ?, candidates = ?, status = ?, votes = ?::jsonb, closed_at = ?, version = version + 1 WHERE id = ?
                RETURNING version
                """,
                    Long::class.java,
                    name,
                    candidates,
                    status.toString(),
                    mapper.writeValueAsString(votes),
                    closedAt,
                    id
                ).also { if (it != version + 1) throw OptimisticLockingException(id) }
        }
    }

    override fun find(id: UUID): VotingSession = try {
        jdbcTemplate.queryForObject(
            """ SELECT * FROM voting_sessions WHERE id = '$id' """
        ) { rs, _ ->
            VotingSession(
                id = rs.getObject("id", UUID::class.java),
                name = rs.getString("name"),
                candidates = rs.getArray("candidates").array.let { it as Array<String> }.map { UUID.fromString(it) },
                status = rs.getString("status").let { VotingStatus.valueOf(it) },
                createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                closedAt = rs.getTimestamp("closed_at")?.toLocalDateTime(),
                version = rs.getLong("version"),
                votes = mapper.readValue(
                    rs.getString("votes"),
                    object : TypeReference<Map<UUID, List<UUID>>>() {}
                )
            )
        }
    } catch (exception: EmptyResultDataAccessException) {
        throw VotingSessionNotFoundException(id)
    }
}

data class OptimisticLockingException(val id: UUID) : RuntimeException(
    "Failed to update Voting session with id = '$id', possibly due to a concurrent modification"
)

data class VotingSessionNotFoundException(val id: UUID) : RuntimeException(
    "Voting session with id = '$id' not found"
)
