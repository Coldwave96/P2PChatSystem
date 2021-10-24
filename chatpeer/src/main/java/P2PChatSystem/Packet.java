package P2PChatSystem;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(value = {"other"})

/*
  The Packet class is used for generating response messages objects. And Jackson helps to
  transform the JSON format String which send by clients to the Packet object. The structure
  of response messages are shown in the README.md file.
 */
public class Packet {
    private String type; //message type
    private String identity; //client identity
    private String roomId; //room id
    private String former; //former room id
    private String content; //message content

    private List<String> identities = new ArrayList<>(); //all clients in the room
    private Map<String, Object> rooms = new HashMap<>(); //room list
    private String neighbors; //all neighbors

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setFormer(String former) {
        this.former = former;
    }

    public String getFormer() {
        return former;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public List<String> getIdentities() {
        return identities;
    }

    public void setIdentities(List<String> identities) {
        this.identities = identities;
    }

    public Map<String, Object> getRooms() {
        return rooms;
    }

    public void setRooms(Map<String, Object> rooms) {
        this.rooms = rooms;
    }

    public String getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(String neighbors) {
        this.neighbors = neighbors;
    }

    private Map<String, Object> other = new HashMap<>(); //other field

    @JsonAnySetter
    public void setOther(String key, Object value) {
        other.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getOther() {
        return other;
    }
}
