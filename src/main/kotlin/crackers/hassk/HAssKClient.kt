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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.ZonedDateTime
import kotlin.math.roundToInt

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
 * Media things
 */
object MediaConstants {
    const val SPOTIFY = "Spotify"
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
open class HAssKClient(private val token: String, haServer: String, haPort: Int = 8123) {
    protected val serverUri = "http://$haServer:$haPort/api"
    protected val client: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()
    protected val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    /**
     * Set up generic service call that uses the entity ID as the payload.
     *
     * @param entityId the id
     * @param serviceType which service
     * @param serviceCommand the command
     */
    fun callService(
        entityId: String,
        serviceType: String,
        serviceCommand: String,
        extraData: Map<String, Any> = emptyMap()
    ): String {
        val payload = JSONObject().apply {
            put("entity_id", entityId)
            extraData.forEach { (k, v) -> put(k, v) }
        }.toString()
        val uri = URI.create("$serverUri/services/$serviceType/$serviceCommand")
        val request = startRequest(uri)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build().also {
                logger.debug("Request: {}", uri)
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
            logger.debug("Receiving: $it")
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
     * with(haClient) {
     *     light("foo") turn on
     *     group("bar") turn off
     * }
     * ```
     */
    infix fun Entity.turn(on: Boolean): List<EntityState> {
        val response = callService(entityId, "homeassistant", "turn_${if (on) "on" else "off"}")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    /**
     * Set a light level for a light/group.
     * @param level 0-100
     *
     * ```kotlin
     * with(haClient) {
     *    light("foo") set 50
     *    group("bar") set 0
     * }
     * ```
     */
    infix fun Light.set(level: Int): List<EntityState> {
        if (level <= 0) return turn(Constants.off)

        val actual = (level * 255f / 100).roundToInt().coerceIn(1, 255)
        val brightness = mapOf("brightness" to actual)
        val response = callService(entityId, "homeassistant", "turn_on", brightness)
        return JSONArray(response).map { parseState(it as JSONObject) }
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
     * Create an entity in the "media_player" domain ( do **not** prefix with "media_player.")
     *
     * Specialized players are also initialized here (e.g. Spotify)
     */
    fun media(name: String) = when {
        name.equals(MediaConstants.SPOTIFY, true) -> SpotifyPlayer(name)
        else -> MediaPlayer(name)
    }

    /**
     * Create an entity in the "sensor" domain (do **not** prefix with "sensor.")
     *
     * @param name the name
     * @return a [Sensor]
     */
    fun sensor(name: String) = Sensor(name)

    /**
     * Uses the state JSON API to get a list of entities and their states. It is filtered by the optional [domain].
     */
    fun states(domain: String? = null): List<EntityState> {
        val response = sendIt(startRequest(URI.create("$serverUri/states")).build())
        return JSONArray(response)
            .map { parseState(it as JSONObject) }
            .filter { domain == null || it.entityId.startsWith("$domain.") }
    }

    /**
     * Basic "thing".
     *
     * @property entityId everything should have a unique ID
     */
    interface Entity {
        val entityId: String
        val domain: String
    }

    /**
     * A light-type entity.
     *
     * This may be a single light or a group, acting as a single item.
     *
     * @property isGroup group of lights (or not)
     */
    class Light(name: String, val isGroup: Boolean = false) : Entity {
        override val domain = "light"
        override val entityId = "$domain.$name"
    }

    /**
     * A pre-determined group of entities at certain states.
     */
    class Scene(name: String) : Entity {
        override val domain = "scene"
        override val entityId = "$domain.$name"
    }

    /**
     * A switch-type entity.
     */
    class Switch(name: String) : Entity {
        override val domain = "switch"
        override val entityId = "$domain.$name"
    }

    /**
     * A sensor entity. **NOTE** Sensors are "read-only" and will not generally respond to commands.
     */
    class Sensor(name: String) : Entity {
        override val domain = "sensor"
        override val entityId = "$domain.$name"
    }

    open class MediaPlayer(name: String) : Entity {
        final override val domain = "media_player"
        override val entityId = "$domain.$name"
    }

    class SpotifyPlayer(name: String) : MediaPlayer(name) {
        var currentPlayer: String = "None"
    }

    fun MediaPlayer.pause(): List<EntityState> {
        val response = callService(entityId, domain, "media_pause")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    fun MediaPlayer.play(): List<EntityState> {
        val response = callService(entityId, domain, "media_play")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    fun SpotifyPlayer.play(player: String? = null): List<EntityState> {
        if (player != null) currentPlayer = player
        if (currentPlayer == "None") throw IllegalStateException("Current player must be set before invoking play")

        callService(entityId, domain, "select_source", mapOf("source" to currentPlayer))
        val response = callService(entityId, domain, "media_play")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    fun MediaPlayer.stop(): List<EntityState> {
        val response = callService(entityId, domain, "media_stop")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    fun MediaPlayer.next(): List<EntityState> {
        val response = callService(entityId, domain, "media_next_track")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    fun MediaPlayer.previous(): List<EntityState> {
        val response = callService(entityId, domain, "media_previous_track")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    fun MediaPlayer.volumeUp(): List<EntityState> {
        val response = callService(entityId, domain, "volume_up")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    fun MediaPlayer.volumeDown(): List<EntityState> {
        val response = callService(entityId, "homeassistant", "volume_down")
        return JSONArray(response).map { parseState(it as JSONObject) }
    }

    operator fun MediaPlayer.unaryPlus() = volumeUp()
    operator fun MediaPlayer.unaryMinus() = volumeDown()
}
