//[hassk](../../index.md)/[crackers.hassk](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [Constants](-constants/index.md) | [jvm]<br>object [Constants](-constants/index.md)<br>DSL constants |
| [EntityState](-entity-state/index.md) | [jvm]<br>data class [EntityState](-entity-state/index.md)(val entityId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val state: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val changed: [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html), val attributes: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null)<br>Data about an entity's current state. |
| [HAssKClient](-h-ass-k-client/index.md) | [jvm]<br>open class [HAssKClient](-h-ass-k-client/index.md)(val token: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), haServer: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), haPort: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 8123)<br>A very simple [HomeAssistant](https://www.home-assistant.io/) REST API client with minimal functionality. |
