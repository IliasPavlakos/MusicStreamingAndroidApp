//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Node;
import java.io.IOException;
import java.net.Socket;

/**
 * The type Node.
 */
public class Node {
    /**
     * The Id.
     */
//fields----------------------------------------------------------------------------<
    protected int id;

    //Constructors----------------------------------------------------------------------<

    /**
     * Default Constructor
     * Instantiates a new Node.
     */

    public Node(){}

    //methods---------------------------------------------------------------------------<

    /**
     *
     * Init.
     * Give this node an id.
     * @param id the id
     */

    public void init(int id) {
        this.id = id;
    }

    /**
     * Connect socket.
     * Connect to the node with the given address and port. Return a socket.
     * @param address the address
     * @param port    the port
     * @return the socket
     */

    public Socket connect(String address, int port) {
        Socket connection = null;
        try {
            connection = new Socket(address, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Disconnect.
     * Close the connection.
     * @param socket the socket
     */

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
