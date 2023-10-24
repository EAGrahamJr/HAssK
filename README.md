# HomeAssistant Client for Kotlin

- Extremely simple API with some DSL goodies
- Requires Java 11<sup>**1**</sup> for included HttpClient stuff
  - uses _async_ under the covers, so multi-threading is provided out-of-the-box
- Minimal external libraries (`SLF4J`, `org.json`)

![Just Build](https://github.com/EAGrahamJr/HAssK/actions/workflows/build.yaml/badge.svg) ![Kotlin](https://badgen.net/badge/Kotlin/1.9,0/purple)  ![Java](https://badgen.net/badge/Java/17/orange) ![Apache License](https://badgen.net/github/license/EAGrahamJr/HAssK)

[Javadocs](https://eagrahamjr.github.io/HAssK)

## Examples

```kotlin
import crackers.hassk.HAssKClient
import crackers.hassk.Constants.off
import crackers.hassk.Constants.on

private val token = "valid.ha.token"
fun main() {
  with(HAssKClient(token, "local name or ip address")) {
    val light = light("shelf_lamp")
    println(light.state())

    val group = light("bedroom_group")
    group turn on
    Thread.sleep(2000)
    group turn off
    Thread.sleep(2000)
  }
}
```

## Building

This project uses [Gradle](https://gradle.org), so the only thing you need is a compatible JDK<sup>**1**</sup>. Additionally, because the project is [Kotlin](https://kotlinlang.org) and uses the _Kotlin Gradle plugin_, a Kotlin installation is also not necessary.

A default build will use the [gradle-plugins](https://github.com/EAGrahamJr/gradle-scripts) to publish to the "local" Maven repository.

[Documentation](docs) is created via the `dokka` plugin: Javadocs **are** created on build (but not published, yet).

---

<sup>**1**</sup>Java 17 is the current build target.
