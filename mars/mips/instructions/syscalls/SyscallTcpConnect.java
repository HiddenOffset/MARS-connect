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


// New instance of a Syscall that connects to an existing TCP socket and returns a handle for the connection
public class SyscallTcpConnect extends AbstractSyscall {
    // Hashmap to store active connections, mapping a handle int to a Socket object
    private static final Map<Integer, Socket> sockets = new HashMap<Integer, Socket>();
    private static int nextHandle = 3;

    // Constructor
    public SyscallTcpConnect() {
        super(1001, "TcpConnect");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        // Read the arguments from the registers
        try {
            int hostPtr = RegisterFile.getValue(4); // $a0
            int port = RegisterFile.getValue(5);    // $a1

            //Validate that port is in the correct range for TCP ports
            if (port < 0 || port > 65535) {
                RegisterFile.updateRegister(2, -2); // Return an error code if not
                return;
            }

            String host = readCString(hostPtr); //Read the null-terminated string for the hostname

            // Create a java Socket object
            Socket socket = new Socket();
            //Initiate connection with a timeout of 3 seconds (this should be configurable in the future)
            socket.connect(new InetSocketAddress(host, port), 3000);

            // Assign a handle to the socket and store it in the hashmap for later
            int handle;
            synchronized (sockets) {
                handle = nextHandle++;
                sockets.put(handle, socket);
            }

            // Return the handle to the MIPS program in $v0
            RegisterFile.updateRegister(2, handle); // $v0 = fake socket handle

        //Error handling
        } catch (AddressErrorException e) {
            RegisterFile.updateRegister(2, -3); // bad MIPS memory address
        } catch (Exception e) {
            RegisterFile.updateRegister(2, -1); // generic connect failure
        }
    }

    // Helper method to read a null-terminated string from MIPS memory given a starting address
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

    // Public helper methods for other syscalls to access Sockets based on their handle
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