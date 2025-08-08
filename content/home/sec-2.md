+++
title = "Language Server Protocol - Presentation"
outputs = ["Reveal"]
+++

## A Simple Kotlin Example
(Leveraging kotlin-lsp and LSP4J)

---

### Server Connection
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

---

### Registering a Workspace
A Workspace is the location of a Kotlin project. As of today, kotlin-lsp (https://github.com/Kotlin/kotlin-lsp) supports Gradle Kotlin/JVM projects only, meaning that
valid worksapces are only kotlin projects with such limitations. Due to this we could not use it with standalone files.

In order to register a workspace, we must spcify project's `URI` and attach it to the initial request to be sent to the server.

```kt
projectFolders = listOf(WorkspaceFolder("file://$projectPath", projectName))
val params = InitializeParams().apply { workspaceFolders = projectFolders }
```

---

### Initializing the Language Server
Once we succesfully launch a client instance, we must start the initialization procedure before any other requst. In this phase
we specify the `WorkspaceFolder` and `ClientCapabilities` (later), upon which the server will respond with his capabilities.

The initialization procedure can end only when, upon receiving server's
response, the client sends an `initialized` *notification* in order to signal
that it's ready state.

```kt
fun initialize(cc: ClientCapabilties, wf: Lisit<WorkSpaceFolder>): Future<Void> {
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



---
{{% section %}}

### Registering Client Capabilties
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

---

#### Registering Client Capabilties (2)

`Completion` capabilties are under the `textDocumuent` set of capabilties, along with others like `Hover`, `Signature`, `Definition`, etc.

```kt
val txtDocCap = TextDocumentCapabilties().apply {
  // set completion configurations (could use also have used `apply` here too)
  val cmplCap = CompletionCapabilties()
  cmplCap.setContextSupport(true)
  cmplCap.setCompletionItem(CompletionItemCapabilties(snippetSupport = true))
  // cmplCap.set...

  completion = cmplCap
}
val capabilties = CompletionCapabilties.apply {
  textDocument = txtDocCap
}
```

{{% /section %}}

---
