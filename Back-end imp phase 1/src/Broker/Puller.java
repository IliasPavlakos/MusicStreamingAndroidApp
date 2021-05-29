//Implementation: Ηλίας Παυλάκος
package Broker;

import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;
import com.pavlakosilias.musicstreamingapp.Objects.MSG;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;
import com.pavlakosilias.musicstreamingapp.Objects.Value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//Puller----------------------------------------------------------------------------<
/* This class represents a Thread that will get the data from a publisher in charge of that data
 * and forward them to the consumer requested who requested them.
 * This thread is responsible for clean up of the current connection and streams and not the server.
 * Only the broker may create this type of thread.
 */
public class Puller extends Thread {
    //fields--------------------------------------------------------------<
    int brokerID;
    Socket consumerConnection;
    Socket publisherConnection;
    ArtistName artistName = null;
    Value value = null;

    //Constructors--------------------------------------------------------<

    //Song pull
    public Puller(int brokerID, Socket consumerConnection, Socket publisherConnection,ArtistName artistName, Value value) {
        this.brokerID = brokerID;
        this.consumerConnection = consumerConnection;
        this.publisherConnection = publisherConnection;
        this.artistName = artistName;
        this.value = value;
    }

    //Album pull
    public Puller(int brokerID, Socket consumerConnection, Socket publisherConnection, ArtistName artistName) {
        this.brokerID = brokerID;
        this.consumerConnection = consumerConnection;
        this.publisherConnection = publisherConnection;
        this.artistName = artistName;
    }

    //Run-----------------------------------------------------------------<
    public void run() {
        try {
            //Build pull request message for publisher
            MSG pullRequest = null;
            if (value != null) pullRequest = new MSG(brokerID, artistName, value);
            else if(artistName != null) pullRequest = new MSG(brokerID, artistName);

            if(pullRequest == null){
                System.out.println("Error on puller.run trying to send null request");
                throw new IOException("null request");
            }

            //Send request to publisher
            ObjectOutputStream pOut = new ObjectOutputStream(publisherConnection.getOutputStream());
            pOut.writeObject(pullRequest);

            //Wait for reply
            ObjectInputStream pIn = new ObjectInputStream(publisherConnection.getInputStream());
            MSG pushResponse = (MSG) pIn.readObject();

            pIn.close();
            pOut.close();
            publisherConnection.close();

            //Determine if was an album or song request
            if(pushResponse.album != null){

                //Send album
                ObjectOutputStream cOut = new ObjectOutputStream(consumerConnection.getOutputStream());
                cOut.writeObject(pushResponse);

            }else{ //Send chunks
                //Get total number of chunks and total raw data
                int totalChunks = pushResponse.totalChunks;
                int totalBytes = pushResponse.totalBytes;

                //Send consumer total number of chunks
                ObjectOutputStream cOut = new ObjectOutputStream(consumerConnection.getOutputStream());
                MSG preResponse = new MSG(totalChunks, totalBytes, (MusicFile) null);
                cOut.writeObject(preResponse);

                //Send the actual chunks one by one
                for(MusicFile chunk : pushResponse.chunkList){
                    MSG response = new MSG(chunk);
                    cOut.writeObject(response);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}