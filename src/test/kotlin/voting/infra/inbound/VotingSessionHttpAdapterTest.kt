package voting.infra.inbound

import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import voting.application.CloseService
import voting.application.CreateService
import voting.application.VoteService
import voting.domain.VotingSessionError
import voting.domain.VotingSessionError.Type.REPEATED_CANDIDATE_ID
import voting.domain.VotingSessionRepository
import java.util.UUID


@Tag("integration")
@WebMvcTest(VotingSessionHttpAdapter::class)
class CustomerRestAdapterTest(@Autowired private val mvc: MockMvc) {

    @MockkBean
    private lateinit var create: CreateService

    @MockkBean
    private lateinit var vote: VoteService

    @MockkBean
    private lateinit var close: CloseService

    @MockkBean
    private lateinit var repository: VotingSessionRepository

    @Test
    fun `should create a voting session`() {
        val votingSessionId = UUID.randomUUID()
        val candidateId = UUID.randomUUID()
        every { create.invoke("Voting Session", listOf(candidateId)) } returns votingSessionId.right()

        val response = mvc.perform(
            post("/voting-sessions")
                .contentType("application/json")
                .content(
                    """ { "name": "Voting Session", "candidates": ["$candidateId"] } """
                )
        )

        response.andExpect(status().isCreated)
        response.andExpect(content().json(""" { "id": "$votingSessionId" } """))
    }

    @Test
    fun `should fail with bad request if creating a session fails`() {
        every { create.invoke(any(), any()) } returns VotingSessionError(REPEATED_CANDIDATE_ID).left()

        val response = mvc.perform(
            post("/voting-sessions")
                .contentType("application/json")
                .content(
                    """ { "name": "Voting Session", "candidates": ["${UUID.randomUUID()}"] } """
                )
        )

        response.andExpect(status().isBadRequest)
        response.andExpect(content().json(""" { "error": "REPEATED_CANDIDATE_ID" } """))
    }

    @Test
    fun `should vote a session`() {
        val votingSessionId = UUID.randomUUID()
        val candidateId = UUID.randomUUID()
        val voterId = UUID.randomUUID()
        every { vote.invoke(votingSessionId, candidateId, voterId) } returns Unit.right()

        val response = mvc.perform(
            patch("/voting-sessions/$votingSessionId/vote")
                .contentType("application/json")
                .content(
                    """ { "candidateId": "$candidateId", "voterId": "$voterId" } """
                )
        )

        response.andExpect(status().isNoContent)
    }

    @Test
    fun `should close a voting session`() {
        val votingSessionId = UUID.randomUUID()
        every { close.invoke(votingSessionId) } returns Unit.right()

        val response = mvc.perform(
            patch("/voting-sessions/$votingSessionId/close")
                .contentType("application/json")
        )

        response.andExpect(status().isNoContent)
    }

    @Test
    fun `should get winners of a voting session`() {
        val votingSessionId = UUID.randomUUID()
        val winners = listOf(UUID.randomUUID())
        every { repository.find(votingSessionId).winners() } returns winners.right()

        val response = mvc.perform(
            get("/voting-sessions/$votingSessionId/winners")
                .contentType("application/json")
        )

        response.andExpect(status().isOk)
        response.andExpect(content().json(""" { "winners": ["${winners[0]}"] } """))
    }
}
