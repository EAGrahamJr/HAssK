/*
 * Copyright 2022-2023 by E. A. Graham, Jr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

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
data class EntityState(
    val entityId: String,
    val state: String,
    val changed: ZonedDateTime,
    val attributes: String? = null
)

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
                logger.debug("Request: $it")
                logger.debug("Payload: $payload")
            }
        return sendIt(request)
    }

    /**
     * Request the state of a thing.
     *
     * @param entityId the id
     */
    fun getState(entityId: String): EntityState {
        val uri = URI.create("$serverUri/states/$entityId")
        val request = startRequest(uri)
            .GET()
            .build()
        return sendIt(request).let {
            logger.debug("Sending $it")
            parseState(JSONObject(it))
        }
    }

    /**
     * Ibid.
     */
    protected fun parseState(stateResponse: JSONObject) = EntityState(
        stateResponse.getString("entity_id"),
        stateResponse.getString("state"),
        ZonedDateTime.parse(stateResponse.getString("last_changed")),
        if (stateResponse.has("attributes")) stateResponse.getJSONObject("attributes").toString() else null
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
                logger.debug("Response: $it")
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

    /**
     * Retrieves the state of the entity.
     */
    fun Entity.state() = getState(entityId)

    /**
     * Create an entity in the "light" domain (do **not** prefix with "light.")
     *
     * @param name the name
     * @return a [Light]
     */
    fun light(name: String) = Light(name)

    /**
     * Create a group in the "light" domain (do **not** prefix with "light.")
     *
     * @param name the name
     * @return a [Light] that has `isGroup == true`
     */
    fun group(name: String) = Light(name, true)

    /**
     * Create an entity in the "scene" domain (do **not** prefix with "scene.")
     *
     * @param name the name
     * @return a [Scene]
     */
    fun scene(name: String) = Scene(name)

    /**
     * Create an entity in the "switch" domain (do **not** prefix with "switch.")
     *
     * @param name the name
     * @return a [Switch]
     */
    fun switch(name: String) = Switch(name)

    /**
     * Create an entity in the "sensor" domain (do **not** prefix with "sensor.")
     *
     * @param name the name
     * @return a [Sensor]
     */
    fun sensor(name: String) = Sensor(name)

    /**
     * Basic "thing".
     *
     * @property entityId everything should have a unique ID
     */
    interface Entity {
        val entityId: String
    }

    /**
     * A light-type entity.
     *
     * This may be a single light or a group, acting as a single item.
     *
     * @property isGroup group of lights (or not)
     */
    class Light(name: String, val isGroup: Boolean = false) : Entity {
        override val entityId = "light.$name"
    }

    /**
     * A pre-determined group of entities at certain states.
     */
    class Scene(name: String) : Entity {
        override val entityId = "scene.$name"
    }

    /**
     * A switch-type entity.
     */
    class Switch(name: String) : Entity {
        override val entityId = "switch.$name"
    }

    /**
     * A sensor entity. **NOTE** Sensors are "read-only" and will not generally respond to commands.
     */
    class Sensor(name: String) : Entity {
        override val entityId = "sensor.$name"
    }
}
