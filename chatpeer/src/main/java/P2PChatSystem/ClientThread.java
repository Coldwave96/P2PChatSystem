package P2PChatSystem;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread implements Runnable {
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public ClientThread(int port) {
        setPort(port);
    }

    private void helper() {
        System.out.println("#help - list this information");
        System.out.println("#connect IP[:port] [local port] - connect to another peer");
        System.out.println("#createroom room - create a chat room");
        System.out.println("#list");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
    }

    @Override
    public void run() {
        Scanner kb = new Scanner(System.in);

        mainLoop:
        while (true) {
            System.out.print(">");
            String input = kb.nextLine();
            String[] command = input.split(" ");

            switch (command[0].toLowerCase()) {
                case "#connect":
                    try {
                        if (command[2] != null) {
                            int port = Integer.getInteger(command[2]);
                            if (port < 1 || port > 65535) {
                                System.out.println("Port error, please check.");
                            } else if (ChatPeer.isLocalPortUsing(port)) {
                                System.out.printf("Port %d is occupied. Please try another one.\n", port);
                            } else {
                                setPort(port);
                            }
                        }

                        String[] parse = command[1].split(":");
                        Socket socket = null;
                        if (getPort() == 0) {
                            socket = new Socket(parse[0], Integer.getInteger(parse[1]));
                        } else {
                            socket = new Socket();
                            socket.bind(new InetSocketAddress(getPort()));
                            socket.connect(new InetSocketAddress(parse[0], Integer.getInteger(parse[1])));
                        }
                        new Thread(new ClientHandleThread(socket)).start();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "#help":
                    helper();
                    break;
            }
        }
    }
}
