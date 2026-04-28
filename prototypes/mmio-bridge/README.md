# prototypes/mmio-bridge/

Prototype for the **memory-mapped I/O bridge** approach.

## Goal

Reserve a small region of the guest's physical address space and use
reads/writes to that region as a network command interface, handled by the
emulator on the host side.

## Status

🚧 Not started — stub only.

## Planned Contents

```
mmio-bridge/
├── README.md          ← this file
├── emulator-side/     # MMIO read/write callbacks added to the emulator
│   └── .gitkeep
└── guest-lib/         # Thin guest-side library wrapping the register accesses
    └── .gitkeep
```

## Proposed Register Map (draft)

Base address: TBD (must not conflict with existing MIPS platform memory map).

| Offset | Width | Access | Name | Description |
|---|---|---|---|---|
| 0x00 | 4 B | write-only | CMD | Write a command code to trigger an operation |
| 0x04 | 4 B | read-only | STATUS | Completion status: 0 = busy, 1 = done, 2 = error |
| 0x08 | 4 B | read-only | ERRNO | Error code from last command (0 = success) |
| 0x0C | 4 B | read/write | BUF_ADDR | Guest physical address of data buffer |
| 0x10 | 4 B | read/write | BUF_LEN | Length of data in buffer |
| 0x14 | 4 B | read-only | RESULT | Return value of last command |

> Keeping CMD (write-only) and STATUS (read-only) as separate registers avoids
> ambiguity when polling for completion and prevents a write to CMD from
> accidentally reading a stale status value.

### Command Codes (draft)

| Code | Name | Description |
|---|---|---|
| 0x01 | NET_OPEN | Open a TCP connection (BUF_ADDR → "host:port\0") |
| 0x02 | NET_CLOSE | Close the connection |
| 0x03 | NET_SEND | Send BUF_LEN bytes from BUF_ADDR |
| 0x04 | NET_RECV | Receive up to BUF_LEN bytes into BUF_ADDR |

_This is a draft — subject to change._

## TODO

- [ ] Choose base address and validate no conflicts.
- [ ] Implement MMIO handler in the emulator.
- [ ] Write guest-lib header file with inline helpers.
- [ ] Write a bare-metal MIPS test program using the library.
- [ ] Add interrupt support to avoid busy-polling.
