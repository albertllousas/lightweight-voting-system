package voting.acceptance

import io.restassured.RestAssured
import org.hamcrest.CoreMatchers.hasItems
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID

@Tag("acceptance")
class VotingSessionLifecycleAcceptanceTest : BaseAcceptanceTest() {

    @Test
    fun `should complete a voting session lifecycle`() {
        val fstCandidate = UUID.randomUUID()
        val sndCandidate = UUID.randomUUID()
        val newVotingSessionId =
            RestAssured
                .given()
                .body(""" { "name": "Voting Session", "candidates": ["$fstCandidate", "$sndCandidate"] } """)
                .contentType("application/json")
                .accept("application/json")
                .port(servicePort)
                .`when`()
                .post("/voting-sessions")
                .then()
                .log().all()
                .assertThat().statusCode(201)
                .extract().jsonPath().getString("id")

        RestAssured
            .given()
            .body(""" { "candidateId": "$fstCandidate", "voterId": "${UUID.randomUUID()}" } """)
            .contentType("application/json")
            .port(servicePort)
            .`when`()
            .patch("/voting-sessions/$newVotingSessionId/vote")
            .then()
            .log().all()
            .assertThat().statusCode(204)

        RestAssured
            .given()
            .contentType("application/json")
            .port(servicePort)
            .`when`()
            .patch("/voting-sessions/$newVotingSessionId/close")
            .then()
            .log().all()
            .assertThat().statusCode(204)

        RestAssured
            .given()
            .accept("application/json")
            .port(servicePort)
            .`when`()
            .get("/voting-sessions/$newVotingSessionId/winners")
            .then()
            .log().all()
            .assertThat().statusCode(200)
            .assertThat().body("winners", hasItems(fstCandidate.toString()))
    }
}
