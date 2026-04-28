package mars.mips.instructions.syscalls;

import java.net.Socket;

import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;

public class SyscallTcpClose extends AbstractSyscall {

    public SyscallTcpClose() {
        super(1002, "TcpClose");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        try {
            int handle = RegisterFile.getValue(4); // $a0
            Socket socket = SyscallTcpConnect.removeSocket(handle);

            if (socket == null) {
                RegisterFile.updateRegister(2, -1);
                return;
            }

            socket.close();
            RegisterFile.updateRegister(2, 0);
        } catch (Exception e) {
            RegisterFile.updateRegister(2, -2);
        }
    }
}