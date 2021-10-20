package P2PChatSystem;

import java.net.Socket;
import java.util.Scanner;

public class ClientHandleThread implements Runnable {
    private Socket s;
    private String roomId;
    private String id;

    public void setS(Socket s) {
        this.s = s;
    }

    public Socket getS() {
        return s;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ClientHandleThread(Socket socket) {
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

    @Override
    public void run() {
        setId(getS().getLocalSocketAddress().toString());
        Scanner kb = new Scanner(System.in);

        mainLoop:
        while (true) {
            System.out.printf("[%s] %s>", getRoomId(), getId());
            String input = kb.nextLine();
            String[] command = input.split(" ");

            switch (command[0].toLowerCase()) {
                case "#help":
                    helper();
                    break;
                case "#join":
                    break;
                case "who":
                    break;
                case "#list":
                    break;
                case "listneighbors":
                    break;
                case "#shout":
                    break;
                case "#quit":
                    break;
                default:
                    break;
            }
        }
    }
}
