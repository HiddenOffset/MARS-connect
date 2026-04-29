package mars.mips.instructions.syscalls;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import mars.Globals;
import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

public class SyscallTcpConnect extends AbstractSyscall {

    // handle table
    private static final Map<Integer, Socket> sockets = new HashMap<Integer, Socket>();
    private static int nextHandle = 3;

    public SyscallTcpConnect() {
        super(1001, "TcpConnect");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        try {
            int hostPtr = RegisterFile.getValue(4); // $a0
            int port = RegisterFile.getValue(5);    // $a1

            if (port < 0 || port > 65535) {
                RegisterFile.updateRegister(2, -2); // bad port
                return;
            }

            String host = readCString(hostPtr);

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);

            int handle;
            synchronized (sockets) {
                handle = nextHandle++;
                sockets.put(handle, socket);
            }

            RegisterFile.updateRegister(2, handle); // $v0 = fake socket handle
        } catch (AddressErrorException e) {
            RegisterFile.updateRegister(2, -3); // bad MIPS memory address
        } catch (Exception e) {
            RegisterFile.updateRegister(2, -1); // generic connect failure
        }
    }

    private String readCString(int addr) throws AddressErrorException {
        StringBuilder sb = new StringBuilder();
        int p = addr;

        while (true) {
            int b;
            synchronized (Globals.memoryAndRegistersLock) {
                b = Globals.memory.getByte(p) & 0xFF;
            }

            if (b == 0) {
                break;
            }

            sb.append((char) b);
            p++;
        }

        return sb.toString();
    }

    public static Socket getSocket(int handle) {
        synchronized (sockets) {
            return sockets.get(handle);
        }
    }

    public static Socket removeSocket(int handle) {
        synchronized (sockets) {
            return sockets.remove(handle);
        }
    }

    public static int addAcceptedSocket(Socket socket) {
    synchronized (sockets) {
        int handle = nextHandle++;
        sockets.put(handle, socket);
        return handle;
        }
    }
    public static Map<Integer, Socket> getSocketHandleMap() {
            return sockets;
        }

        public static void resetSocketHandleCounter() {
            nextHandle = 3;
        }
}