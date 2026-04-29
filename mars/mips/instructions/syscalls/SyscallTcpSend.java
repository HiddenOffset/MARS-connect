package mars.mips.instructions.syscalls;

import java.io.OutputStream;
import java.net.Socket;

import mars.Globals;
import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

public class SyscallTcpSend extends AbstractSyscall {

    public SyscallTcpSend() {
        super(1003, "TcpSend");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        try {
            int handle = RegisterFile.getValue(4); // $a0
            int bufPtr = RegisterFile.getValue(5); // $a1
            int len    = RegisterFile.getValue(6); // $a2

            if (len < 0) {
                RegisterFile.updateRegister(2, -2);
                return;
            }

            Socket socket = SyscallTcpConnect.getSocket(handle);
            if (socket == null) {
                RegisterFile.updateRegister(2, -1); // bad handle
                return;
            }

            byte[] data = readBuffer(bufPtr, len);

            OutputStream out = socket.getOutputStream();
            out.write(data);
            out.flush();

            RegisterFile.updateRegister(2, len); // bytes sent
        } catch (AddressErrorException e) {
            RegisterFile.updateRegister(2, -3); // bad MIPS memory
        } catch (Exception e) {
            RegisterFile.updateRegister(2, -4); // generic send failure
        }
    }

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