//Implementation: Ηλίας Παυλάκος
package Broker;

import com.pavlakosilias.musicstreamingapp.Node.Node;
import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;
import com.pavlakosilias.musicstreamingapp.Objects.MSG;
import com.pavlakosilias.musicstreamingapp.Objects.Value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static Publisher.ScanAndHash.ArtistHasher.getMd5;

public class Broker extends Node {
    //fields----------------------------------------------------------------------------<
    private int key;
    private ArrayList<MSG> registeredUsers;
    private ArrayList<MSG> registeredPublishers;
    private ArrayList<MSG> availableBrokers;
    private ServerSocket   server;

    //Constructors----------------------------------------------------------------------<

    //Constructor
    public Broker(int id, String[] info){
        super();
        this.id = id;
        registeredPublishers = new ArrayList<MSG>();
        registeredUsers = new ArrayList<MSG>();
        availableBrokers = new ArrayList<MSG>();

        //Get brokers info from program arguments and generate keys.
        for(int i = 0; i < info.length; i += 3){
            int brokerKey = generateKey(Integer.parseInt(info[i + 1]), info[i + 2]);
            MSG brokerInfo = new MSG(Integer.parseInt(info[i]), brokerKey, Integer.parseInt(info[i + 1]), info[i + 2]);
            availableBrokers.add(brokerInfo);
            if(brokerInfo.id == this.id) key = brokerInfo.key;
        }
        System.out.println("Broker " + this.id + " created with key " + key);
    }

    //methods---------------------------------------------------------------------------<

    //Returns an MD5 hash code for this broker.
    public int generateKey(int port, String brokerIP) {
        //Convert system IP to decimal number
        int[] ip = new int[4];
        String[] parts = brokerIP.split("\\.");
        for (int i = 0; i < 4; i++) {
            ip[i] = Integer.parseInt(parts[i]);
        }
        long n = 0;
        for (int i = 0; i < 4; i++) {
            n += ip[i] << (24 - (8 * i));
        }

        //Get MD5 hash
        n = n + port;
        String out = String.valueOf(n);
        BigInteger MD5hash = getMd5(out);

        //Calculate the final hash code of the broker.
        int finalHash = MD5hash.intValue();
        if (finalHash < 0) finalHash = -1 * finalHash;
        finalHash = finalHash % 17;

        return finalHash;
    }

