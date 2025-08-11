import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
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
    completions.apply { right?.items?.forEachIndexed { idx, it ->
        println("""
    - (${idx + 1}). ${it.label} (${it.kind}), type: ${it.labelDetails.description}
        - found in ${it.labelDetails.detail}
        - sorted by ${it.sortText}
    """.trimIndent()) } }
}

private fun testHover() {
    val hoverResult = client.hover(sourcesPath.resolve("Main.kt"), Position(13, 12)).get() // Hover over `println`
    println(" >>> Hover result over `println`:")
    println(hoverResult?.contents?.right?.value)
}

private fun testReferences() {
    val references = client.findReferences(sourcesPath.resolve("Main.kt"), Position(13, 12)).get()
        ?.filter { it.uri.startsWith("file") }
    println(" >>> Found ${references?.size ?: 0} references for `println`:")
    references?.forEach { println("- ${it.uri} ${it.range.prettyPrint()}") }
}

fun main() {
    listOf(
        { client.init(root.path).get() },
        { client.openDocument( sourcesPath.resolve("Main.kt")) },
        { testHover() },
        { testReferences() },
        { testCompletions() },
        { client.closeDocument(sourcesPath.resolve("Main.kt")) },
        { client.shutdown().thenRun { client.exit() } },
    ).runAllDelayed(1000)
    exitProcess(0)
}

fun List<() -> Any?>.runAllDelayed(delay: Long) =
    forEach { it(); Thread.sleep(delay) }

fun Range.prettyPrint() = "From l:${start.line} c:${start.character} to l:${end.line} c:${end.character}"