+++
title = "Language Server Protocol - Presentation"
outputs = ["Reveal"]
+++

## A Simple Kotlin Example
(Leveraging kotlin-lsp and LSP4J)

---

### Server Connection

<div style="text-align: justify">

Accomplished by means of Sockets input and output streams

```kt
val socket = Socket("127.0.0.1", 9999)

val launcher =
  LSPLauncher.createClientLauncher(
    languageClient,
    socket.inputStream,
    socket.outputStream
  )

launcher.startListening()
val lspServer = launcher.remoteProxy
```

</div>

---

### Registering a Workspace

<div style="text-align: justify">

A Workspace is the location of a Kotlin project. As of today, kotlin-lsp
(https://github.com/Kotlin/kotlin-lsp) supports Gradle Kotlin/JVM projects only,
meaning that valid workspaces are only kotlin projects with such limitations.
Due to this we could not use it with standalone files.

In order to register a workspace, we must specify project's `URI` and attach it to the initial request to be sent to the server.

```kt
projectFolders = listOf(WorkspaceFolder("file://$projectPath", projectName))
val params = InitializeParams().apply { workspaceFolders = projectFolders }
```

</div>

---

### Initializing the Language Server

<div style="text-align: justify">

Once we successfully launch a client instance, we must start the initialization procedure before any other request. In this phase
we specify the `WorkspaceFolder` and `ClientCapabilities` (later), upon which the server will respond with his capabilities.

The initialization procedure can end only when, upon receiving server's
response, the client sends an `initialized` *notification* in order to signal
that it's ready state.

```kt
fun initialize(cc: ClientCapabilities, wf: List<WorkSpaceFolder>): Future<Void> {
  val params = InitializeParams().apply {
    capabilities = cc
    workspaceFolders = wf
  }
  return languageServer.initialize(params).thenCompose {
    languageServer.initialized()
    CompletableFuture.complete(null)
  }
}
```

</div>

---
{{% section %}}

### Registering Client Capabilities

<div style="text-align: justify">

Client capabilities defines which operations will be available during client-server session. For this project, the most important capability is `Completion`.

```ts
interface CompletionClientCapabilities {
	dynamicRegistration?: boolean;
	completionItem?: {
		snippetSupport?: boolean;
		commitCharactersSupport?: boolean;
		documentationFormat?: MarkupKind[];
		deprecatedSupport?: boolean;
		preselectSupport?: boolean;
		tagSupport?: { valueSet: CompletionItemTag[]; };
		insertReplaceSupport?: boolean;
		resolveSupport?: { properties: string[]; };
		insertTextModeSupport?: { valueSet: InsertTextMode[]; };
		labelDetailsSupport?: boolean;
	};
	completionItemKind?: { valueSet?: CompletionItemKind[]; };
	contextSupport?: boolean;
	insertTextMode?: InsertTextMode;
	completionList?: { itemDefaults?: string[]; }
}
```


A lot of configurations... mostly related on how the editor **shows** and **insert** received completions options.

</div>

---

### Registering Client Capabilities (2)

<div style="text-align: justify">

`Completion` capabilities are under the `textDocument` set of capabilities, along with others like `Hover`, `Signature`, `Definition`, etc.

```kt
val txtDocCap = TextDocumentCapabilities().apply {
  // set completion configurations (could use also have used `apply` here too)
  val cmplCap = CompletionCapabilities()
  cmplCap.setContextSupport(true)
  cmplCap.setCompletionItem(CompletionItemCapabilities(snippetSupport = true))
  // cmplCap.set...

  completion = cmplCap
}
val capabilities = CompletionCapabilities.apply {
  textDocument = txtDocCap
}
```

</div>

{{% /section %}}

---

{{% section %}}

### Document Synchronization
<div style="text-align: justify">

In order to make the editor signals the language server to:

- start tracking a file just opened in the editor
- register changes made by the user
- stop tracking the opened file

we must use the methods `didOpen`, `didChange` and `didClose` respectively.


To keep track of files and their contents, we use `TextDocumentIdentifier`:
simply a container for their URI and a version number if clients support it
(not necessarily incremental).

</div>

---

#### Document Opening
<div style="text-align: justify">


We create an instance of `TextDocumentItem` with its URI, version number, language id and it's content.
By sending the notification `textDocumentService/didOpen`, the server will now start tracking and synching the document.

```kt
val content = Files.readString(Paths.get(uri))
val params = DidOpenTextDocumentParams(
    TextDocumentItem(uri.toString(), "kotlin", 1, content)
)
languageServer.textDocumentService.didOpen(params)
```
</div>

---

#### Document Changing

<div style="text-align: justify">


Two different approaches in informing the server about changes: **incremental**, meaning that we send *diffs* (new contents and their positions), or **full** where the entire, updated document content is sent to the server. In the latter case, we could simply do:

```kt
fun changeDocument(uri: URI, newContent: String) {
    val params = DidChangeTextDocumentParams(
        VersionedTextDocumentIdentifier(uri.toString(), 1),
        listOf(TextDocumentContentChangeEvent(newContent)),
    )
    languageServer.textDocumentService.didChange(params)
}
```

Changes are represented as a list of `TexDocumentContentChangeEvent`: such events can define insertions/modifications/deletions in case the strategy for updating the fil is `incremental` (which must set in initial client capabilities, otherwise `full` is the default).

</div>

---

#### Document Closing

<div style="text-align: justify">

The closing notification just simply requires the URI of the document

```kt
val params = DidCloseTextDocumentParams(TextDocumentIdentifier(uri.toString()))
languageServer.textDocumentService.didClose(params)
```

</div>

{{% /section %}}

---

### Recap Scheme

<img src="img/lsp-user-editor-server-interactions.png" height=850>

---

### `documentText/Completion`

<div style="text-align: justify">
Completions can be triggered by 3 main actions inside the editor:
1. By pressing certain characters (e.g. '`.`', '`(`')
2. Explicit invocation (i.e. `<ctrl> + <space>`)
3. When the current completions is incomplete.

When one of these three events occur, we can trigger a completion specifying the URI of the file and the cursor line and character position,
and retrieve a result of type `List<CompletionItem>` or `CompletionList` depending on whether current completion is incomplete or not.

```kt
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
```

</div>
