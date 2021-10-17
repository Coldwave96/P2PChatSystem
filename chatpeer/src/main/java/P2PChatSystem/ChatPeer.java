package P2PChatSystem;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.net.Socket;

public class ChatPeer {
    private int listenPort; //listen port
    private int connectPort; //connection port

    public void setListenPort(int port) {
        this.listenPort = port;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setConnectPort(int port) {
        this.connectPort = port;
    }

    public int getConnectPort() {
        return connectPort;
    }

    static class CmdOption {
        @Option(name = "-p", hidden = true, usage = "Port that the peer listens on for incoming connections")
        private int pPort = 4444;
        @Option(name = "-i", hidden = true, usage = "Port that the peer uses to make connections to other peers")
        private int iPort = 0;
    }

    //check whether the given port is used or not
    public static boolean isLocalPortUsing(int port) {
        boolean flag = false;
        try {
            Socket socket = new Socket("127.0.0.1", port);
            flag = true;
        } catch (IOException ignored) {
        }
        return flag;
    }

    public static void main(String[] args) {
        CmdOption option = new CmdOption();
        CmdLineParser parser = new CmdLineParser(option);

        ChatPeer chatPeer = new ChatPeer();

        try {
            parser.parseArgument(args);

            if (option.pPort < 1 || option.pPort > 65535 || option.iPort < 0 || option.iPort > 65535) {
                System.out.println("Port error, please check.");
                System.exit(0);
            } else if (isLocalPortUsing(option.pPort)) {
                System.out.printf("Port %d is occupied. Please try another one.\n", option.pPort);
                System.exit(0);
            } else if (isLocalPortUsing(option.iPort)) {
                System.out.printf("Port %d is occupied. Please try another one.\n", option.iPort);
                System.exit(0);
            } else {
                chatPeer.setListenPort(option.pPort);
                chatPeer.setConnectPort(option.iPort);
            }
        } catch (CmdLineException e) {
            System.out.println("Command line error: " + e.getMessage());
        }
    }
}
