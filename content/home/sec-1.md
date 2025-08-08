+++
title = "Language Server Protocol - Presentation"
outputs = ["Reveal"]
+++
## Why do we need LSP?

* Code editor should provide language support
* Standard approach: a new implementation for each language and editor
* Better approach: language server communicating with editor using standarized protocol

![How it works](img/idea.png)

{{< note >}}
* IDE or simpler editor.
* autocompletion, goto definition, or documentation on hover
* server can be reused
{{< /note >}}

---

## What is LSP?
* Protocol used between editors and servers that provide language support
* JSON-RPC-based
* Originally developed for Microsoft Visual Studio Code, now open standard

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

{{< note >}}
* Code completion, syntax highlighting, marking of warnings and errors, and help with refactoring
* RPC (Remote Procedure Call) is a protocol that allows a program to execute a procedure or function on a remote computer or server as if it were a local call, simplifying distributed application development by abstracting away the complexities of network communication.
{{< /note >}}

---
{{% section %}}

## Key concepts of LSP

---

### Overview

* **Client**: editor
* **Server**: language server
* Message exchange through stdio or socket

![Exchange of messages](img/messages.png)

{{< note >}}
* Connection maintained until the exit notification is sent
{{< /note >}}

---

### Multiple languages

* Typical implementation
* Separate language server for each programming language

![Exchange of messages](img/multiple-languages.png)

{{< note >}}
* for a single project development tool usually starts 
{{< /note >}}

---

### Capabilities

---
### Messages

1. **Request** — expects a response
2. **Response** — sent after receiving a request
3. **Notification**
   * treated as an event
   * must not get a response
   * does not have id

---



### Message structure

{{% /section %}}

---
{{% section %}}

## Phases

{{% /section %}}

---

## Sources

* https://medium.com/@malintha1996/understanding-the-language-server-protocol-5c0ba3ac83d2
* https://microsoft.github.io/language-server-protocol/overviews/lsp/overview/

---


















# OLD

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
