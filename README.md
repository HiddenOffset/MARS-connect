# MARS — MIPS Assembler and Runtime Simulator

Short, portable MIPS assembler / simulator used for teaching and experimenting with MIPS assembly language programs.

**Quick summary**
- **What:** Educational MIPS simulator (GUI + CLI) and tools.
- **Language:** Java (plain .java sources in project root and `mars/`).
- **Run:** Build then run the `Mars` entry point.

**Quickstart**
- **Prerequisites:** Java JDK (11+ recommended) installed and `java`/`javac` on PATH.
- **Build (Windows):**

```powershell
compile.bat
```

- **Run:**

```powershell
java -cp . Mars
```

If you prefer to build manually, compile the sources and then run `Mars`:

```powershell
javac -d . Mars.java
javac -d . mars\\**\\*.java
java -cp . Mars
```

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
# MARS
MARS (official) MIPS Assembler and Runtime Simulator

 MARS is a lightweight interactive development environment (IDE) for programming in MIPS assembly language, intended for educational-level use with Patterson and Hennessy's Computer Organization and Design.

 It is available for you to download as an executable JAR file.  Click the MARS v.4.5 link at right, under Releases, to access the download.

 We have relocated the MARS website to  https://dpetersanderson.github.io/    There are still a couple of display issues to work out but all website content is there.  This includes all documentation and the JAR download.  We are not experienced in github so if you know a way we can host the website in this MARS repository let us know.

 This is the original MARS, developed by Pete Sanderson and Ken Vollmar.  Developed while Pete was professor at Otterbein University and Ken was professor at Missouri State University.  Both are now retired. This is a legacy application but is very stable and continues to be used by universities throughout the world as of 2024. The last release was MARS 4.5 in August 2014.  
 
 Until October 2024 the MARS website, including executable JAR file download, was hosted by Missouri State University. That is no longer the case. That's why it is here.
