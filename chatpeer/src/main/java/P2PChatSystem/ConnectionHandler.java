package P2PChatSystem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class ConnectionHandler {
    private Socket s;
    public String roomId;
    private String id;

    public void setS(Socket s) {
        this.s = s;
    }

    public Socket getS() {
        return s;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

            setId(getS().getLocalSocketAddress().toString());
            Scanner kb = new Scanner(System.in);

            mainLoop:
            while (true) {
                while (true) {
                    try {
                        String content = in.readUTF();
                        handleContent(content);
                    } catch (Exception e) {
                        break;
                    }
                }

                System.out.printf("[%s] %s>", getRoomId(), getId());
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
                        Map<String, Object> whoMap = new HashMap<>();
                        whoMap.put("type", "who");
                        whoMap.put("roomId", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(whoMap));
                        out.flush();
                        break;
                    case "#list":
                        Map<String, Object> listMap = new HashMap<>();
                        listMap.put("type", "list");
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(listMap));
                        out.flush();
                        break;
                    case "#listneighbors":
                        break;
                    case "#shout":
                        break;
                    case "#quit":
                        break mainLoop;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(0);
        }
    }

    private void handleContent(String content) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Packet packet = mapper.readValue(content, Packet.class);

        switch (packet.getType()) {
            case "roomlist":
                System.out.println("Room list:");
                for (String room : packet.getRooms().keySet()) {
                    System.out.println(room + ": " + packet.getRooms().get(room) + " guests");
                }
                break;
            case "roomchange":
                if (Objects.equals(packet.getFormer(), "")) {
                    System.out.println(packet.getIdentity() + " moves to " + packet.getRoomId());
                } else {
                    System.out.println(packet.getIdentity() + " moved from " + packet.getFormer() + " to " + packet.getRoomId());
                }
                setRoomId(packet.getRoomId());
                break;
            case "roomcontents":
                System.out.println(packet.getRoomId() + " contains " + packet.getIdentities());
                break;
            case "message":
                System.out.println(packet.getIdentity() + ": " + packet.getContent());
                break;
        }
    }
}
