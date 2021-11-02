# P2PChatSystem

Transform the existing chat program into a decentralized chat application.

## Usage
```
# -p [port] - port that the peer listens on for incomming connections - optional (default port 4444)
# -i [port] - e port that the peer uses to make connections to other peers - optional (random default port)
java -jar chatpeer.jar -p [port] -i [port]
```

## Commands
Do not use #connect command to connect to other peers
```
#help - list this information
#connect IP[:port] [local port] - connect to another peer
#createroom ROOM - create a chat room
#list - list all the chat room in the peer
#who ROOM - list all members in the chat room
#kick USER - kick the user and block he or she from reconnecting
#delete ROOM - delete a chat room
#listneighbors - request the server to list its neighbors
#searchnetwork - list chat rooms over all accessible peers
#quit - quit the system
```

After connected to another peer
```
#help - list this information
#join ROOM - join a chat room
#who ROOM - request a member list of the chat room
#list - request a room list of the current peer
#listneighbors - request the server to list its neighbors
#shout - delivery message to all rooms on all peers of the network
#quit - disconnect from the peer
message - all the input other than the commands below
```
