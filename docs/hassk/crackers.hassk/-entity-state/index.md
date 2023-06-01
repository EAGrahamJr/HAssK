//[hassk](../../../index.md)/[crackers.hassk](../index.md)/[EntityState](index.md)

# EntityState

[jvm]\
data class [EntityState](index.md)(val entityId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val state: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val changed: [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html), val attributes: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null)

Data about an entity's current state.

## Constructors

| | |
|---|---|
| [EntityState](-entity-state.md) | [jvm]<br>constructor(entityId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), state: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), changed: [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html), attributes: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [attributes](attributes.md) | [jvm]<br>val [attributes](attributes.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null |
| [changed](changed.md) | [jvm]<br>val [changed](changed.md): [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html)<br>when it was changed on HA |
| [entityId](entity-id.md) | [jvm]<br>val [entityId](entity-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>ibid |
| [state](state.md) | [jvm]<br>val [state](state.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>the state, based on the entity |
