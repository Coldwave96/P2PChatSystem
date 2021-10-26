package P2PChatSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class ClientThread implements Runnable {
    private int connectPort;
    public static boolean connectStat;
    public static Socket connectSocket;

    public void setConnectPort(int port) {
        this.connectPort = port;
    }

    public int getConnectPort() {
        return connectPort;
    }

    public ClientThread(int connectPort) {
        setConnectPort(connectPort);
    }

    private void helper() {
        System.out.println("#help - list this information");
        System.out.println("#connect IP[:port] [local port] - connect to another peer");
        System.out.println("#createroom ROOM - create a chat room");
        System.out.println("#list - list all the chat room in the peer");
        System.out.println("#who ROOM - list all members in the chat room");
        System.out.println("#kick USER - kick the user and block he or she from reconnecting");
        System.out.println("#delete ROOM - delete a chat room");
        System.out.println("#listneighbors - request the server to list its neighbors");
        System.out.println("#searchnetwork - list chat rooms over all accessible peers");
        System.out.println("#quit - quit the system");
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
                                setConnectPort(port);
                            }
                        }

                        String[] parse = command[1].split(":");
                        Socket socket;
                        if (getConnectPort() == 0) {
                            socket = new Socket(parse[0], Integer.parseInt(parse[1]));
                        } else {
                            socket = new Socket();
                            socket.bind(new InetSocketAddress(getConnectPort()));
                            socket.connect(new InetSocketAddress(parse[0], Integer.parseInt(parse[1])));
                        }
                        connectStat = true;
                        connectSocket = socket;
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
                        for (Socket s : ChatPeer.roomList.get(command[1])) {
                            try {
                                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                                Map<String, Object> deleteMap = new HashMap<>();
                                ObjectMapper mapper = new ObjectMapper();
                                deleteMap.put("type", "roomchange");
                                deleteMap.put("identity", ChatPeer.socketList.get(s));
                                deleteMap.put("former", command[1]);
                                deleteMap.put("roomId", "");
                                out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deleteMap));
                                out.flush();
                            } catch (IOException e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                        ChatPeer.roomList.remove(command[1]);
                    } else {
                        System.out.println("Room " + command[1] + " does not exist.");
                    }
                    break;
                case "#listneighbors":
                    System.out.println(ChatPeer.neighbors.values());
                    break;
                case "#searchnetwork":
                    searchNetwork(ChatPeer.neighbors);
                    break;
                case "#quit":
                    break mainLoop;
                default:
                    System.out.println("Use #help command to get more information.");
                    break;
            }
        }
        System.exit(0);
    }

    private void searchNetwork(HashMap<Socket, String> network) {
        for (Socket s : network.keySet()) {
            try {
                String[] temp = network.get(s).split(":");
                Socket socket = new Socket(temp[0].substring(1), Integer.parseInt(temp[1]));

                DataInputStream in = new DataInputStream(socket.getInputStream());
                ObjectMapper mapper = new ObjectMapper();
                String roomList = in.readUTF();
                Packet packet1 = mapper.readValue(roomList, Packet.class);
                System.out.println(socket.getRemoteSocketAddress());
                if (packet1.getRooms().isEmpty()) {
                    System.out.println("Not create a room yet.");
                } else {
                    for (String room : packet1.getRooms().keySet()) {
                        System.out.println(room + ": " + packet1.getRooms().get(room) + " users");
                    }
                }

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                Map<String, Object> listMap = new HashMap<>();
                listMap.put("type", "listneighbors");
                out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(listMap));
                out.flush();

                String newPeers = in.readUTF();
                Packet packet2 = mapper.readValue(newPeers, Packet.class);
                if (!packet2.getNeighbors().equals("[]")) {
                    HashMap<Socket, String> newNetwork = new HashMap<>();
                    String[] tempPeers = packet2.getNeighbors().substring(1, packet2.getNeighbors().length()-1).split(", ");
                    for (String str : tempPeers) {
                        if (!listMap.containsValue(str)) {
                            String[] tempStr = str.split(":");
                            Socket tempSocket = new Socket(tempStr[0].substring(1), Integer.parseInt(tempStr[1]));
                            newNetwork.put(tempSocket, str);
                        }
                    }
                    if (!newNetwork.isEmpty()) {
                        searchNetwork(newNetwork);
                    }
                } else {
                    System.out.println("Searching...");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("All done!");
    }
}
