# mips-net-plugin

An experimental project exploring how to give MIPS programs access to network
functionality through a plugin, bridge, or adapter layer — without modifying
the programs themselves (or with only minimal changes).

---

## Project Idea

MIPS programs running inside emulators or simulators are normally isolated from
the host's network stack. This project investigates different approaches to
bridging that gap: intercepting syscalls, injecting emulator plugins, exposing
memory-mapped I/O regions, or running a companion proxy on the host that the
guest communicates with.

## Goals

- Understand the trade-offs between the various integration approaches.
- Build small, focused proofs-of-concept (PoCs) for the most promising paths.
- Keep experiments self-contained so they can be compared side-by-side.
- Avoid locking in to a single implementation language until we have enough data.

## Possible Approaches

| Approach | Short Description |
|---|---|
| **Syscall interception** | Trap network-related syscalls inside the emulator and redirect them to host sockets. |
| **Emulator plugin** | Write a plugin for an existing MIPS emulator (e.g. QEMU, GXemul) that adds a virtual NIC. |
| **Memory-mapped I/O bridge** | Reserve a MMIO region; reads/writes to it are translated into network operations by the emulator. |
| **Host-side proxy / service** | Run a small service on the host; the MIPS program communicates via shared memory, a serial port emulation, or a simple IPC mechanism. |

See [`docs/design-notes.md`](docs/design-notes.md) for a deeper breakdown of
each approach.

## Open Design Questions

- Which MIPS emulator(s) should we target first?
- Should the guest program be aware of the bridge, or should it stay unmodified?
- What is the minimal viable API surface for the guest (raw sockets vs. BSD socket-like calls)?
- How do we handle blocking I/O inside a single-threaded emulator loop?
- What are the security implications of giving emulated code real network access?
- Do we need to support both big-endian (MIPS I) and little-endian (MIPS EL) guests?

## Repository Layout

```
mips-net-plugin/
├── docs/                   # Design docs, notes, and references
│   ├── design-notes.md     # Implementation path analysis
│   └── roadmap.md          # Task list and milestones
├── experiments/            # One-off exploration scripts and notes
│   └── README.md
└── prototypes/             # Skeleton code for each approach
    ├── syscall-intercept/
    │   └── README.md
    ├── emulator-plugin/
    │   └── README.md
    ├── mmio-bridge/
    │   └── README.md
    └── host-proxy/
        └── README.md
```

## Contributing / Exploring

This repo is intentionally sparse. If you want to try an approach:

1. Create a sub-folder under `prototypes/` (or `experiments/` for quick one-offs).
2. Add a short `README.md` describing what you tried and what you learned.
3. Prefer stubs, comments, and notes over large blocks of untested code.
4. Open a PR / issue to discuss findings before investing heavy implementation effort.

## Status

🚧 **Early exploration — no working implementation yet.**
