package mars.mips.instructions.syscalls;

import java.net.ServerSocket;
import java.net.Socket;

import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;

public class SyscallTcpAccept extends AbstractSyscall {

    public SyscallTcpAccept() {
        super(1006, "TcpAccept");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        try {
            int serverHandle = RegisterFile.getValue(4); // $a0

            ServerSocket server = SyscallTcpListen.getServer(serverHandle);
            if (server == null) {
                RegisterFile.updateRegister(2, -1); // invalid handle
                return;
            }

            // BLOCKS until a client connects
            Socket client = server.accept();

            // Reuse your existing socket table from TcpConnect
            int clientHandle;
            synchronized (SyscallTcpConnect.class) {
                // reuse the same mechanism as connect()
                clientHandle = SyscallTcpConnect.addAcceptedSocket(client);
            }

            RegisterFile.updateRegister(2, clientHandle);

        } catch (Exception e) {
            RegisterFile.updateRegister(2, -2); // accept failed
        }
    }
}