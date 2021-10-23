package P2PChatSystem;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
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
        System.out.println("#createroom ROOM - create a chat room");
        System.out.println("#list - list all the chat room in the peer");
        System.out.println("#join ROOM- join a chat room");
        System.out.println("#who ROOM - list all members in the chat room");
        System.out.println("#kick USER - kick the user and block he or she from reconnecting");
        System.out.println("#delete ROOM - delete a chat room");
        System.out.println("#listneighbors - request the server to list its neighbors");
        System.out.println("#searchnetwork - list chat rooms over all accessible peers");
        System.out.println("#shout - delivery message to all rooms on all peers of the network");
        System.out.println("#quit - quit the system");
        System.out.println("message - all the input other than the commands below");
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
                        if (command.length > 2) {
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
                            socket = new Socket(parse[0], Integer.parseInt(parse[1]));
                        } else {
                            socket = new Socket();
                            socket.bind(new InetSocketAddress(getPort()));
                            socket.connect(new InetSocketAddress(parse[0], Integer.parseInt(parse[1])));
                        }
                        ConnectionHandler connectionHandler = new ConnectionHandler(socket);
                        connectionHandler.run();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "#help":
                    helper();
                    break;
                case "#createroom":
                    if (command[1].matches("^[0-9a-zA-Z]+$")
                            && command[1].length() >=3
                            && command[1].length() <=32
                            && !ChatPeer.roomList.containsKey(command[1])) {
                        ChatPeer.roomList.put(command[1], new ArrayList<>());
                        System.out.printf("Room %s created.\n", command[1]);
                    } else {
                        System.out.printf("Room %s is invalid or already in use.\n", command[1]);
                    }
                    break;
                case "#list":
                    for (String room : ChatPeer.roomList.keySet()) {
                        System.out.println(room + ": " + ChatPeer.roomList.get(room).size() + " guests");
                    }
                    break;
                case "#join":
                    System.out.println("Not connected to a peer yet.");
                    break;
                case "#who":
                    if (ChatPeer.roomList.containsKey(command[1])) {
                        System.out.println(command[1] + " contains " + ChatPeer.roomList.get(command[1]));
                    } else {
                        System.out.println("room " + command[1] + " does not exist.");
                    }
                    break;
                case "#kick":
                    if (!ChatPeer.socketList.containsValue(command[1])) {
                        System.out.println("Peer not exist.");
                    } else {
                        for (Socket s : ChatPeer.socketList.keySet()) {
                            if (Objects.equals(ChatPeer.socketList.get(s), '/' + command[1])) {
                                try {
                                    s.close();
                                    ChatPeer.socketList.remove(s);
                                    for (String room : ChatPeer.roomList.keySet()) {
                                        ChatPeer.roomList.get(room).remove(s);
                                    }

                                    String[] tempStr = command[1].split(":");
                                    ChatPeer.blackList.add(tempStr[1].substring(1));
                                } catch (IOException e) {
                                    System.out.println("Error: " + e.getMessage());
                                }
                            }
                        }
                    }
                    break;
                case "#delete":
                    if (ChatPeer.roomList.containsKey(command[1])) {
                        ChatPeer.roomList.remove(command[1]);
                        //send RoomChange message to peers
                    } else {
                        System.out.println("Room " + command[1] + " does not exist.");
                    }
                    break;
                case "#listneighbors":
                    System.out.println(ChatPeer.socketList.keySet());
                    break;
                case "#searchnetwork":
                    break;
                case "#shout":
                    break;
                case "#quit":
                    break mainLoop;
                default:
                    System.out.println("You have not connected to a peer yet.");
                    break;
            }
        }
        System.exit(0);
    }
}
