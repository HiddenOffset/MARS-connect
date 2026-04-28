# Design Notes — mips-net-plugin

This document explores the possible implementation paths for connecting MIPS
programs to the internet.  Each section describes the approach, its advantages,
its challenges, and any open questions.

---

## 1. Syscall Interception

### Idea
Intercept network-related syscalls (socket, connect, send, recv, …) before
they reach the emulated kernel.  Replace the in-emulator handling with calls to
the *real* host network stack.

### How it might work
1. The emulator traps every syscall instruction (`syscall` / `TRAP`).
2. A handler checks whether the syscall number is network-related.
3. If yes, the handler translates the MIPS calling convention (a0–a3 registers,
   guest memory addresses) into equivalent host calls.
4. The return value is written back into the guest's v0/v1 registers.

### Advantages
- Transparent to the guest program — no code changes required.
- Works at a well-defined interface boundary.
- Relatively easy to prototype in emulators that already expose syscall hooks
  (e.g. QEMU's linux-user mode).

### Challenges
- Syscall numbers differ between MIPS ABI variants and OS targets (Linux O32,
  N32, N64; bare-metal; etc.).
- Pointer translation: guest pointers must be resolved to host memory before
  being passed to host syscalls.
- Blocking calls (e.g. `recv`) stall the emulator's main loop unless threaded
  carefully.
- Signal / interrupt delivery back into the guest is non-trivial.

### Open Questions
- Which MIPS ABI and OS target do we start with?
- Do we intercept at the emulator level or via a kernel module on a real MIPS
  board?

---

## 2. Emulator Plugin

### Idea
Write a plugin (shared library / module) for an existing MIPS emulator that
registers a virtual network interface card (NIC) the emulated OS can drive with
a standard driver.

### How it might work
1. The plugin registers a virtual device with the emulator's device bus.
2. The emulated OS loads a driver for that device (e.g. a virtio-net or custom
   driver).
3. The driver pushes/pulls Ethernet frames; the plugin forwards them to a host
   TAP/TUN interface or a user-space socket.

### Advantages
- The guest OS stays unmodified if a compatible driver already exists.
- Works with full-system emulation (real OS, real network stack inside guest).
- Leverage existing emulator extension APIs (QEMU QOM, GXemul module API, …).

### Challenges
- Requires a full-system emulation setup (heavier than linux-user mode).
- Writing or porting a driver for the virtual device adds significant complexity.
- Plugin APIs differ between emulators; porting effort per emulator is real.

### Open Questions
- Target emulator: QEMU? GXemul? Spike (for RISC-V comparison)? Custom?
- Use an existing NIC model (e.g. QEMU's `e1000`) or design a minimal custom one?

---

## 3. Memory-Mapped I/O (MMIO) Bridge

### Idea
Reserve a region of the guest's physical address space.  Reading from or
writing to that region triggers a callback in the emulator that performs a
corresponding network operation on the host.

### How it might work
1. Define a small register map (e.g. 4 KB):
   - `0x00` — command register (open/close/send/recv)
   - `0x04` — status / error code
   - `0x08` — data buffer pointer (guest physical address)
   - `0x0C` — data length
2. The guest writes the command + arguments, then polls the status register.
3. The emulator's MMIO handler performs the host operation and writes the result
   back into the status/data registers.

### Advantages
- Simple, well-understood mechanism (used by real hardware peripherals).
- No syscall involvement — works even for bare-metal guest programs.
- The "protocol" is fully under our control.

### Challenges
- Requires changes to the guest program (or a thin library it links against).
- Polling wastes cycles; interrupt-driven mode is more complex to implement.
- Buffer management across guest/host boundary needs careful alignment handling.

### Open Questions
- What physical address range to use? (must not conflict with existing MIPS
  memory map for the chosen platform)
- Polling vs. interrupt-driven?
- Should the register map expose raw bytes or a higher-level framing?

---

## 4. Host-Side Proxy / Service

### Idea
Run a small service on the host that the guest communicates with through an
already-emulated channel — e.g. a virtual serial port (UART), a shared memory
region, or a loopback socket if the emulator already provides basic networking.

### How it might work
1. The emulator exposes a UART or shared-memory device to the guest.
2. The guest sends simple text or binary commands over that channel
   (e.g. `CONNECT 93.184.216.34 80\r\n`).
3. The host-side proxy parses the commands, performs real network operations,
   and relays results back over the same channel.

### Advantages
- Can be prototyped without touching the emulator at all, if a UART is already
  available.
- The proxy can be written in any language independently of the emulator.
- Easy to test: the proxy is just a standalone process.

### Challenges
- Bandwidth and latency over a virtual UART are limited.
- Need to define a framing / protocol (line-based text, length-prefixed binary,
  …).
- The guest still needs a small driver/library to talk the protocol.

### Open Questions
- What transport channel to use? (UART, shared memory, Unix socket between host
  and guest via emulator passthrough?)
- Design a custom protocol or adopt an existing one (SOCKS5, HTTP CONNECT, …)?

---

## Comparison Matrix

| | Syscall Intercept | Emulator Plugin | MMIO Bridge | Host Proxy |
|---|:---:|:---:|:---:|:---:|
| Guest changes needed | None | None (with driver) | Small library | Small library |
| Emulator changes needed | Syscall hook | Plugin API | MMIO region | None (UART reuse) |
| Works bare-metal | ✗ | ✓ | ✓ | ✓ |
| Implementation complexity | Medium | High | Medium | Low |
| Performance ceiling | High | High | Medium | Low |
| Easiest to prototype first | ✓ | | | ✓ |

---

## Recommended Starting Point

Two approaches seem best for early PoCs:

1. **Host-Side Proxy** — lowest barrier to entry; no emulator changes needed.
2. **Syscall Interception** — cleanest eventual API; good leverage from QEMU
   linux-user mode.

Start with the host proxy to validate the concept end-to-end, then move to
syscall interception for a more integrated solution.
