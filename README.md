# compose-perf-tracer

A lightweight Jetpack Compose recomposition tracker that surfaces unnecessary recompositions in debug builds.

---

## Installation

Add the dependency to your module's `build.gradle`:

```groovy
dependencies {
    debugImplementation 'io.github.compose-perf-tracer:compose-perf-tracer:1.0.0'
}
```

---

## Usage

Wrap your composable with `TrackedComposable` to monitor recomposition counts:

```kotlin
import com.composeperf.tracer.TrackedComposable

@Composable
fun MyScreen() {
    TrackedComposable(tag = "MyScreen") {
        // Your existing composable content
        Text(text = "Hello, World!")
        MyButton()
    }
}
```

Recomposition events are logged to Logcat under the tag `ComposePerfTracer`:

```
D/ComposePerfTracer: [MyScreen] recomposed 3 times — 2 flagged as unnecessary
```

You can also enable a global overlay to visualize recomposition counts directly on screen:

```kotlin
// In your Application or debug setup
ComposePerfTracer.enableOverlay(enabled = true)
```

> **Note:** `compose-perf-tracer` is a no-op in release builds. No code or overhead is included in production.

---

## Requirements

- Android API 21+
- Jetpack Compose 1.4.0+
- Kotlin 1.8+

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

---

## License

[MIT](LICENSE) © compose-perf-tracer contributors