package P2PChatSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConnectionHandler {
    private Socket s;
    public static String roomId;
    public static String id;

    public void setS(Socket s) {
        this.s = s;
    }

    public Socket getS() {
        return s;
    }

    public ConnectionHandler(Socket socket) {
        setS(socket);
    }

    private void helper() {
        System.out.println("#help - list this information");
        System.out.println("#join ROOM - join a chat room");
        System.out.println("#who ROOM - request a member list of the chat room");
        System.out.println("#list - request a room list of the current peer");
        System.out.println("#listneighbors - request the server to list its neighbors");
        System.out.println("#shout - delivery message to all rooms on all peers of the network");
        System.out.println("#quit - disconnect from the peer");
        System.out.println("message - all the input other than the commands below");
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> hostChangeMap = new HashMap<>();
            hostChangeMap.put("type", "hostchange");
            String host = getS().getLocalAddress() + ":" + ChatPeer.listenPort;
            hostChangeMap.put("host", host);
            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hostChangeMap));
            out.flush();

            id = getS().getLocalSocketAddress().toString();
            Scanner kb = new Scanner(System.in);

            new Thread(new MessageHandleThread(in)).start();

            mainLoop:
            while (true) {
                String input = kb.nextLine();
                String[] command = input.split(" ");

                switch (command[0].toLowerCase()) {
                    case "#help":
                        helper();
                        break;
                    case "#join":
                        Map<String, Object> joinMap = new HashMap<>();
                        joinMap.put("type", "join");
                        joinMap.put("roomId", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap));
                        out.flush();
                        break;
                    case "#who":
                        if (ChatPeer.roomList.containsKey(command[1])) {
                            Map<String, Object> whoMap = new HashMap<>();
                            whoMap.put("type", "who");
                            whoMap.put("roomId", command[1]);
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(whoMap));
                            out.flush();
                        } else {
                            System.out.println("Room " + command[1] + " does not exist.");
                        }
                        break;
                    case "#list":
                        Map<String, Object> listMap = new HashMap<>();
                        listMap.put("type", "list");
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(listMap));
                        out.flush();
                        break;
                    case "#listneighbors":
                        Map<String, Object> listNeighborsMap = new HashMap<>();
                        listNeighborsMap.put("type", "listneighbors");
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(listNeighborsMap));
                        out.flush();
                        break;
                    case "#shout":
                        Map<String, Object> shoutMap = new HashMap<>();
                        shoutMap.put("type", "shout");
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(shoutMap));
                        out.flush();
                        break;
                    case "#quit":
                        Map<String, Object> quitMap = new HashMap<>();
                        quitMap.put("type", "quit");
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(quitMap));
                        out.flush();
                        break mainLoop;
                    default:
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("type", "message");
                        messageMap.put("content", input);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageMap));
                        out.flush();
                        break;
                }
            }
            in.close();
            out.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }
}
