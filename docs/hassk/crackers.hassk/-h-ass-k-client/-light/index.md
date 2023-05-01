//[hassk](../../../../index.md)/[crackers.hassk](../../index.md)/[HAssKClient](../index.md)/[Light](index.md)

# Light

[jvm]\
class [Light](index.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val isGroup: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false) : [HAssKClient.Entity](../-entity/index.md)

A light-type entity.

This may be a single light or a group, acting as a single item.

## Constructors

| | |
|---|---|
| [Light](-light.md) | [jvm]<br>constructor(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), isGroup: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false) |

## Properties

| Name | Summary |
|---|---|
| [entityId](entity-id.md) | [jvm]<br>open override val [entityId](entity-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>everything should have a unique ID |
| [isGroup](is-group.md) | [jvm]<br>val [isGroup](is-group.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false<br>group of lights (or not) |
