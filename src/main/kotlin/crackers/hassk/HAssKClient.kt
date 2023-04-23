package crackers.hassk

import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.ZonedDateTime

data class EntityState(val entityId: String, val state: String, val changed: ZonedDateTime)

object Constants {
    const val on = true
    const val off = false
}

/**
 * Very simple REST API client with minimal functionality. All requsets are multithreaded by the underlying HTTP client.
 * All calls **must** be invoked within the "context" of a client instance for an `entityId`. See the docs for the actions
 * on how to invoke them.
 */
class HAssKClient(val token: String, haServer: String, haPort: Int = 8123) {
    val serverUri = "http://$haServer:$haPort/api"
    val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Set up generic service call that uses entity_id as the only payload
     */
    private fun callService(entityId: String, serviceType: String, serviceCommand: String): String {
        val payload = """{"entity_id":"$entityId"}""".trimIndent()
        val uri = URI.create("$serverUri/services/$serviceType/$serviceCommand")
        val request = startRequest(uri)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build().also {
                logger.info("Request: $it")
                logger.info("Payload: $payload")
            }
        return sendIt(request)
    }

    /**
     * Request the state of a thing
     */
    private fun getState(entityId: String): EntityState {
        val uri = URI.create("$serverUri/states/$entityId")
        val request = startRequest(uri)
            .GET()
            .build()
        return sendIt(request).let {
            logger.info(it)
            parseState(JSONObject(it))
        }
    }

    /**
     * Ibid.
     */
    private fun parseState(stateResponse: JSONObject) = EntityState(
        stateResponse.getString("entity_id"),
        stateResponse.getString("state"),
        ZonedDateTime.parse(stateResponse.getString("last_changed"))
    )

    /**
     * Set up the basic request, including auth.
     */
    fun startRequest(uri: URI) = HttpRequest.newBuilder()
        .uri(uri)
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")

    /**
     * Off we go...
     */
    private val requestPayloadHandler = HttpResponse.BodyHandlers.ofString()
    fun sendIt(request: HttpRequest): String {
        return client.sendAsync(request, requestPayloadHandler).let {
            val response = it.get()
            if (response.statusCode() != 200) {
                logger.error("Error response: ${response.statusCode()}")
                throw IOException("Error from HA - see logs")
            }
            response.body().also {
                logger.info("Response: $it")
            }
        }
    }

    /**
     * Generic turn on/off
     */
    infix fun Entity.turn(on: Boolean): List<EntityState> =
        callService(entityId, "homeassistant", "turn_${if (on) "on" else "off"}").let {
            JSONArray(it).map {
//                val entity = it as JSONObject
//                val isGroup = entity.getJSONObject("attributes").has("entity_id")
                parseState(it as JSONObject)
            }
        }

    fun Entity.state() = getState(entityId)

    /**
     * Create an entity in the "light" domain (do not prefix with "light.")
     */
    fun light(name: String) = Light(name)

    /**
     * Create a group in the "light" domain (do not prefix with "light.")
     */
    fun group(name: String) = Light(name, true)

    /**
     * Create a scene in the "scene" domain (do not prefix with "scene.")
     */
    fun scene(name: String) = Scene(name)

    interface Entity {
        val entityId: String
    }

    class Light(name: String, val isGroup: Boolean = false) : Entity {
        override val entityId = "light.$name"
    }

    class Scene(name: String) : Entity {
        override val entityId = "scene.$name"
    }
}