    //Notify publishers for what artists this broker is responsible.
    public void notifyPublisher(Socket connection) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());

            //Send this broker's hash code to the publisher
            MSG msg = new MSG(this.id, this.key);
            output.writeObject(msg);

            //Close the stream
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Notify consumer consumer for the available brokers.
    public void notifyConsumer(Socket connection){
        try {
            ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());

            //Send the available brokers and the artists they are in charge
            ArrayList<String> artists = new ArrayList<String>();
            MSG msg = new MSG(availableBrokers.size(), (List<MSG>) availableBrokers);
            output.writeObject(msg);

            //Close the stream
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Gets data from a publisher and forwards that data to the consumer who requested them.
    public void pull(Socket connection, ArtistName artistName, Value value) {
            try {
                int pID = 0;
                String pIP = null;
                int pPort = 0;


                //If its a normal request
                if (artistName != null) {

                    //Get publisher in charge of the input artist
                    for (MSG publisher : registeredPublishers) {
                        for (ArtistName artist : publisher.artistList) {
                            String str = artist.getArtistName();
                            if (artistName.getArtistName().equals(str)) {
                                pID = publisher.id;
                            }
                            if(pID != 0) break;
                        }
                        //If found, break
                        if (pID != 0) break;
                    }

                    //Connect to publisher in charge
                    for (MSG publisher : registeredPublishers) {
                        if (pID == publisher.id) {
                            pIP = publisher.ip;
                            pPort = publisher.port;
                        }
                        if(pIP != null && pPort != 0) break;
                    }


                }else{
                    //its the initial request
                    pID = registeredPublishers.get(0).id;
                    pIP = registeredPublishers.get(0).ip;
                    pPort = registeredPublishers.get(0).port;
                }

                if (pID == 0 || pIP == null || pPort == 0) {
                    System.out.println("Warning at Broker.pull(): Null id || IP || port for publisher " + pID);
                    throw new IOException("e");
                }


                if (artistName != null) {
                    Socket publisherConnection = null;

                    try {
                        publisherConnection = new Socket(pIP, pPort);
                    }catch(IOException e){
                        MSG failed = new MSG();
                        ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                        output.writeObject(failed);
                        System.out.println("Connection declined: publisher are down");
                        return;
                    }

                    //Create a new puller thread to handle the request
                    Puller puller = null;

                    //if song request
                    if (value != null) {
                        System.out.println("Received consumer song request for artist " + artistName.getArtistName());
                        puller = new Puller(id, connection, publisherConnection, artistName, value);
                    } else {
                        //if album request
                        System.out.println("Received consumer album request for artist " + artistName.getArtistName());
                        puller = new Puller(id, connection, publisherConnection, artistName);
                    }

                    //Start the thread
                    puller.start();


                //else, its the initial artist list request.
                }else{
                    System.out.println("Received consumer artist list request.");
                    ArrayList<ArtistName> artistList = new ArrayList<ArtistName>();
                    for(MSG publisher : registeredPublishers){
                        artistList.addAll(publisher.artistList);
                    }
                    MSG response = new MSG(null, artistList);
                    ObjectOutputStream cOut = new ObjectOutputStream(connection.getOutputStream());
                    cOut.writeObject(response);

                    cOut.close();
                    connection.close();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    //Stores the registration msg of a consumer inside this broker's registeredConsumers list.
    public void acceptConsumerConnection(MSG msg) {
        registeredUsers.add(msg);
        System.out.println("Consumer " + msg.id + " registered!");
    }

    //Stores the registration msg of a publisher inside this broker's registeredPublishers list.
    public void acceptPublisherConnection(MSG msg) {
        registeredPublishers.add(msg);
        System.out.println("Publisher " + msg.id + " registered!");
    }

    //Sends true if this broker is responsible for the artist the consumer provided, else sends false.
    public void answerQuery(Socket connection, MSG query){
        try {
            int artistKey = 0;
            String q = query.artistName.getArtistName();

            //Get the artist key
            for (MSG publisher : registeredPublishers) {
                for (ArtistName artist : publisher.artistList) {
                    String str = artist.getArtistName();
                    if (q.equals(str)){
                        artistKey = artist.getArtistKey();
                    }
                    if(artistKey != 0) break;
                }
                if (artistKey != 0) break;
            }




            //Determine if this broker is in charge of the artist
            boolean isInCharge = false;
            boolean otherInCharge = false;


            if(artistKey <= this.key){
                //for every other broker
                for(MSG b : availableBrokers){
                    if(b.key != this.key){
                        //if other in charge
                        if(b.key < this.key && artistKey <= b.key) otherInCharge = true;
                    }
                }
                if(otherInCharge) isInCharge = false;
                else isInCharge = true;
            }else{
                int maxKey = this.key;
                //for every other broker
                for(MSG b : availableBrokers){
                    if(b.key != this.key){
                        if(maxKey < b.key) otherInCharge = true;
                    }
                }
                if(!otherInCharge) isInCharge = true;
            }

            //Send query answer
            query.response = isInCharge;
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(query);
            System.out.println("query");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Server----------------------------------------------------------------------------<
    public void openServer(String address, int port){
        try{
            System.out.println("Server Started");
            server = new ServerSocket(port);

            //noinspection InfiniteLoopStatement
            while(true){
                //Wait for publishers or Consumers to connect.
                System.out.print("Awaiting requests...");
                Socket connection = server.accept();

                //Get msg
                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                MSG msg = (MSG) input.readObject();




                //Check if it's a Register request
                if (msg.type.equals("Register")) {
                    //Register the new node.
                    if (msg.sender.equals("Consumer")) {
                        //Decline requests if publishers are down.
                        if(registeredPublishers.size() <= 2) {
                            MSG failed = new MSG();
                            ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
                            output.writeObject(failed);
                            System.out.println("Connection declined: publisher are down");
                        }else {
                            //Else, register consumer
                            acceptConsumerConnection(msg);
                            notifyConsumer(connection);
                        }
                    } else if (msg.sender.equals("Publisher")) {
                        acceptPublisherConnection(msg);
                        notifyPublisher(connection);
                    }

                    //Check if it's a consumer request
                } else if (msg.type.equals("Request") && msg.sender.equals("Consumer")) {

                        pull(connection, msg.artistName, msg.value);

                    //Check if its a consumer query
                } else if (msg.type.equals("Query")) {
                    answerQuery(connection, msg);
                }


            }//End of while
        } catch (IOException | ClassNotFoundException i){
            i.printStackTrace();
        } finally {
            try{
                server.close();
            }catch(IOException i){
                i.printStackTrace();
            }
        }//End of try

    }//End of OpenServer
    

}//End of Broker


