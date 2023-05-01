//[hassk](../../../index.md)/[crackers.hassk](../index.md)/[HAssKClient](index.md)

# HAssKClient

open class [HAssKClient](index.md)(val token: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), haServer: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), haPort: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 8123)

A very simple [HomeAssistant](https://www.home-assistant.io/) REST API client with minimal functionality.

All requests are multithreaded by the underlying HTTP client. All calls **must** be invoked within the &quot;context&quot; of a client instance for an `entityId`. See the docs for the actions on how to invoke them.

#### Parameters

jvm

| | |
|---|---|
| token | a &quot;user token&quot; to authorize with HA |
| haServer | where HA is running |
| haPort | the port (default sto `8123` |

## Constructors

| | |
|---|---|
| [HAssKClient](-h-ass-k-client.md) | [jvm]<br>constructor(token: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), haServer: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), haPort: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 8123) |

## Types

| Name | Summary |
|---|---|
| [Entity](-entity/index.md) | [jvm]<br>interface [Entity](-entity/index.md)<br>Basic &quot;thing&quot;. |
| [Light](-light/index.md) | [jvm]<br>class [Light](-light/index.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val isGroup: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false) : [HAssKClient.Entity](-entity/index.md)<br>A light-type entity. |
| [Scene](-scene/index.md) | [jvm]<br>class [Scene](-scene/index.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [HAssKClient.Entity](-entity/index.md)<br>A pre-determined group of entities at certain states. |

## Functions

| Name | Summary |
|---|---|
| [callService](call-service.md) | [jvm]<br>fun [callService](call-service.md)(entityId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), serviceType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), serviceCommand: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Set up generic service call that uses the entity ID as the payload. |
| [getState](get-state.md) | [jvm]<br>fun [getState](get-state.md)(entityId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [EntityState](../-entity-state/index.md)<br>Request the state of a thing. |
| [group](group.md) | [jvm]<br>fun [group](group.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [HAssKClient.Light](-light/index.md)<br>Create a group in the &quot;light&quot; domain (do **not** prefix with &quot;light.&quot;) |
| [light](light.md) | [jvm]<br>fun [light](light.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [HAssKClient.Light](-light/index.md)<br>Create an entity in the &quot;light&quot; domain (do **not** prefix with &quot;light.&quot;) |
| [scene](scene.md) | [jvm]<br>fun [scene](scene.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [HAssKClient.Scene](-scene/index.md)<br>Create an entity in the &quot;scene&quot; domain (do **not** prefix with &quot;scene.&quot;) |
| [sendIt](send-it.md) | [jvm]<br>fun [sendIt](send-it.md)(request: HttpRequest): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Off we go... (call [startRequest](start-request.md) to kick things off) |
| [startRequest](start-request.md) | [jvm]<br>fun [startRequest](start-request.md)(uri: [URI](https://docs.oracle.com/javase/8/docs/api/java/net/URI.html)): HttpRequest.Builder<br>Set up the basic request, including auth. |
| [state](state.md) | [jvm]<br>fun [HAssKClient.Entity](-entity/index.md).[state](state.md)(): [EntityState](../-entity-state/index.md)<br>Retrieves the state of the entity. |
| [turn](turn.md) | [jvm]<br>infix fun [HAssKClient.Entity](-entity/index.md).[turn](turn.md)(on: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[EntityState](../-entity-state/index.md)&gt;<br>Generic turn on/off |

## Properties

| Name | Summary |
|---|---|
| [serverUri](server-uri.md) | [jvm]<br>val [serverUri](server-uri.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>the location for the API on HA |
| [token](token.md) | [jvm]<br>val [token](token.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
