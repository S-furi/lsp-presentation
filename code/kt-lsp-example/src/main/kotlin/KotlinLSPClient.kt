import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.CompletionCapabilities
import org.eclipse.lsp4j.CompletionContext
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemCapabilities
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.CompletionTriggerKind
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentClientCapabilities
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceClientCapabilities
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageServer
import java.net.Socket
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class KotlinLSPClient {
    private val socket = Socket("127.0.0.1", 9999)
    private val languageClient = KotlinLanguageClient()
    private val languageServer: LanguageServer = getRemoteLanguageServer()
    private lateinit var stopFuture: Future<Void>

    typealias Completions = Either<List<CompletionItem>, CompletionList>

    fun init(kotlinProjectRoot: String, projectName: String = "None"): Future<Void> {
        val capabilities = getCompletionCapabilities()
        val workspaceFolders = listOf(WorkspaceFolder("file://$kotlinProjectRoot", projectName))

        val params = InitializeParams().apply {
            this.capabilities = capabilities
            this.workspaceFolders = workspaceFolders
        }

        return languageServer.initialize(params)
            .thenCompose { res ->
                println(">>> Initialization response from server:\n$res")
                languageServer.initialized(InitializedParams())
                CompletableFuture.completedFuture(null)
            }
    }

    fun getCompletion(
        uri: URI,
        position: Position,
        triggerKind: CompletionTriggerKind = CompletionTriggerKind.Invoked
    ): Future<Completions> {
        val context = CompletionContext(triggerKind)
        val params = CompletionParams(TextDocumentIdentifier(uri.toString()), position, context)
        return languageServer.textDocumentService.completion(params)
            ?: CompletableFuture.completedFuture(Either.forLeft(emptyList()))
    }

    fun openDocument(uri: URI) {
        val content = Files.readString(Paths.get(uri))
        val params = DidOpenTextDocumentParams(
            TextDocumentItem(uri.toString(), "kotlin", 1, content)
        )
        languageServer.textDocumentService.didOpen(params)
    }

    /**
     * Changes are not considered incremental, the whole document is replaced.
     */
    fun changeDocument(uri: URI, newContent: String) {
        val params = DidChangeTextDocumentParams(
            VersionedTextDocumentIdentifier(uri.toString(), 1),
            listOf(TextDocumentContentChangeEvent(newContent)),
        )
        languageServer.textDocumentService.didChange(params)
    }

    fun closeDocument(uri: URI) {
        val params = DidCloseTextDocumentParams(TextDocumentIdentifier(uri.toString()))
        languageServer.textDocumentService.didClose(params)
    }


    fun shutdown(): CompletableFuture<Any> = languageServer.shutdown()

    fun exit() {
        languageServer.exit()
        stopFuture.cancel(true)
        socket.close()
    }

    private fun getCompletionCapabilities() = ClientCapabilities().apply {
        textDocument = TextDocumentClientCapabilities().apply {
            completion =
                CompletionCapabilities(
                    CompletionItemCapabilities(true)
                ).apply { contextSupport = true }
        }
        workspace = WorkspaceClientCapabilities().apply { workspaceFolders = true }
    }

    private fun getRemoteLanguageServer(): LanguageServer{
        val input = socket.getInputStream()
        val output = socket.getOutputStream()
        val launcher = LSPLauncher.createClientLauncher(languageClient, input, output)
        stopFuture = launcher.startListening()
        return launcher?.remoteProxy ?: throw RuntimeException("Cannot connect to server")
    }
}