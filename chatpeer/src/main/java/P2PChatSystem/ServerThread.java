package P2PChatSystem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {
    int port;

    public ServerThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(port);
            System.out.printf("Listening on port %d\n", port);

            while (true) {
                Socket newSocket = serverSocket.accept();
                ChatPeer.socketList.put(newSocket, newSocket.getRemoteSocketAddress().toString());
                new Thread(new ServerHandleThread(newSocket)).start();
            }
        } catch (IOException e) {
            System.out.printf("Error handling connections, %s\n", e.getMessage());
        }
    }
}
