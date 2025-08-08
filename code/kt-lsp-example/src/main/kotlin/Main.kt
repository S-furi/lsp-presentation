import org.eclipse.lsp4j.Position
import java.net.URI
import java.nio.file.Paths
import kotlin.system.exitProcess

val client = KotlinLSPClient()
val root = Paths.get("test").toAbsolutePath().takeIf { it.toFile().exists() }?.toUri()
    ?: throw RuntimeException("Cannot retrieve test file")
val sourcesPath: URI = root.resolve("src/main/kotlin/")

private fun testCompletions() {
    val completions = client.getCompletion(sourcesPath.resolve("CompleteMe.kt"), Position(2, 12)).get()
    println(" >>> Found ${completions.right?.items?.size ?: 0} completions:")
    completions.apply { right?.items?.forEach { println("- ${it.label} (${it.kind}) - ${it.labelDetails}") } }
}

private fun testHover() {
    val hoverResult = client.hover(sourcesPath.resolve("Main.kt"), Position(13, 12)).get() // Hover over `println`
    println(" >>> Hover result:")
    println(hoverResult?.contents?.right)
}

private fun testReferences() {
    val references = client.findReferences(sourcesPath.resolve("Main.kt"), Position(13, 12)).get()
        ?.filter { it.uri.startsWith("file") }
    println(" >>> Found ${references?.size ?: 0} references:")
    references?.forEach { println("- ${it.uri} ${it.range}") }
}

private fun testWorkspaceSymbol(query: String) {
    val workspaceSymbols = client.workspaceSymbol(query).get()
    println(" >>> Found the following workspace symbols for query '$query':")
    println(workspaceSymbols?.right?.joinToString("\n") { "- ${it.name} [${it.kind}], (${it.location?.left})" } ?: "No results found.")
}

fun main() {
    listOf(
        { client.init(root.path).get() },
        { client.openDocument( sourcesPath.resolve("Main.kt")) },
        { testCompletions() },
        { testHover() },
        { testReferences() },
        { testWorkspaceSymbol("myWork") },
        { client.closeDocument(sourcesPath.resolve("Main.kt")) },
        { client.shutdown().thenRun { client.exit() } },
    ).runAllDelayed(1000)
    exitProcess(0)
}

fun List<() -> Any?>.runAllDelayed(delay: Long) =
    forEach { it(); Thread.sleep(delay) }