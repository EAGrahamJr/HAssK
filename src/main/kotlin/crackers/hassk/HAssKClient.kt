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

/**
 * Data about an entity's current state.
 *
 * @property entityId ibid
 * @property state the state, based on the entity
 * @property changed when it was changed on HA
 */
data class EntityState(val entityId: String, val state: String, val changed: ZonedDateTime)

/**
 * DSL constants
 */
object Constants {
    const val on = true
    const val off = false
}

/**
 * A very simple [HomeAssistant](https://www.home-assistant.io/) REST API client with minimal functionality.
 *
 * All requests are multithreaded by the underlying HTTP client. All calls **must** be invoked within the "context" of
 * a client instance for an `entityId`. See the docs for the actions on how to invoke them.
 *
 * @param token a "user token" to authorize with HA
 * @param haServer where HA is running
 * @param haPort the port (default sto `8123`
 * @property serverUri the location for the API on HA
 */
open class HAssKClient(val token: String, haServer: String, haPort: Int = 8123) {
    val serverUri = "http://$haServer:$haPort/api"
    protected val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()
    protected val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Set up generic service call that uses the entity ID as the payload.
     *
     * @param entityId the id
     * @param serviceType which service
     * @param serviceCommand the command
     */
    fun callService(entityId: String, serviceType: String, serviceCommand: String): String {
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
     *
     * @param entityId the id
     */
    fun getState(entityId: String): EntityState {
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
    protected fun parseState(stateResponse: JSONObject) = EntityState(
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

    protected val requestPayloadHandler = HttpResponse.BodyHandlers.ofString()

    /**
     * Off we go... (call [startRequest] to kick things off)
     */
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
     *
     * ```kotlin
     * with(haCliewnt) {
     *     light("foo") turn on
     *     group("bar") turn off
     * }
     * ```
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

    /**
     * Basic thing
     *
     * @property entityId everything should have a unique ID
     */
    interface Entity {
        val entityId: String
    }

    /**
     * A light
     *
     * @property isGroup group of lights (or not)
     */
    class Light(name: String, val isGroup: Boolean = false) : Entity {
        override val entityId = "light.$name"
    }

    /**
     * Pre-determined group of entities at certain states.
     */
    class Scene(name: String) : Entity {
        override val entityId = "scene.$name"
    }
}
