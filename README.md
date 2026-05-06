# MARS — MIPS Assembler and Runtime Simulator

Short, portable MIPS assembler / simulator used for teaching and experimenting with MIPS assembly language programs.

**Quick summary**
- **What:** Educational MIPS simulator (GUI + CLI) and tools.
- **Language:** Java (plain .java sources in project root and `mars/`).
- **Run:** Download, extract, and run — demos included out of the box.

**Quickstart**
- **Prerequisites:** Java JDK (11+ recommended) installed and `java` on PATH.

1. Go to the [Releases](../../releases) page and download the latest `.zip` file.
2. Extract the zip to any folder.
3. Run MARS:

```powershell
java -jar Mars.jar
```

That's it! The `demos/` folder is included in the zip — open any `.asm` file from there via **File → Open** to get started right away.

**No compiling required. Everything is ready to go.**

**Files of interest**
- **Mars.java:** main entry point (launcher). See [Mars.java](Mars.java).
- **compile.bat / CreateMarsJar.bat:** helper scripts to build / produce jar files.
- **mars/**: core simulator code and packages.

**Usage & Examples**
- Try the demos in the `demos/` folder (e.g., `http_server.asm`, `listen_demo.asm`).
- When running from VS Code, ensure `java.project.sourcePaths` includes `.` and `mars` (see `.vscode/settings.json`).

**Networking / Internet Connectivity**
- Several demos (notably `demos/http_server.asm` and `demos/listen_demo.asm`) demonstrate the simulator's networking syscalls.
- The simulator implements a small socket-like API exposed to MIPS programs via special syscalls (examples used in `http_server.asm` include `1000` reset, `1005` listen, `1006` accept, `1004` recv, `1003` send, `1002` close). These map to real TCP sockets in the host JVM, so MIPS programs can accept and make real network connections.
- How to run the HTTP demo:

```powershell
compile.bat
java -cp . Mars
# In the GUI: File → Open → demos/http_server.asm, then Run/Simulate the program
# Or assemble and run via your usual workflow
```

- Then from the same machine you can test with:

```powershell
curl http://localhost:8080/
```

- To connect from another machine, replace `localhost` with the host machine's IP (ensure the host's firewall allows inbound connections on the demo port, e.g., `8080`).
- Security note: the simulator uses the host JVM networking stack. Do not run untrusted assembly that opens network sockets on public interfaces without auditing it first.


**Development & Contributing**
- Use your IDE (VS Code + Java extension recommended) or command-line `javac`.
- Run the build script (`compile.bat`) to compile all sources into the project root.
- Open issues or PRs for bugs, features, or documentation improvements.

**Roadmap / Innovative ideas**
- Networking & distributed MIPS instances — run small MIPS services that communicate.
- Web-based IDE with realtime visualization (registers, memory, pipeline).
- Add performance analysis (cache, branch predictor statistics) and optimization challenges.
- Create a minimal compiler that targets the simulator as a backend.

**License**
- This project contains MIT-licensed sources (see header comments in the Java files). Keep the original license notices when redistributing or modifying.

**Contact / Next steps**
- Want me to: add a CONTRIBUTING.md, wire a simple CI build, or scaffold the web IDE prototype? Reply with which item to start first and I'll implement it.
