package mars.mips.instructions.syscalls;

import java.io.InputStream;
import java.net.Socket;

import mars.Globals;
import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

public class SyscallTcpRecv extends AbstractSyscall {

    public SyscallTcpRecv() {
        super(1004, "TcpRecv");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        try {
            int handle = RegisterFile.getValue(4); // $a0
            int bufPtr = RegisterFile.getValue(5); // $a1
            int maxLen = RegisterFile.getValue(6); // $a2

            if (maxLen < 0) {
                RegisterFile.updateRegister(2, -2);
                return;
            }

            Socket socket = SyscallTcpConnect.getSocket(handle);
            if (socket == null) {
                RegisterFile.updateRegister(2, -1); // bad handle
                return;
            }

            InputStream in = socket.getInputStream();
            byte[] tmp = new byte[maxLen];
            int n = in.read(tmp);

            if (n > 0) {
                writeBuffer(bufPtr, tmp, n);
            }

            RegisterFile.updateRegister(2, n); // bytes read, 0 EOF, or -1 from Java EOF
        } catch (AddressErrorException e) {
            RegisterFile.updateRegister(2, -3); // bad MIPS memory
        } catch (Exception e) {
            RegisterFile.updateRegister(2, -4); // generic recv failure
        }
    }

    private void writeBuffer(int addr, byte[] data, int len) throws AddressErrorException {
        for (int i = 0; i < len; i++) {
            synchronized (Globals.memoryAndRegistersLock) {
                Globals.memory.setByte(addr + i, data[i]);
            }
        }
    }
}