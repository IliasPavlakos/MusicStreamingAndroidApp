//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Node;
import java.io.IOException;
import java.net.Socket;

public class Node {
    //fields----------------------------------------------------------------------------<
    public int id;

    //Constructors----------------------------------------------------------------------<

    //Default Constructor
    public Node(){}

    //methods---------------------------------------------------------------------------<

    //Give this node an id.
    public void init(int id) {
        this.id = id;
    }

    //Connect to the node with the given address and port. Return a socket.
    public Socket connect(String address, int port) {
        Socket connection = null;
        try {
            connection = new Socket(address, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    //Close the connection.
    public void disconnect(Socket socket) {
        if(socket == null) System.out.println("Warning on node.disconnect():" +
                " Attempted to close a non-existing socket.");
        else {
            try {
                socket.close();
            } catch (IOException s) {
                s.printStackTrace();
            }
        }
    }

}
