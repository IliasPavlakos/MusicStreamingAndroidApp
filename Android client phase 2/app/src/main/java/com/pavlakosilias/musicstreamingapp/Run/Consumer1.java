//Implementation: Ηλίας Παυλάκος
package com.pavlakosilias.musicstreamingapp.Run;

import com.pavlakosilias.musicstreamingapp.Consumer.Consumer;
import com.pavlakosilias.musicstreamingapp.IOHelper;
import com.pavlakosilias.musicstreamingapp.Objects.MusicFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.getSystemIP;
import static com.pavlakosilias.musicstreamingapp.Objects.Helpers.printAlbumInConsole;

public class Consumer1 {

    public static void main(String[] args) {
        //Create a consumer
        Consumer c1 = new Consumer(1);

        //Register to a random broker
        c1.register(getSystemIP(), new Random().nextInt(2) + 5001);

        //Get and print an album info in console
        ArrayList<MusicFile> album1 = c1.getAlbum("Rafael Krux"); //If you want change this to another artist
        printAlbumInConsole(album1);

        //Download and save the chunks of a random song from the album
        MusicFile song1 = album1.get(new Random().nextInt(album1.size())); //get a random song from the artist requested above.

        ArrayList<MusicFile> chunkList1 = null;

        File out;
        try {
            out = IOHelper.getInstance().getFileForTrack(song1);
            chunkList1 = c1.getSongAndSaveItChunkByChunk(song1.artistName, song1);
            //Restore the mp3 file and save the song.
            //c1.saveSong(chunkList1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}