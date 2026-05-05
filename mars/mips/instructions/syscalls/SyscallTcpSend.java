package mars.mips.instructions.syscalls;

import java.io.OutputStream;
import java.net.Socket;

import mars.Globals;
import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

// New instance of a Syscall that sends data over an existing TCP socket
public class SyscallTcpSend extends AbstractSyscall {

    // Constructor
    public SyscallTcpSend() {
        super(1003, "TcpSend");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        // Read the arguments from the registers
        try {
            int handle = RegisterFile.getValue(4); // $a0
            int bufPtr = RegisterFile.getValue(5); // $a1
            int len    = RegisterFile.getValue(6); // $a2

            // Make sure the packet isn't empty
            if (len < 0) {
                RegisterFile.updateRegister(2, -2);
                return;
            }

            // Get the Socket associated with the handle from the socket table in SyscallTcpConnect
            Socket socket = SyscallTcpConnect.getSocket(handle);

            // Error checking for bad handle
            if (socket == null) {
                RegisterFile.updateRegister(2, -1); // bad handle
                return;
            }

            // Read the data buffer from MIPS memory
            byte[] data = readBuffer(bufPtr, len);

            // Get the output stream for the socket and send the data we just read
            OutputStream out = socket.getOutputStream();
            out.write(data);
            out.flush();

            // Return the number of bytes sent (should be the same as the length of the buffer we sent)
            RegisterFile.updateRegister(2, len); // bytes sent

        // Error handling
        } catch (AddressErrorException e) {
            RegisterFile.updateRegister(2, -3); // bad MIPS memory
        } catch (Exception e) {
            RegisterFile.updateRegister(2, -4); // generic send failure
        }
    }

    // Helper method to read a byte buffer from MIPS memory given a starting address and length
    private byte[] readBuffer(int addr, int len) throws AddressErrorException {
        byte[] data = new byte[len];

        for (int i = 0; i < len; i++) {
            synchronized (Globals.memoryAndRegistersLock) {
                data[i] = (byte)(Globals.memory.getByte(addr + i) & 0xFF);
            }
        }

        return data;
    }
}