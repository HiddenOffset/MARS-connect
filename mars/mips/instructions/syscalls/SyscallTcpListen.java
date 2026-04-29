package mars.mips.instructions.syscalls;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;

public class SyscallTcpListen extends AbstractSyscall {

    // handle -> ServerSocket
    private static final Map<Integer, ServerSocket> servers = new HashMap<Integer, ServerSocket>();
    private static int nextHandle = 100; // separate space from client sockets

    public SyscallTcpListen() {
        super(1005, "TcpListen");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        try {
            int port = RegisterFile.getValue(4); // $a0

            if (port < 0 || port > 65535) {
                RegisterFile.updateRegister(2, -2); // invalid port
                return;
            }

            ServerSocket server = new ServerSocket(port);

            int handle;
            synchronized (servers) {
                handle = nextHandle++;
                servers.put(handle, server);
            }

            RegisterFile.updateRegister(2, handle); // return server handle

        } catch (Exception e) {
            RegisterFile.updateRegister(2, -1); // bind/listen failed
        }
    }

    // helper for accept syscall (next step)
    public static ServerSocket getServer(int handle) {
        synchronized (servers) {
            return servers.get(handle);
        }
    }

    public static ServerSocket removeServer(int handle) {
        synchronized (servers) {
            return servers.remove(handle);
        }
    }

    public static Map<Integer, ServerSocket> getServerHandleMap() {
        return servers;
    }

    public static void resetServerHandleCounter() {
        nextHandle = 100;
    }
}