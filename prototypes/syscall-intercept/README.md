# prototypes/syscall-intercept/

Prototype for the **syscall interception** approach.

## Goal

Intercept network-related Linux syscalls inside a MIPS emulator and redirect
them to real host sockets, making network calls transparent to the guest
program.

## Status

🚧 Not started — stub only.

## Planned Contents

```
syscall-intercept/
├── README.md          ← this file
├── handler/           # Syscall handler logic (language TBD)
│   └── .gitkeep
└── guest/             # Minimal MIPS test program that exercises the handler
    └── .gitkeep
```

## Key Syscalls to Intercept

| Syscall | Linux O32 number | Description |
|---|---|---|
| socket | 4183 | Create a socket |
| connect | 4170 | Connect to a remote address |
| send / sendto | 4180 / 4181 | Send data |
| recv / recvfrom | 4175 / 4176 | Receive data |
| close | 4006 | Close a file descriptor |
| bind | 4169 | Bind a socket (server side) |
| listen | 4174 | Mark socket as passive |
| accept | 4168 | Accept a connection |

> ⚠️ **Verify these numbers before use.**  Older MIPS O32 kernels multiplex all
> socket operations through a single `socketcall` (4102) syscall with an opcode
> in a0; newer kernels expose individual syscalls.  Cross-reference
> `/usr/include/asm/unistd.h` (or `arch/mips/include/uapi/asm/unistd.h` in the
> kernel tree) for the exact target configuration.

## TODO

- [ ] Identify hook point in chosen emulator.
- [ ] Implement fd mapping table (guest fd → host fd).
- [ ] Handle endianness when reading sockaddr structs from guest memory.
- [ ] Deal with blocking calls (non-blocking mode + polling, or threading).
- [ ] Write a test MIPS binary that makes a simple HTTP request.
