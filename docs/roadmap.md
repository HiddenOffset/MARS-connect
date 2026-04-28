# Roadmap — mips-net-plugin

A lightweight task list for working through architecture decisions and reaching
early proof-of-concept milestones.  Items are roughly ordered by dependency;
later items assume earlier ones are resolved.

---

## Phase 0 — Project Setup ✅

- [x] Create repository with initial folder structure.
- [x] Write README with project overview and open design questions.
- [x] Write design notes covering all candidate approaches.
- [ ] Agree on a contribution / experimentation workflow.

---

## Phase 1 — Environment & Tooling

- [ ] Choose a MIPS emulator to target first (QEMU linux-user is suggested).
- [ ] Document how to build / install that emulator from source.
- [ ] Get a minimal "Hello, World" MIPS binary running inside the emulator.
- [ ] Verify which MIPS ABI variant the test binary uses (O32/N32/N64).
- [ ] Set up a simple CI check: compile a stub MIPS program and confirm it
       executes without crashing.

---

## Phase 2 — Host-Side Proxy PoC

- [ ] Define a minimal command protocol (text or binary).
  - Suggested commands: `CONNECT <host> <port>`, `SEND <len> <data>`,
    `RECV <max_len>`, `CLOSE`.
- [ ] Implement a stub proxy server on the host that echoes commands back.
- [ ] Write a minimal MIPS guest program that opens the UART / shared channel
       and exchanges a single request/response pair.
- [ ] Document the round-trip: latency, throughput, known limitations.

---

## Phase 3 — Syscall Interception PoC

- [ ] Identify relevant Linux syscall numbers for the chosen MIPS ABI
       (socket=4183, connect=4170, …).
- [ ] Find or create a hook point in the chosen emulator for syscall trapping.
- [ ] Implement a minimal handler for `socket` and `connect` that creates a
       *real* host socket and stores a mapping (guest fd → host fd).
- [ ] Extend the handler to cover `send`/`recv`/`close`.
- [ ] Run an unmodified MIPS `curl`-like test program through the interceptor.

---

## Phase 4 — MMIO Bridge PoC

- [ ] Choose a physical address range that is safe for the target platform.
- [ ] Define the register map (≤ 4 KB).
- [ ] Add a MMIO read/write callback to the emulator.
- [ ] Write a minimal guest-side header / library that wraps the register
       accesses into a simple API (`mmio_connect`, `mmio_send`, …).
- [ ] Demonstrate an end-to-end TCP connection using only the MMIO interface.

---

## Phase 5 — Emulator Plugin PoC

- [ ] Pick the emulator plugin API (QEMU QOM device, GXemul module, …).
- [ ] Implement a virtual NIC device stub (registers with the emulator but does
       nothing yet).
- [ ] Wire the NIC to a host TAP interface or user-space packet socket.
- [ ] Boot a MIPS Linux image and confirm the NIC appears as a network device.
- [ ] Run a simple ping or wget inside the guest through the virtual NIC.

---

## Phase 6 — Architecture Decision

After PoCs in phases 2–5 are complete:

- [ ] Review findings from each approach (latency, complexity, portability).
- [ ] Decide which approach (or combination) to pursue further.
- [ ] Choose implementation language(s).
- [ ] Write an ADR (Architecture Decision Record) in `docs/`.

---

## Phase 7 — Integration & Hardening

_Items here are speculative until Phase 6 is complete._

- [ ] Implement proper error handling and edge cases.
- [ ] Add basic security considerations (sandboxing, syscall filtering).
- [ ] Write integration tests that exercise the full guest→host→internet path.
- [ ] Performance benchmarks vs. native Linux networking.
- [ ] Packaging / distribution plan.

---

## Notes

- Phases 2 and 3 can proceed in parallel.
- Phase 4 and 5 depend on having a working emulator setup (Phase 1).
- Keep experiments in `experiments/` and graduated prototypes in `prototypes/`.
