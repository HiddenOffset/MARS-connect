# prototypes/host-proxy/

Prototype for the **host-side proxy / service** approach.

## Goal

Run a small proxy service on the host that the MIPS guest communicates with
over an already-available emulated channel (virtual UART, shared memory, etc.).
The proxy translates guest commands into real network operations.

## Status

🚧 Not started — stub only.

## Planned Contents

```
host-proxy/
├── README.md          ← this file
├── proxy/             # Host-side proxy service (language TBD)
│   └── .gitkeep
└── guest-client/      # MIPS guest library / driver for talking to the proxy
    └── .gitkeep
```

## Draft Protocol

A simple line-oriented text protocol over a virtual UART (easy to debug with
a terminal; can be replaced with a binary framing later).

```
# Guest → Proxy
CONNECT <host> <port>\r\n
SEND <length>\r\n<data bytes>
RECV <max_length>\r\n
CLOSE\r\n

# Proxy → Guest
OK\r\n
OK <length>\r\n<data bytes>
ERR <code> <message>\r\n
```

_This is a draft — the protocol will likely evolve._

> ⚠️ **Security consideration:** The `CONNECT` command accepts arbitrary host
> addresses, which means any guest code can reach any reachable host from the
> proxy's network perspective.  Before this leaves the experimental stage,
> consider:
> - An explicit allowlist of permitted destination hosts / CIDR ranges.
> - Rate-limiting or connection caps to prevent abuse.
> - Running the proxy in a sandboxed process (separate user, namespace, or
>   seccomp filter) to limit blast radius if the guest sends malicious input.
> - Documenting the intended threat model (trusted guest vs. untrusted guest).

## Candidate Transport Channels

| Channel | Notes |
|---|---|
| Virtual UART (serial port) | Most emulators expose one; simple but slow |
| Shared memory region | Faster; requires emulator support for shared mem |
| Unix socket passthrough | If the emulator can forward a guest device to a host socket |

## TODO

- [ ] Pick transport channel (start with virtual UART — least setup).
- [ ] Implement proxy stub that just echoes commands back.
- [ ] Write guest UART driver (or reuse one from the emulator's BSP).
- [ ] Implement `CONNECT` + `SEND` + `RECV` in the proxy.
- [ ] Test end-to-end: MIPS guest fetches a small HTTP resource through proxy.
- [ ] Measure throughput and latency; decide if the channel is fast enough.
