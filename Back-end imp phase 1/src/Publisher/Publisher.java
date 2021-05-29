//Implementation: Ηλίας Παυλάκος
package Publisher;

import com.pavlakosilias.musicstreamingapp.Node.Node;
import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;
import com.pavlakosilias.musicstreamingapp.Objects.MSG;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;
import com.pavlakosilias.musicstreamingapp.Objects.Value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static Publisher.ScanAndHash.ArtistHasher.assignHashes;
import static Publisher.ScanAndHash.mp3DataExtractor.scanDataSet;

public class Publisher extends Node {
    //fields----------------------------------------------------------------------------<
    private ServerSocket server = null;
    public ArrayList<ArtistName> artistList = new ArrayList<ArtistName>();
    protected ArrayList<ArrayList<MusicFile>> albumList = new ArrayList<ArrayList<MusicFile>>();
    private ArrayList<MSG> availableBrokers = new ArrayList<MSG>();
    private int port;
    private String IP;

    //Constructors----------------------------------------------------------------------<
    public Publisher(int id, String IP, int port, char fromArtist, char toArtist, String dataSetPath){
        super();
        this.id = id;
        this.IP = IP;
        this.port = port;
        char from = Character.toLowerCase(fromArtist);
        char to = Character.toLowerCase(toArtist);
        scanDataSet(from, to, dataSetPath, artistList, albumList);
        assignHashes(artistList);
    }

    //methods---------------------------------------------------------------------------<

    //Sends a Register message and waits for a response containing the broker's key.
    public void register(String address, int port) {
        System.out.print("Registering to broker " + (port - 5000) + "...");
        try {
            //Connect to broker.
            Socket connection = new Socket(address, port);

            //Send a register message.
            ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
            MSG msg = new MSG(id, IP, this.port, artistList);
            output.writeObject(msg);

            //Wait for response
            ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
            MSG response = (MSG) input.readObject();

            //Check response format.
            if(response.type.equals("RegisterResponse") && response.sender.equals("Broker") && response.key != 0) {
                availableBrokers.add(response);
                System.out.println("....Done!");
            }else{
                System.out.println("Failed to register on broker" + (port - 5000));
            }

            //Clean up.
            output.close();
            input.close();
            connection.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Sends the data requested.
    public void push(Socket connection, ArtistName artistName, Value value) {
        if(artistName == null) System.out.println("Warning at Publisher.push: null artistName");

        //If its a song request.
        if(value != null){
            String songPath = null;

            //If the user sent the actual metadata get the path
            if(value.getMusicFile().path != null) {
                //Get song path
                songPath = value.getMusicFile().path;

            //Else, find and get the actual music file.
            }else {
                //Get actual musicFile
                String songName = value.getMusicFile().trackName;
                for(ArrayList<MusicFile> album : albumList){
                    for(MusicFile song : album){
                        if(song.trackName.equals(songName)){
                            songPath = song.path;
                            break;
                        }
                    }
                    if(songPath != null) break;
                }
            }

            //Create and start a new thread to send the song.
            Pusher pusher = new Pusher(connection, songPath);
            pusher.start();
        }else{
            //Check if this publisher is indeed responsible for this artist
            String artistToCheck = artistName.getArtistName();
            boolean isInCharge = false;
            for(ArrayList<MusicFile> album : albumList){
                if(artistToCheck.equals(album.get(0).artistName)){
                    isInCharge = true;
                    break;
                }
            }
            if(isInCharge = false){
                System.out.println("Error at publisher.push : NOT IN CHARGE");
            }

            //Get the requested album
            ArrayList<MusicFile> albumToSend = null;
            String keyArtist = artistName.getArtistName();
            for(ArrayList<MusicFile> album : albumList){
                if(keyArtist.equals(album.get(0).artistName)){
                    albumToSend = album;
                    break;
                }
            }

            //Create and start a new thread to send the album
            Pusher pusher = new Pusher(connection, albumToSend);
            pusher.start();
        }
    }

    //Server----------------------------------------------------------------------------<
    public void openServer(String address, int port){
        try{
            server = new ServerSocket(port);
            System.out.println("Server started");

            //noinspection InfiniteLoopStatement
            while(true){
                System.out.print("Awaiting requests...");

                //Wait for brokers to connect
                Socket connection = server.accept();

                //Get message
                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                MSG request = (MSG) input.readObject();

                //Check request
                if(request.type.equals("Pull") && request.sender.equals("Broker")) {
                    System.out.println("received broker's " + request.id + " request.");

                    //Check if the current connection is the appropriate to reply
                    for(MSG broker : availableBrokers){
                        if(broker.key <= request.key){
                            if(broker.id == request.id) break;
                            else throw new IOException("False connection on publisher");
                        }
                    }

                    //Handle request.
                    push(connection, request.artistName, request.value);

                }else System.out.println("Unexpected request!");
            }//End of while


        }catch (IOException | ClassNotFoundException i){
            i.printStackTrace();
        } finally {
            try{
                server.close();
            }catch(IOException i){
                i.printStackTrace();
            }
        }//End of try

    } //End of OpenServer


}//End of Publisher
