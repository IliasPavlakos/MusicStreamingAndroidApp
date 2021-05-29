//Implementation: Ηλίας Παυλάκος
package Run;

import com.pavlakosilias.musicstreamingapp.Consumer.Consumer;
import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;

import java.util.ArrayList;
import java.util.Random;

import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.getSystemIP;
import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.printAlbumInConsole;

public class Consumer3 {

    public static void main(String[] args) {
        //Create a consumer
        Consumer c3 = new Consumer(3);

        //Register to a random broker
        c3.register(getSystemIP(), new Random().nextInt(2) + 5001);

        ArrayList<ArtistName> artistList = c3.getArtists();
        System.out.println("-----------------Available Artists----------------------");
        for(int i = 0; i < artistList.size(); i++){
            System.out.println(artistList.get(i).getArtistName());
        }
        System.out.println("--------------------------------------------------------\n");

        //Get and print an album info in console
        ArrayList<MusicFile> album1 = c3.getAlbum("Alexander Nakarada");
        printAlbumInConsole(album1);

        //Download and save the chunks of a random song from the album
        MusicFile song1 =  album1.get(new Random().nextInt(album1.size()));  //get a random song from the artist requested above.
        ArrayList<MusicFile> chunkList1 = c3.getSongAndSaveItChunkByChunk(song1.artistName, song1,
                "C:\\Users\\paula\\Desktop\\c3chunks"); //TODO: Change the path to your consumer 3 folder

        //Restore the mp3 file and save the song.
        c3.saveSong(chunkList1, "C:\\Users\\paula\\Desktop\\c3chunks"); //TODO: Change the path to your consumer 3 folder
    }

}