# tcpip-sample
Simple Java socket application

## Correct flow
1. Run Server.java
2. Run Client.java
3. send messages

### Lost connection
If client close connection, server going back to start state (waiting connection).
If server close connection, client is finish connection.

## Wrong flow
If run Client.java first, will display "ERROR, cannot connect the server"
