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

WHAT EXACTLY SERVER GIVE US
SERVER SAVES FILES ON ITS SIDE
(IF THE LANGUAGE IS EXPOSED BY LSP ITSELF?)
{{< /note >}}

---

## What is LSP?
* Protocol used between editors and servers that provide language support
* JSON-RPC-based
* Originally developed for Microsoft Visual Studio Code, now open standard

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

### Capability

* Set of language features
* Client and server announce their supported features using capabilities
* Enables backward compatibility

{{< note >}}
* defined by the LSP specification

HOW DOES IT ENABLE BACKWARD CAPABILITY
{{< /note >}}

{{% /section %}}

---

{{% section %}}

## Messages

---

### Types of messages

1. **Request** — expects a response
2. **Response** — result of a request
3. **Notification**
   * treated as an event
   * must not get a response
   * does not have id


{{< note >}}
WHY DO WE NEED NOTIFICATIONS EVEN -> GIVE EXAMPLE LOGS
{{< /note >}}


---

### Message structure

* **Header**
  * `Content-Lenght`
  * optional `Content-Type`; default: `application/vscode-jsonrpc;charset=utf-8`
* **Content**
  * `jsonrpc` -- version of JSON-RPC used, always equal to 2.0
  * the rest of the fields depend on the type of the message

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
* header + content like HTTP
* header separated by 2 endlines
{{< /note >}}


---

#### Request
* `id`
* `method`
* optional `params`

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
* method - type/title
* params - content/data
{{< /note >}}

---

#### Response
* `id`
* optional `result`
* optional `error`


```json
Content-Length: ...\r\n
\r\n
{
	"jsonrpc": "2.0",
	"id": 1,
	"result": {
		...
	}
}
```

{{< note >}}
* id correspondes to previous request
* EITHER result OR error
 {{< /note >}}

---

#### Notification
* `method`
* optional `params`

```json
Content-Length: ...\r\n
\r\n
{
	"jsonrpc": "2.0",
	"method": "initialized",
	"params": {
		...
	}
}
```

{{< note >}}
* we don't have id
  {{< /note >}}

{{% /section %}}

---

{{% section %}}

## Server Lifecycle

---

### Phases


1. Initialization
2. Communication
3. Shutdown

---

### Initialization

![Exchange of messages](img/initialization.png)

{{< note >}}
* The whole lifecycle is managed by the client
* Establishing capabilities
{{< /note >}}

{{< note >}}
WHAT WOULD HAPPEN IF WE DIDN'T PROVIDE INITIALIZATION
{{< /note >}}

---

### Shutdown

![Exchange of messages](img/shutdown.png)

{{< note >}}
* in result: null
  {{< /note >}}


{{< note >}}
WHY HAVING SHUTDOWN
{{< /note >}}

---

### Other lifecycle capabilities

* registering new capabilities
* setting log preferences

{{% /section %}}

---
{{%section %}}

## Document synchronization

---

### Mandatory capabilities

Notifications:
* didOpen -- transfers the whole document and locks the file
* didChange -- transfers changes
* didClose -- unlocks the file

{{< note >}}
* used for document synchronization
* server implements either all or none

WHAT WOULD HAPPEN WITHOUT OPENING THE FILE?
{{< /note >}}

---

### Other document synchronization capabilities

* willing to save the document
* saving the document

{{< note >}}
* Renaming through close and open
{{< /note >}}

---

### Example

![Exchange of messages](img/document.png)

{{% /section %}}


[//]: # (---)

[//]: # (## Sources)

[//]: # ()
[//]: # (* https://medium.com/@malintha1996/understanding-the-language-server-protocol-5c0ba3ac83d2)

[//]: # (* https://microsoft.github.io/language-server-protocol/overviews/lsp/overview/)
