import org.eclipse.lsp4j.Position
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main() {
    val client = KotlinLSPClient()
    val root = Paths.get("test").toAbsolutePath().takeIf { it.toFile().exists() }?.toUri()
        ?: throw RuntimeException("Cannot retrieve test file")
    val sourcesPath = root.resolve("src/main/kotlin/")

    listOf(
        { client.init(root.path).get() },
        { client.openDocument( sourcesPath.resolve("Main.kt")) },
        {
            val completions = client.getCompletion(sourcesPath.resolve("CompleteMe.kt"), Position(1, 12)).get()
            println(" >>> Found ${completions.right?.items?.size ?: 0} completions:")
            completions.apply { right?.items?.forEach { println("- ${it.label} (${it.kind}) - ${it.labelDetails}") } }
        },
        {
            val hoverResult = client.hover(sourcesPath.resolve("Main.kt"), Position(13, 12)).get() // Hover over `println`
            println(" >>> Hover result:")
            println(hoverResult?.contents?.right)
        },
        {
            val references = client.findReferences(sourcesPath.resolve("Main.kt"), Position(13, 12)).get()
                ?.filter { it.uri.startsWith("file") }
            println(" >>> Found ${references?.size ?: 0} references:")
            references?.forEach { println("- ${it.uri} ${it.range}") }
        },
        { client.changeDocument(sourcesPath.resolve("Main.kt"), "fun main() {\n    println(\"Hello World!\")\n}") },
        { client.closeDocument(sourcesPath.resolve("Main.kt")) },
        { client.shutdown().thenRun { client.exit() } },
    ).runAllDelayed(1000)
    exitProcess(0)
}

fun List<() -> Any?>.runAllDelayed(delay: Long) =
    forEach { it(); Thread.sleep(delay) }