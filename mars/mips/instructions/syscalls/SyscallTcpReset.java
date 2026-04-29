package mars.mips.instructions.syscalls;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mars.ProgramStatement;
import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;

public class SyscallTcpReset extends AbstractSyscall {

    public SyscallTcpReset() {
        super(1000, "TcpReset");
    }

    @Override
    public void simulate(ProgramStatement statement) throws ProcessingException {
        int closed = 0;

        try {
            // Close all client sockets
            synchronized (SyscallTcpConnect.class) {
                List<Integer> socketHandles = new ArrayList<Integer>(SyscallTcpConnect.getSocketHandleMap().keySet());
                for (Integer h : socketHandles) {
                    try {
                        Socket s = SyscallTcpConnect.removeSocket(h);
                        if (s != null) {
                            s.close();
                            closed++;
                        }
                    } catch (Exception e) {
                        // keep going
                    }
                }
                SyscallTcpConnect.resetSocketHandleCounter();
            }

            // Close all server sockets
            synchronized (SyscallTcpListen.class) {
                List<Integer> serverHandles = new ArrayList<Integer>(SyscallTcpListen.getServerHandleMap().keySet());
                for (Integer h : serverHandles) {
                    try {
                        ServerSocket ss = SyscallTcpListen.removeServer(h);
                        if (ss != null) {
                            ss.close();
                            closed++;
                        }
                    } catch (Exception e) {
                        // keep going
                    }
                }
                SyscallTcpListen.resetServerHandleCounter();
            }

            RegisterFile.updateRegister(2, closed); // $v0 = number closed
        } catch (Exception e) {
            RegisterFile.updateRegister(2, -1);
        }
    }
}