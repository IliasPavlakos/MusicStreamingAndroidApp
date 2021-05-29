//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Objects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/* This class represents a message send between different nodes.
 * Message types:
 *      1. Failure:             used by any node to notify that it failed to respond.
 *      2. Register:            used by consumer and publisher to register to a broker.
 *      3. RegisterResponse:    used by broker to respond to a publisher registered.
 *      4. Pull:                used by broker to get data from a publisher
 *      5. Push:                used by publisher to send data to a broker
 *      6. Request:             used by consumer to request data from a broker.
 *      7. Response:            used by publisher to respond to a broker's request.
 *      8. Response:            used by broker to respond to a consumer's request.
 *      9. Query:               used by consumer to question a broker if he is in charge of an artist
 *  If the message doesn't make use of one of the field, that field must be null or 0;
 */

/**
 * The type Msg.
 */
public class MSG implements Serializable {
    //fields----------------------------------------------------------------------------<

    /**
     * The Type.
     */
//General
    public String type = null;
    /**
     * The Sender.
     */
    public String sender = null;

    /**
     * The Id.
     */
//Only for broker - publisher communication.
    public int id = 0;
    /**
     * The Key.
     */
    public int key = 0;

    /**
     * The Port.
     */
    public int port = 0; // 2
    /**
     * The Ip.
     */
    public String ip = null; // 2
    /**
     * The Artist list.
     */
    public ArrayList<ArtistName> artistList = null;  // 2
    /**
     * The Artist name.
     */
    public ArtistName artistName = null; // 2, 6, 9
    /**
     * The Value.
     */
    public Value value = null; // 4, 5, 6
    /**
     * The Album.
     */
    public ArrayList<MusicFile> album = null; // 5
    /**
     * The Chunk list.
     */
    public ArrayList<MusicFile> chunkList = null;  // 7
    /**
     * The Total bytes.
     */
    public int totalBytes = 0; // 7, 8
    /**
     * The Total chunks.
     */
    public int totalChunks = 0;  // 7, 9
    /**
     * The Total brokers.
     */
    public int totalBrokers = 0; // 3
    /**
     * The Available brokers.
     */
    public List<MSG> availableBrokers = null; // 3
    /**
     * The Response.
     */
    public boolean response = false; // 9
    /**
     * The Chunk.
     */
    public MusicFile chunk = null; // 8

    //Constructors----------------------------------------------------------------------<

    /**
     * Constructor
     * Instantiates a new Msg.
     */
//1. General failure message.
    public MSG(){
        this.type = "Failure";
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param id the id
     */
//2. Consumer Register to broker
    public MSG(int id){
        this.type = "Register";
        this.sender = "Consumer";
        this.id = id;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param id         the id
     * @param ip         the ip
     * @param port       the port
     * @param artistList the artist list
     */
//2. Publisher Register to broker
    public MSG(int id, String ip, int port, ArrayList<ArtistName> artistList){
        this.type = "Register";
        this.sender = "Publisher";
        this.id = id;
        this.port = port;
        this.ip = ip;
        this.artistList = artistList;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param id   the id
     * @param key  the key
     * @param port the port
     * @param ip   the ip
     */
//2. Broker's system info, this message is used for broker's local storage and not for communication
    public MSG(int id,int key, int port, String ip){
        this.type = "Storage";
        this.sender = "None";
        this.id = id;
        this.key = key;
        this.port = port;
        this.ip = ip;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param id  the id
     * @param key the key
     */
//3. Broker's response to publisher's register
    public MSG(int id,int key){
        this.type = "RegisterResponse";
        this.sender = "Broker";
        this.id = id;
        this.key = key;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param totalBrokers     the total brokers
     * @param availableBrokers the available brokers
     */
//3. Broker's response to consumer's register
    public MSG(int totalBrokers, List<MSG> availableBrokers){
        this.type = "RegisterResponse";
        this.sender = "Broker";
        this.totalBrokers = totalBrokers;
        this.availableBrokers = availableBrokers;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param id         the id
     * @param artistName the artist name
     * @param value      the value
     */
//4. Broker pull(song) request to publisher
    public MSG(int id, ArtistName artistName, Value value){
        this.type = "Pull";
        this.sender = "Broker";
        this.id = id;
        this.artistName = artistName;
        this.value = value;

    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param id         the id
     * @param artistName the artist name
     */
//4. Broker pull(album) request to publisher
    public MSG(int id, ArtistName artistName){
        this.type = "Pull";
        this.sender = "Broker";
        this.id = id;
        this.artistName = artistName;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param album the album
     */
//5. Publisher push(album) to broker
    public MSG(ArrayList<MusicFile> album){
        this.type = "Push";
        this.sender = "Publisher";
        this.album = album;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param artistName the artist name
     * @param value      the value
     */
//6. Consumer request to broker.
    public MSG(ArtistName artistName, Value value){
        this.type = "Request";
        this.sender = "Consumer";
        this.artistName = artistName;
        this.value = value;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param totalChunks the total chunks
     * @param totalBytes  the total bytes
     * @param chunkList   the chunk list
     */
//7. Publishers response to a broker's request.
    public MSG(int totalChunks, int totalBytes, ArrayList<MusicFile> chunkList){
        this.type = "Response";
        this.sender = "Broker";
        this.totalChunks = totalChunks;
        this.totalBytes = totalBytes;
        this.chunkList = chunkList;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param totalChunks the total chunks
     * @param totalBytes  the total bytes
     * @param chunk       the chunk
     */
//8. Broker's pre-response to consumer song request.
    public MSG(int totalChunks, int totalBytes, MusicFile chunk){
        this.type = "Response";
        this.sender = "Broker";
        this.totalChunks = totalChunks;
        this.totalBytes = totalBytes;
        this.chunk = chunk;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param chunk the chunk
     */
//8. Broker actual-response to consumer's song request
    public MSG(MusicFile chunk){
        this.type = "Response";
        this.sender = "Broker";
        this.chunk = chunk;
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param artistName the artist name
     * @param response   the response
     */
//9. Consumer query to broker
    //9. broker's response to consumer's query
    public MSG(ArtistName artistName, boolean response){
        this.type = "Query";
        this.artistName = artistName;
        this.response = response; //This is set to true by the broker in charge of the input artist.
    }

    //Phase 2 Implementations-----------------------------------------------------------<

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param initial the initial
     */
//10. consumer request to broker for artist list.
    public MSG(Void initial){
        this.type = "Request";
        this.sender = "Consumer";
    }

    /**
     * Constructor
     * Instantiates a new Msg.
     *
     * @param initial    the initial
     * @param artistList the artist list
     */
//11. Broker response to consumer artist list request.
    public MSG(Void initial, ArrayList<ArtistName> artistList){
        this.type = "Response";
        this.sender = "Broker";
        this.artistList = artistList;
    }

}
