# prototypes/emulator-plugin/

Prototype for the **emulator plugin** approach.

## Goal

Write a plugin for an existing MIPS emulator that registers a virtual NIC,
allowing a full-system MIPS guest to use standard network drivers and reach
the host's network stack.

## Status

🚧 Not started — stub only.

## Planned Contents

```
emulator-plugin/
├── README.md          ← this file
├── device/            # Virtual NIC device implementation (language TBD)
│   └── .gitkeep
└── guest-driver/      # Optional: minimal guest-side driver if needed
    └── .gitkeep
```

## Candidate Emulators

- **QEMU** (mips or mipsel target) — mature plugin/device API (QOM).
- **GXemul** — simpler codebase, easier to modify but less maintained.
- **Custom toy emulator** — full control, but significant up-front effort.

## TODO

- [ ] Decide which emulator to target.
- [ ] Study the emulator's device registration API.
- [ ] Implement a no-op NIC device (registers, does nothing).
- [ ] Wire TX/RX paths to a host TAP interface.
- [ ] Boot a MIPS Linux image and confirm `/dev/eth0` (or equivalent) appears.
- [ ] Run a ping from inside the guest through the virtual NIC.
