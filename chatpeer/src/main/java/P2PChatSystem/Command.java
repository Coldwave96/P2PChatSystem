package P2PChatSystem;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(value = {"other"})

/*
  The Command class is used for generating command message objects when user enter a command.
  Then the command message will be sent to the chat server via JSON format string. Jackson is
  used to transform JSON string to a Command object. Some values field will be reused in
  different command message. The structure of command messages are shown in the README.md file.
 */
public class Command {
    private String type; //message type
    private String roomId; //room id
    private String content; //message content

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private Map<String, Object> other = new HashMap<>();

    @JsonAnySetter
    public void setOther(String key, Object value) {
        other.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getOther() {
        return other;
    }
}