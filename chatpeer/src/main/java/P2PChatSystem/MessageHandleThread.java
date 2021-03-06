package P2PChatSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageHandleThread extends Thread {
    private DataInputStream in;

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public DataInputStream getIn() {
        return in;
    }

    public MessageHandleThread(DataInputStream in) {
        setIn(in);
    }

    @Override
    public void run() {
            try {
                while (true) {
                    System.out.printf("[%s] %s>", ConnectionHandler.roomId, ConnectionHandler.id);
                    String content = getIn().readUTF();
                    handleContent(content);
                }
            } catch (Exception e) {
                //do nothing
            }
    }

    private void handleContent(String content) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Packet packet = mapper.readValue(content, Packet.class);

        System.out.println("\nReceived:");

        if (packet.getNeighbors() != null && packet.getType() == null) {
            System.out.println(packet.getNeighbors());
        } else {
            switch (packet.getType()) {
                case "roomlist":
                    if (packet.getRooms().isEmpty()) {
                        System.out.println("Not create a room yet.");
                    } else {
                        for (String room : packet.getRooms().keySet()) {
                            System.out.println(room + ": " + packet.getRooms().get(room) + " users");
                        }
                    }
                    break;
                case "roomchange":
                    if (Objects.equals(packet.getFormer(), "")) {
                        System.out.println(packet.getIdentity() + " moves to " + packet.getRoomId());
                    } else {
                        System.out.println(packet.getIdentity() + " moved from " + packet.getFormer() + " to " + packet.getRoomId());
                    }
                    ConnectionHandler.roomId = packet.getRoomId();
                    break;
                case "roomcontents":
                    System.out.println(packet.getRoomId() + " contains " + packet.getIdentities());
                    break;
                case "message":
                    System.out.println(packet.getIdentity() + ": " + packet.getContent());
                    break;
                case "shout":
                    System.out.println(packet.getIdentity() + " shouted");

                    Map<String, Object> shout = new HashMap<>();
                    shout.put("type", "shout");
                    shout.put("identity", packet.getIdentity());
                    for (String room : ChatPeer.roomList.keySet()) {
                        for (Socket s : ChatPeer.roomList.get(room)) {
                            if (!s.getLocalSocketAddress().toString().equals(s.getLocalAddress() + ":" + ChatPeer.listenPort)){
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(shout));
                                outputStream.flush();
                            }
                        }
                    }
                    break;
            }
        }
    }
}
