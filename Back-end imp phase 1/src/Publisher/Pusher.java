//Implementation: Ηλίας Παυλάκος
package Publisher;

import com.pavlakosilias.musicstreamingapp.Objects.MSG;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

import static Publisher.ScanAndHash.mp3DataExtractor.buildMusicFile;

/* This class represents a thread created by a publisher to handle a broker's request.
 * There are 2 types of requests: album or song request.
 * Based on the request type the appropriate constructor is called.
 * This thread may only be created by a publisher and only replay to a broker.
 */
public class Pusher extends Thread {
    //fields--------------------------------------------------------------<
    private Socket connection;
    private String songPath = null;
    private ArrayList<MusicFile> albumToSend = null;

    //Constructors--------------------------------------------------------<

    //Constructor for song request.
    public Pusher(Socket connection, String songPath){
        this.connection = connection;
        this.songPath = songPath;
    }

    //Constructor for album request.
    public Pusher(Socket connection, ArrayList<MusicFile> albumToSend){
        this.connection = connection;
        this.albumToSend = albumToSend;
    }

    //Run-----------------------------------------------------------------<
    public void run(){
        try {
            ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());

            //If it's a song to send
            if (albumToSend == null) {

                //Get raw data of the song the broker requested.
                File file = new File(songPath);
                MusicFile metaData = buildMusicFile(file);
                byte[] musicFileExtract = Files.readAllBytes(file.toPath());

                //If data could not be extracted.
                if (metaData == null || musicFileExtract.length == 0) {
                    //Send failure message
                    MSG msg = new MSG();
                    output.writeObject(msg);

                //Else, go ahead and send the data.
                } else {
                    ArrayList<MusicFile> chunkList = new ArrayList<MusicFile>();

                    //Get chunks of 1 KB
                    int totalData = musicFileExtract.length;
                    int rest = totalData % 524288;
                    int pos = 0; int totalChunks = 0;
                    for(pos = 0; pos < totalData - rest; pos+= 524288){
                        byte[] data = new byte[524288];
                        for(int j = 0; j < 524288; j++){
                            data[j] = musicFileExtract[pos + j];
                        }
                        MusicFile chunk = new MusicFile(metaData, data);
                        ++totalChunks;
                        chunkList.add(chunk);
                        if(pos + 524288 > totalData) break;
                    }

                    //If there is more data left, less than 1 KB
                    if(rest != 0) {
                        int i = 0;
                        byte[] data = new byte[rest];
                        while (pos < totalData) {
                            data[i] = musicFileExtract[pos];
                            i++;
                            pos++;
                        }
                        MusicFile chunk = new MusicFile(metaData, data);
                        ++totalChunks;
                        chunkList.add(chunk);
                    }


                    //Send chunkList
                    MSG response = new MSG(totalChunks , totalData, chunkList);
                    output.writeObject(response);
                }


            //Else, its an album to send.
            } else {

                //Send the album
                if(!albumToSend.isEmpty()) {
                    MSG msg = new MSG(albumToSend);
                    output.writeObject(msg);

                //Else, send failure message.
                }else{
                    //Send failure message
                    MSG msg = new MSG();
                    output.writeObject(msg);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }//End of run


}//End of RequestHandler
