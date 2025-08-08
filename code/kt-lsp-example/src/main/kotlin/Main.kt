import java.nio.file.Paths
import kotlin.system.exitProcess

fun main() {
    val client = KotlinLSPClient()
    val root = Paths.get("test").toAbsolutePath().takeIf { it.toFile().exists() }?.toUri()
        ?: throw RuntimeException("Cannot retrieve test file")

    listOf(
        { client.init(root.path).get() },
        { client.openDocument(root.resolve("src/main/kotlin/Main.kt")) },
        { client.changeDocument(root.resolve("src/main/kotlin/Main.kt"), "fun main() {\n    println(\"Hello World!\")\n}") },
        { client.closeDocument(root.resolve("src/main/kotlin/Main.kt")) },
        { client.shutdown().thenRun { client.exit() } },
    ).runAllDelayed(1000)
    exitProcess(0)
}

fun List<() -> Any>.runAllDelayed(delay: Long) =
    forEach { it(); Thread.sleep(delay) }