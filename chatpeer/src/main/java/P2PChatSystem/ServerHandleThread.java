package P2PChatSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ServerHandleThread implements Runnable {
    Socket s;

    public ServerHandleThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            ObjectMapper mapper = new ObjectMapper();

            roomListMessage(out, mapper);
            out.flush();

            DataInputStream in = new DataInputStream(s.getInputStream());

            mainLoop:
            while (true) {
                String require = in.readUTF();
                Command command = mapper.readValue(require, Command.class);

                //handle command messages send by clients
                switch (command.getType()) {
                    case "hostchange":
                        if (!ChatPeer.neighbors.containsValue(command.getHost())) {
                            ChatPeer.neighbors.put(s, command.getHost());
                        }
                        break;
                    case "join":
                        //find the client's current room
                        String former = "";
                        for (String room : ChatPeer.roomList.keySet()) {
                            if (ChatPeer.roomList.get(room).contains(s)) {
                                former = room;
                            }
                        }

                        //room existed and doesn't equal to the current one
                        if (ChatPeer.roomList.containsKey(command.getRoomId()) && !Objects.equals(former, command.getRoomId())) {
                            Map<String, Object> joinMap1 = new HashMap<>();
                            joinMap1.put("type", "roomchange");
                            joinMap1.put("identity", ChatPeer.socketList.get(s));
                            joinMap1.put("former", former);
                            joinMap1.put("roomId", command.getRoomId());
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));

                            //broadcast RoomChange message to all the clients in the former room
                            if (!Objects.equals(former, "")) {
                                ArrayList<Socket> formerRoom = ChatPeer.roomList.get(former);
                                formerRoom.remove(s);
                                for (Socket s : formerRoom) {
                                    DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                    outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                                    outputStream.flush();
                                }
                            }

                            //broadcast RoomChange message to all the client in the new room
                            for (Socket s : ChatPeer.roomList.get(command.getRoomId())) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                                outputStream.flush();
                            }
                            out.flush();

                            //remove client from former room
                            //add client to the new room
                            if (!Objects.equals(former, "")) {
                                ChatPeer.roomList.get(former).remove(s);
                            }
                            ChatPeer.roomList.get(command.getRoomId()).add(s);
                        } else {
                            Map<String, Object> joinMap2 = new HashMap<>();
                            joinMap2.put("type", "roomchange");
                            joinMap2.put("identity", ChatPeer.socketList.get(s));
                            joinMap2.put("former", former);
                            joinMap2.put("roomId", former);
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap2));
                            out.flush();
                        }
                        break;
                    case "who":
                        roomContentMessage(out, mapper, command.getRoomId());
                        out.flush();
                        break;
                    case "list":
                        roomListMessage(out, mapper);
                        out.flush();
                        break;
                    case "listneighbors":
                        HashMap<Socket, String> tempHost = ChatPeer.neighbors;
                        tempHost.remove(s);

                        Map<String, Object> neighbors = new HashMap<>();
                        neighbors.put("neighbors", tempHost.values().toString());
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(neighbors));
                        out.flush();
                        break;
                    case "quit":
                        quit();
                        break mainLoop;
                    case "message":
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "message");
                        message.put("identity", ChatPeer.socketList.get(s));
                        message.put("content", command.getContent());

                        String roomId = null;
                        for (String room : ChatPeer.roomList.keySet()) {
                            if (ChatPeer.roomList.get(room).contains(s)) {
                                roomId = room;
                            }
                        }

                        if (roomId != null) {
                            for (Socket socket : ChatPeer.roomList.get(roomId)) {
                                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
                                outputStream.flush();
                            }
                        } else {
                            out.flush();
                        }
                        break;
                    case "shout":
                        Map<String, Object> shout = new HashMap<>();
                        if (command.getIdentity() == null) {
                            shout.put("type", "shout");
                            shout.put("identity", ChatPeer.socketList.get(s));

                            shout(mapper, shout);
                        } else {
                            shout.put("type", "shout");
                            shout.put("identity", command.getIdentity());

                            shout(mapper, shout);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void shout(ObjectMapper mapper, Map<String, Object> shout) throws IOException {
        for (String room : ChatPeer.roomList.keySet()) {
            for (Socket s : ChatPeer.roomList.get(room)) {
                    DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                    outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(shout));
                    outputStream.flush();
            }
        }

        if (ClientThread.connectStat) {
            String server = ClientThread.connectSocket.getRemoteSocketAddress().toString();
            if (!server.equals(ClientThread.connectSocket.getLocalAddress() + ":" + ChatPeer.listenPort)) {
                DataOutputStream connectOut = new DataOutputStream(ClientThread.connectSocket.getOutputStream());
                connectOut.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(shout));
                connectOut.flush();
            }
        }
    }

    //generate RoomList message
    private void roomListMessage(DataOutputStream out, ObjectMapper mapper) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "roomlist");
        Map<String, Object> innerMap = new HashMap<>();
        for (String room : ChatPeer.roomList.keySet()) {
            innerMap.put(room, ChatPeer.roomList.get(room).size());
        }
        map.put("rooms", innerMap);
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
    }

    //generate RoomContents message
    private void roomContentMessage(DataOutputStream out, ObjectMapper mapper, String roomId) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "roomcontents");
        map.put("roomId", roomId);
        List<String> roomMember = new ArrayList<>();
        for (Socket socket : ChatPeer.roomList.get(roomId)) {
            roomMember.add(ChatPeer.socketList.get(socket));
        }
        map.put("identities", roomMember);
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
    }

    private void quit() {
        for (String room : ChatPeer.roomList.keySet()) {
            ChatPeer.roomList.get(room).remove(s);
        }
        ChatPeer.socketList.remove(s);
        ChatPeer.neighbors.remove(s);
    }
}
