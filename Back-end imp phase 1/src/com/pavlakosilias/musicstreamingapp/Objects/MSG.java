//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Objects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* This class represents a message send between different nodes.
 * Message types:
 *
 *      1. Failure:             used by any node to notify that it failed to respond.
 *      2. Register:            used by consumer and publisher to register to a broker.
 *      3. RegisterResponse:    used by broker to respond to a publisher registered.
 *      4. Pull:                used by broker to get data from a publisher
 *      5. Push:                used by publisher to send data to a broker
 *      6. Request:             used by consumer to request data from a broker.
 *      7. Response:            used by publisher to respond to a broker's request.
 *      8. Response:            used by broker to respond to a consumer's request.
 *      9. Query:               used by consumer to question a broker if he is in charge of an artist
 *      10.Request:             used by consumer to request to broker for artist list.
 *      11.Response:            used by broker to respond to consumer artist list request.
 *
 *  If the message doesn't make use of one of the field, that field must be null or 0;
 */
public class MSG implements Serializable {
    //fields----------------------------------------------------------------------------<

    //General
    public String type = null;
    public String sender = null;

    //Only for broker - publisher communication.
    public int id = 0;
    public int key = 0;

    public int port = 0; // 2
    public String ip = null; // 2
    public ArrayList<ArtistName> artistList = null;  // 2
    public ArtistName artistName = null; // 2, 6, 9
    public Value value = null; // 4, 5, 6
    public ArrayList<MusicFile> album = null; // 5
    public ArrayList<MusicFile> chunkList = null;  // 7
    public int totalBytes = 0; // 7, 8
    public int totalChunks = 0;  // 7, 9
    public int totalBrokers = 0; // 3
    public List<MSG> availableBrokers = null; // 3
    public boolean response = false; // 9
    public MusicFile chunk = null; // 8

    //Constructors----------------------------------------------------------------------<

    //1. General failure message.
    public MSG(){
        this.type = "Failure";
    }

    //2. Consumer Register to broker
    public MSG(int id){
        this.type = "Register";
        this.sender = "Consumer";
        this.id = id;
    }

    //2. Publisher Register to broker
    public MSG(int id, String ip, int port, ArrayList<ArtistName> artistList){
        this.type = "Register";
        this.sender = "Publisher";
        this.id = id;
        this.port = port;
        this.ip = ip;
        this.artistList = artistList;
    }

    //2. Broker's system info, this message is used for broker's local storage and not for communication
    public MSG(int id,int key, int port, String ip){
        this.type = "Storage";
        this.sender = "None";
        this.id = id;
        this.key = key;
        this.port = port;
        this.ip = ip;
    }

    //3. Broker's response to publisher's register
    public MSG(int id,int key){
        this.type = "RegisterResponse";
        this.sender = "Broker";
        this.id = id;
        this.key = key;
    }

    //3. Broker's response to consumer's register
    public MSG(int totalBrokers, List<MSG> availableBrokers){
        this.type = "RegisterResponse";
        this.sender = "Broker";
        this.totalBrokers = totalBrokers;
        this.availableBrokers = availableBrokers;
    }

    //4. Broker pull(song) request to publisher
    public MSG(int id, ArtistName artistName, Value value){
        this.type = "Pull";
        this.sender = "Broker";
        this.id = id;
        this.artistName = artistName;
        this.value = value;

    }

    //4. Broker pull(album) request to publisher
    public MSG(int id, ArtistName artistName){
        this.type = "Pull";
        this.sender = "Broker";
        this.id = id;
        this.artistName = artistName;
    }

    //5. Publisher push(album) to broker
    public MSG(ArrayList<MusicFile> album){
        this.type = "Push";
        this.sender = "Publisher";
        this.album = album;
    }

    //6. Consumer request to broker.
    public MSG(ArtistName artistName, Value value){
        this.type = "Request";
        this.sender = "Consumer";
        this.artistName = artistName;
        this.value = value;
    }

    //7. Publishers response to a broker's request.
    public MSG(int totalChunks, int totalBytes, ArrayList<MusicFile> chunkList){
        this.type = "Response";
        this.sender = "Broker";
        this.totalChunks = totalChunks;
        this.totalBytes = totalBytes;
        this.chunkList = chunkList;
    }

    //8. Broker's pre-response to consumer song request.
    public MSG(int totalChunks, int totalBytes, MusicFile chunk){
        this.type = "Response";
        this.sender = "Broker";
        this.totalChunks = totalChunks;
        this.totalBytes = totalBytes;
        this.chunk = chunk;
    }

    //8. Broker actual-response to consumer's song request
    public MSG(MusicFile chunk){
        this.type = "Response";
        this.sender = "Broker";
        this.chunk = chunk;
    }

    //9. Consumer query to broker
    //9. broker's response to consumer's query
    public MSG(ArtistName artistName, boolean response){
        this.type = "Query";
        this.artistName = artistName;
        this.response = response; //This is set to true by the broker in charge of the input artist.
    }


    //Phase 2 Implementations-----------------------------------------------------------<

    //10. consumer request to broker for artist list.
    public MSG(Void initial){
        this.type = "Request";
        this.sender = "Consumer";
    }

    //11. Broker response to consumer artist list request.
    public MSG(Void initial, ArrayList<ArtistName> artistList){
        this.type = "Response";
        this.sender = "Broker";
        this.artistList = artistList;
    }


}
