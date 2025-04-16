package voting.infra.inbound

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import voting.application.CloseService
import voting.application.CreateService
import voting.application.VoteService
import voting.domain.VotingSessionRepository
import voting.infra.outbound.VotingSessionRepositoryPostgresAdapter
import java.util.UUID

@RestController
@RequestMapping("/voting-sessions")
class VotingSessionHttpAdapter(
    private val create: CreateService,
    private val vote: VoteService,
    private val close: CloseService,
    private val repository: VotingSessionRepositoryPostgresAdapter,
) {

    @PostMapping
    fun createVotingSession(@RequestBody request: CreateHttpRequest) =
        create(request.name, request.candidates)
            .fold(ifLeft = { badRequest(it.error.name) }, ifRight = { created(CreateHttpResponse(it)) })

    @PatchMapping("/{id}/vote")
    fun vote(@RequestBody request: VoteHttpRequest, @PathVariable id: UUID) =
        vote(id, request.candidateId, request.voterId)
            .fold(ifLeft = { badRequest(it.error.name) }, ifRight = { noContent() })

    @PatchMapping("/{id}/close")
    fun closeVotingSession(@PathVariable id: UUID) =
        close(id).fold(ifLeft = { badRequest(it.error.name) }, ifRight = { noContent() })

    @GetMapping("/{id}/winners")
    fun getWinners(@PathVariable id: UUID) =
        repository.find(id).winners()
            .fold(ifLeft = { badRequest(it.error.name) }, ifRight = { ok(WinnersHttpResponse(it)) })

    private fun badRequest(error: String) = badRequest().body(HttpErrorResponse(error))

    private fun <T> created(body: T) = status(CREATED).body(body)

    private fun <T> noContent() = ResponseEntity.noContent().build<T>()
}

data class CreateHttpRequest(val name: String, val candidates: List<UUID>)

data class CreateHttpResponse(val id: UUID)

data class HttpErrorResponse(val error: String)

data class VoteHttpRequest(val candidateId: UUID, val voterId: UUID)

data class WinnersHttpResponse(val winners: List<UUID>)
