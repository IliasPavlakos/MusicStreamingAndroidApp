//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Consumer;

import com.pavlakosilias.musicstreamingapp.Node.Node;
import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;
import com.pavlakosilias.musicstreamingapp.Objects.MSG;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;
import com.pavlakosilias.musicstreamingapp.Objects.Value;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Consumer extends Node{
    //fields----------------------------------------------------------------------------<
    List<MSG> availableBrokers;
    List chunkQueue; //Will be needed in phase 2

    //Constructors----------------------------------------------------------------------<

    //Constructor
    public Consumer(int id) {
        super();
        this.id = id;
        this.chunkQueue = new LinkedList<MusicFile>();
    }

    //methods---------------------------------------------------------------------------<

    //Register to the broker provided and get all the available brokers.
    public boolean register(String address, int port) {
        boolean connected = false;
        System.out.print("Retrieving available brokers info...");
        try {
            //Connect to broker.
            Socket connection = new Socket(address, port);

            //Send a register message.
            ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
            MSG msg = new MSG(id);
            output.writeObject(msg);

            //Wait for response
            ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
            MSG response = (MSG) input.readObject();

            //Check response format.
            if (response.type.equals("RegisterResponse") && response.sender.equals("Broker")) {
                this.availableBrokers = response.availableBrokers;
                connected = true;
                System.out.println("....Done!");
            } else {
                System.out.println("Failed to retrieve brokers info.");
            }

            //Clean up.
            output.close();
            input.close();
            connection.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connected;
    }

    //Returns a connection to the broker in charge of the input artist.
    private Socket ConnectToBrokerInCharge(ArtistName artistName) {
        Socket connection = null;
        String ip = null;
        int port = 0;
        try {
            //Ask available brokers until the broker in charge of the input artist has been found.
            if (artistName != null) {
                for (MSG broker : availableBrokers) {
                    //Connect to the broker
                    connection = new Socket(broker.ip, broker.port);
                    ip = broker.ip;
                    port = broker.port;

                    //Send query
                    ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                    MSG query = new MSG(artistName, false);
                    out.writeObject(query);

                    //Get response
                    ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                    MSG response = (MSG) in.readObject();

                    //clean up
                    out.close();
                    in.close();
                    connection.close();

                    //If broker in charge is found, break
                    if (response.response) break;
                }
            } else {
                int rand = new Random().nextInt(availableBrokers.size());
                ip = availableBrokers.get(rand).ip;
                port = availableBrokers.get(rand).port;
            }
            return new Socket(ip, port);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Returns all the artists available
    public ArrayList<ArtistName> getArtists() {
        try {
            //Build request message
            Void initial = null;
            MSG artistListRequest = new MSG(initial);

            //Connect to random broker
            Socket connection = ConnectToBrokerInCharge(null);

            //Send request.
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(artistListRequest);
            System.out.print("Requested album data...");

            //Wait for response
            System.out.print("Awaiting response.....");
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            MSG response = (MSG) in.readObject();

            //clean up
            out.close();
            in.close();
            connection.close();

            //Check response
            if (response.type.equals("Failure"))
                throw new IOException("Consumer.getAlbum : Album returned is null");

            //Return list requested
            System.out.println("!Done.");
            return response.artistList;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Returns the album of the input artist.(Only metadata of the songs)
    public ArrayList<MusicFile> getAlbum(String artist) {
        ArrayList<MusicFile> album;
        try {
            //Build request message
            ArtistName artistName = new ArtistName(artist, new Random().nextInt(15) + 1);
            MSG albumRequest = new MSG(artistName, null);

            //Connect to broker
            Socket connection = ConnectToBrokerInCharge(artistName);

            if(connection == null){
                System.out.println("Error: on Consumer.getAlbum null connection.");
                return null;
            }

            //Send request.
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(albumRequest);
            System.out.print("Requested album data...");

            //Wait for response
            System.out.print("Awaiting response.....");
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            MSG response = (MSG) in.readObject();

            //Clean up
            out.close();
            in.close();
            connection.close();

            //Check response
            if (response.type.equals("Failure"))
                throw new IOException("Consumer.getAlbum : Album returned is null");

            //Return album requested
            System.out.println("!Done.");
            return response.album;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Returns a list of chunks of the input song
    public ArrayList<MusicFile> getSong(String artist, MusicFile song) {
        chunkQueue.clear();
        ArrayList<MusicFile> chunkList = new ArrayList<>();
        try {
            //Build request message
            ArtistName artistName = new ArtistName(artist);
            Value value = new Value(song);
            MSG songRequest = new MSG(artistName, value);

            //Connect to broker
            Socket connection = ConnectToBrokerInCharge(artistName);

            //Send request
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(songRequest);
            System.out.print("Requested song data...");

            //Wait for response
            System.out.print("Awaiting response...");
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            MSG response = (MSG) in.readObject();

            int totalChunks = response.totalChunks;

            for (int i = 0; i < totalChunks; i++) {
                MSG chunkResponse = (MSG) in.readObject();
                MusicFile chunk = chunkResponse.chunk;
                chunkList.add(chunk);
                chunkQueue.add(chunk);
            }

            //clean up
            out.close();
            in.close();
            connection.close();

            //Return album requested
            System.out.println("!Done.");
            return chunkList;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Reforges the music file and saves it locally to the input path.
    public void saveSong(ArrayList<MusicFile> chunks, String folderToSave) {
        System.out.print("Saving...");
        try {
            //Get total file size
            int totalBytes = 0;
            for (MusicFile chunk : chunks) totalBytes += chunk.musicFileExtract.length;

            //Build file
            byte[] musicFileExtract = new byte[totalBytes];
            int i = 0;
            for (MusicFile chunk : chunks) {
                for (byte b : chunk.musicFileExtract) {
                    musicFileExtract[i] = b;
                    ++i;
                }
            }

            //Save it
            FileOutputStream fos = new FileOutputStream(
                    folderToSave + "\\RESTORED_" + chunks.get(0).trackName + ".mp3");
            fos.write(musicFileExtract);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("!Done.");
    }

    //Play the song
    public void playSong(ArrayList<MusicFile> chunks) {
        //In phase 2, this method will open and play the music chunk by chunk on the client's phone.
    }


    //Other methods (for examination purposes)------------------------------------------<

    //Combines saveSong() and saveChunk(). This is mainly for examination.
    //We sleep for 1 seconds for each chunk in order to make the downloading of chunks more
    // visible to the user.
    public ArrayList<MusicFile> getSongAndSaveItChunkByChunk(String artist, MusicFile song, String folderToSave) {
        ArrayList<MusicFile> chunkList = new ArrayList<>();
        try {
            //Build request message
            ArtistName artistName = new ArtistName(artist);
            Value value = new Value(song);
            MSG songRequest = new MSG(artistName, value);

            //Connect to broker
            Socket connection = ConnectToBrokerInCharge(artistName);

            //Send request
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            out.writeObject(songRequest);
            System.out.print("Requested song data...");

            //Wait for response
            System.out.print("Awaiting response...");
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            MSG response = (MSG) in.readObject();

            int totalChunks = response.totalChunks;

            for (int i = 0; i < totalChunks; i++) {
                MSG chunkResponse = (MSG) in.readObject();
                MusicFile chunk = chunkResponse.chunk;
                chunkList.add(chunk);
                saveChunk(chunk, i + 1, folderToSave);
                TimeUnit.SECONDS.sleep(1); //TODO: Remove this if not needed
            }

            //Return song requested
            System.out.println("!Done.");
            return chunkList;
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Save the chunk locally to the input path.
    private void saveChunk(MusicFile chunk, int chunkID, String folderToSave) {
        try {
            byte[] musicFileExtract = new byte[chunk.musicFileExtract.length];
            int i = 0;
            for (byte b : chunk.musicFileExtract) {
                musicFileExtract[i] = b;
                ++i;
            }

            FileOutputStream fos = new FileOutputStream(
                    folderToSave + "\\Chunk_" + chunkID + ".mp3");
            fos.write(musicFileExtract);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Saves the chunks locally to the input path.
    public void saveChunks(ArrayList<MusicFile> chunks, String folderToSave) {
        System.out.print("Saving...");
        try {
            int count = 1;
            for (MusicFile chunk : chunks) {
                byte[] musicFileExtract = new byte[chunk.musicFileExtract.length];
                int i = 0;
                for (byte b : chunk.musicFileExtract) {
                    musicFileExtract[i] = b;
                    ++i;
                }

                FileOutputStream fos = new FileOutputStream(
                        folderToSave + "\\Chunk_" + count + ".mp3");
                fos.write(musicFileExtract);
                ++count;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("!Done.");
    }

}//End of Consumer