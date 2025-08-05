+++
title = "LSP Presentation - JetBrains Internship Summer 2025"
outputs = ["Reveal"]
+++

## Base Protocol

The base protocol consists of a header and a content part (comparable to HTTP).

---

### Header Part

Composed of fileds `<name>: <value>`, terminated with `\r\n`. Currently, only two fields are supported

| Name | Value | Description |
| ---- | ------ | ------------ |
| Content-Length | number | length of content in bytes (**required**) |
| Content-Type | string | mime type of content part |

If no `Content-Type` is provided, default is `application/vscode-jsonrpc;charset=utf-8`

---

### Content Part

Content part begins after header, marked with a double `\r\n` (like HTTP), and uses **`JSON-RPC`** (https://www.jsonrpc.org)
to describe *requests*, *responses* and *notifications*.

Example:

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
