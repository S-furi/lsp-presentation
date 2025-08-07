+++
title = "Language Server Protocol - Presentation"
outputs = ["Reveal"]
+++

## Base Protocol

The base protocol consists of a header and a content part (comparable to HTTP).

**Header** *must* contain the `Content-Length` value, plus it can include a `Content-Type`.
If no `Content-Type` is provided, default is `application/vscode-jsonrpc;charset=utf-8`.

**Content part** begins after the header, marked with a double `\r\n` (like HTTP), and uses **`JSON-RPC`** (https://www.jsonrpc.org)
to describe *requests*, *responses* and *notifications*.

```json
Content-Length: ...\r\n
\r\n
{
	"jsonrpc": "2.0",
	"id": 1,
	"method": "textDocument/completion",
	"params": {
		...
	}
}
```


---
{{% section %}}

### JSON-RPC

JSON-RPC is a stateless, lightweight RPC protocol, where an RPC *call* is a JSON object composed as follows:

```json
{
  "jsonrpc": "2.0",
  "method": "nameOfTheMethod",
  "params": { ... },
  "id": "clientDefinedIdentifier"
}
```

---

### JSON-RPC

When a call is made, a *response* is composed of:
```json
{
  "jsonrpc": "2.0",
  "result": Any,
  "error?": {
    "code": integer,
    "message": "errorMsg",
    "data": Any // additional info
  }
  "id": "sameValueAsClientDefinedIdInRequest"
}
```

---

### JSON-RPC

Requests where there is a lack of interest in a response from the sender are called *notifications*. A notification is like a request, but it misses `id` parameter. Special kind of notifications include:

- method `$/cancelRequest`
- method `$/progress`

{{% /section %}}

---

{{% section %}}

### A Simple Kotlin Example (2)

An **`initialize`** message could be simply modelled as the following data class.

```kotlin
@Serializable
data class InitializeMessage(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String = "initialize",
    val params: Map<String, Map<String, String?>?> = mapOf(
      "processId" to null,
      "workspaceFolders" to null,
      "capabilities" to emptyMap()
    )
)

fun buildJSONRPCMessage(msg: InitializeMessage): ByteArray {
    val body = Json.encodeToString(msg)
    return "Content-Length: ${body.length}\r\n\r\n$body".toByteArray(Charsets.UTF_8)
}
```

---

### A Simple Kotlin Example
We can communicate with the language server by means of a simple TCP connection, sending an `initialize` request and receiving server's response </br> (reading 4 Kb for the sake of simplicity)


```kotlin
val initializeMessage = buildJSONRPCMessage(InitializeMessage())
Socket("127.0.0.1", 9999).use {client ->
    client.outputStream.write(initializeMessage)

    val buffer = ByteArray(4096)
    val bytesRead = client.inputStream.read(buffer)
    val response = String(buffer, 0, bytesRead, Charsets.UTF_8)

    assertTrue { response.isNotEmpty() }
    println(response)
}
```

Querying the Kotlin-LSP (https://github.com/Kotlin/kotlin-lsp) returns a result like:

```json
Content-Length: 3462
Content-Type: application/json-rpc; charset=utf-8

{
    "jsonrpc": "2.0",
    "id": 1,
    "result": {
        "capabilities": {
            "textDocumentSync": 2,
            "completionProvider": {
                "kind": "com.jetbrains.lsp.protocol.CompletionRegistrationOptionsImpl",
                "triggerCharacters": [
                    "."
                ...
                ]
          ...
        },
        "serverInfo": {
            "name": "Kotlin LSP by JetBrains",
            "version": "0.1"
        }
    }
}
```


{{% /section %}}

---
