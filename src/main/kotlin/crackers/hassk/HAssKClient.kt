package crackers.hassk

import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

/**
 * Very simple REST API client with minimal functionality.
 */
class HAssKClient(val token: String, haServer: String, haPort: Int = 8123)  {
    val serverUri = "http://$haServer:$haPort/api"
    val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun sendLight(name:String, on:Boolean) {
        val payload = """
            {"entity_id":"light.$name"}
        """.trimIndent()

        val uri = URI.create("$serverUri/services/light/turn_${if (on) "on" else "off"}")
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()
        val response = client.send(request, BodyHandlers.ofString()).also {
            if (it.statusCode() != 200) logger.error("Error response: ${it.statusCode()}")
        }
//        logger.info("The response:\n{}", response)
    }

    infix fun String.light(on: Boolean) = sendLight(this, on)
}

val token = """
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJmZDE0ODI0ZGJhZWQ0MjIzYjc5ZjEyNWVmOWZmNjFmMCIsImlhdCI6MTU1NDU3NjMwOCwiZXhwIjoxODY5OTM2MzA4fQ.xCTiigyRQJH42w2CSEkjyTraM9ULXEin0cKjaUwCW3M    
""".trimIndent()
