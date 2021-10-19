package P2PChatSystem;

import java.net.Socket;

public class ServerHandleThread implements Runnable {
    Socket s;

    public ServerHandleThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {

    }
}
