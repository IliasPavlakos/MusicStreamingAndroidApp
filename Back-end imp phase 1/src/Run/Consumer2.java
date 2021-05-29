//Implementation: Ηλίας Παυλάκος
package Run;


import com.pavlakosilias.musicstreamingapp.Consumer.Consumer;
import com.pavlakosilias.musicstreamingapp.Objects.ArtistName;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;

import java.util.ArrayList;
import java.util.Random;

import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.getSystemIP;
import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.printAlbumInConsole;

public class Consumer2 {

    public static void main(String[] args) {
        //Create a consumer
        Consumer c2 = new Consumer(2);

        //Register to a random broker
        c2.register("192.168.1.2", new Random().nextInt(2) + 5001);

        ArrayList<ArtistName> artistList = c2.getArtists();
        System.out.println("-----------------Available Artists----------------------");
        for(int i = 0; i < artistList.size(); i++){
            System.out.println(artistList.get(i).getArtistName());
        }
        System.out.println("--------------------------------------------------------\n");

        //Get and print an album info in console
        ArrayList<MusicFile> album1 = c2.getAlbum("Kevin MacLeod"); //If you want change this to another artist
        printAlbumInConsole(album1);

        //Download and save the chunks of a random song from the album
        MusicFile song1 = album1.get(new Random().nextInt(album1.size()));  //get a random song from the artist requested above.
        ArrayList<MusicFile> chunkList1 = c2.getSongAndSaveItChunkByChunk(song1.artistName, song1,
                "C:\\Users\\paula\\Desktop\\c2chunks"); //TODO: Change the path to your consumer 2 folder

        //Restore the mp3 file and save the song.
        c2.saveSong(chunkList1 ,"C:\\Users\\paula\\Desktop\\c2chunks"); //TODO: Change the path to your consumer 2 folder

    }

}