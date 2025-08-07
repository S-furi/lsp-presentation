+++
title = "Language Server Protocol - Presentation"
outputs = ["Reveal"]
+++

## Language Server Protocol

Defines a set of JSON-RPC requests, response and notification messages in order
to standardize communication between developement tools and language specific
servers.

---

### Capabilities

A language server and a client can have different sets of capabilities, i.e. groups of **language features**. This set of capabilities is exchanged between client and server in the *initialization phase* in order to make them agree over which features are supported in the session.
